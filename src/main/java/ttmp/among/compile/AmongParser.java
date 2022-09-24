package ttmp.among.compile;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.compile.Report.ReportType;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongMacroDef;
import ttmp.among.obj.AmongObject;
import ttmp.among.obj.AmongOperatorDef;
import ttmp.among.obj.AmongPrimitive;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.MacroParameter;
import ttmp.among.util.MacroParameterList;
import ttmp.among.util.MacroType;
import ttmp.among.util.OperatorRegistry;
import ttmp.among.util.OperatorType;
import ttmp.among.util.Source;

import java.util.ArrayList;
import java.util.List;

import static ttmp.among.compile.AmongToken.TokenType.*;

/**
 * Eats token. Shits object. Crazy.
 */
public final class AmongParser{
	private final AmongRoot root;
	private final AmongEngine engine;
	private final AmongTokenizer tokenizer;
	private final List<Report> reports = new ArrayList<>();

	private boolean recovering;

	public AmongParser(Source source, AmongEngine engine, AmongRoot root){
		this.engine = engine;
		this.root = root;
		this.tokenizer = new AmongTokenizer(source, this, this.root);
	}

	public AmongEngine engine(){
		return engine;
	}

	public CompileResult parse(){
		try{
			among();
		}catch(Halt ignored){
			// no-op
		}catch(RuntimeException ex){
			reportError("Unexpected error", ex);
		}
		return new CompileResult(tokenizer.source(), root, reports);
	}


	private void among(){
		while(true){
			tokenizer.discard();
			AmongToken next = tokenizer.next(true, TokenizationMode.WORD);
			if(next.is(EOF)) return;
			if(next.is(WORD)){
				switch(next.expectLiteral()){
					case "def":
						def(next.start);
						continue;
					case "undef":
						undef();
						continue;
				}
			}
			tokenizer.reset(true);
			Among a = nameable(TokenizationMode.NAME, false);
			if(a==null){
				next = tokenizer.next(true, TokenizationMode.NAME);
				if(!next.is(COMPLEX_PRIMITIVE)){
					reportError("Top level statements can only be def statement, undef statement, named/unnamed collections, or primitives denoted with ' or \"");
					tryToRecover(TokenizationMode.NAME);
					continue;
				}
				a = Among.value(next.expectLiteral());
			}
			root.addObject(a);
		}
	}

	private void def(int startIndex){
		AmongToken next = tokenizer.next(true, TokenizationMode.WORD);
		if(!next.isLiteral()){
			reportError("Invalid def statement");
			tryToRecover(TokenizationMode.WORD);
			return;
		}
		String name = next.expectLiteral();
		next = tokenizer.next(true, TokenizationMode.WORD);
		switch(next.type){
			case COLON:
				defMacro(name, MacroType.CONST);
				break;
			case L_BRACE:
				defMacro(name, MacroType.OBJECT);
				break;
			case L_BRACKET:
				defMacro(name, MacroType.LIST);
				break;
			case L_PAREN:
				defMacro(name, MacroType.OPERATION);
				break;
			default:
				if(next.is(WORD, "as")) defOperator(name, startIndex);
				else{
					reportError("Invalid def statement; expected '{', '[', '(', ':' (macro definitions) or 'as' (operator definition)");
					tryToRecover(TokenizationMode.WORD);
				}
		}
	}

	private void defMacro(String name, MacroType type){
		MacroParameterList params;
		if(type==MacroType.CONST) params = MacroParameterList.of();
		else{
			switch(type){
				case OBJECT:
					params = defParam(R_BRACE);
					break;
				case LIST:
					params = defParam(R_BRACKET);
					break;
				case OPERATION:
					params = defParam(R_PAREN);
					break;
				default: throw new IllegalStateException("Unreachable");
			}
			switch(tokenizer.next(true, TokenizationMode.UNEXPECTED).type){
				case COLON: break;
				case EOF:
					reportError("Incomplete def statement");
					return;
				default:
					reportError("Expected ':' after parameter definition");
					tryToRecover(TokenizationMode.NAME);
					return;
			}
		}
		root.addMacro(new AmongMacroDef(name, type, params, exprOrError(true)));
	}

	private MacroParameterList defParam(AmongToken.TokenType closure){
		List<MacroParameter> params = new ArrayList<>();
		while(true){
			AmongToken next = tokenizer.next(true, TokenizationMode.PARAM);
			if(next.is(closure)) break;
			if(next.is(EOF)) break; // It will be reported in defMacro()
			if(!next.is(PARAM_NAME)){
				reportError("Expected parameter name");
				if(tryToRecover(TokenizationMode.PARAM, closure, true)) break;
				else continue;
			}
			String name = next.expectLiteral();
			next = tokenizer.next(true, TokenizationMode.PARAM);

			Among def;
			if(next.is(EQ)){
				def = exprOrError(false);
				next = tokenizer.next(true, TokenizationMode.PARAM);
			}else def = null;
			params.add(new MacroParameter(name, def));

			if(next.is(closure)) break;
			else if(!next.is(COMMA)){
				reportError("Expected ',' or "+closure.friendlyName());
				if(tryToRecover(TokenizationMode.PARAM, closure, true)) break;
			}
		}
		return MacroParameterList.of(params);
	}

	private void defOperator(String name, int startIndex){
		OperatorType type;
		switch(tokenizer.next(true, TokenizationMode.NAME).keywordOrEmpty()){
			case "binary":
				type = OperatorType.BINARY;
				break;
			case "prefix":
				type = OperatorType.PREFIX;
				break;
			case "postfix":
				type = OperatorType.POSTFIX;
				break;
			default:
				reportError("Expected keyword; 'binary', 'prefix' or 'postfix'");
				skipUntilLineBreak();
				return;
		}
		boolean isKeyword;
		switch(tokenizer.next(true, TokenizationMode.NAME).keywordOrEmpty()){
			case "operator":
				isKeyword = false;
				break;
			case "keyword":
				isKeyword = true;
				break;
			default:
				reportError("Expected keyword; 'operator' or 'keyword'");
				skipUntilLineBreak();
				return;
		}

		tokenizer.discard();
		double priority = Double.NaN;
		if(tokenizer.next(true, TokenizationMode.UNEXPECTED).is(COLON)){
			priority = tokenizer.next(true, TokenizationMode.VALUE)
					.asNumber();
			if(Double.isNaN(priority)) reportError("Expected number");
		}else tokenizer.reset(true);
		AmongOperatorDef def = new AmongOperatorDef(name, isKeyword, type, priority);
		OperatorRegistry.RegistrationResult result = root.operators().add(def);
		if(!result.isSuccess())
			report(engine.allowInvalidOperatorRegistration ?
							ReportType.WARN : ReportType.ERROR,
					result.message(def), startIndex);
	}

	private void undef(){
		AmongToken next = tokenizer.next(true, TokenizationMode.NAME);
		if(next.is(NAME)){
			tokenizer.discard();
			AmongToken next2 = tokenizer.next(false, TokenizationMode.NAME);
			if(next2.isLiteral()){
				switch(next.expectLiteral()){
					case "operator":
						undefOperator(next2.expectLiteral(), false);
						return;
					case "keyword":
						undefOperator(next2.expectLiteral(), true);
						return;
				}
			}
			tokenizer.reset();
		}else if(!next.isLiteral()){
			reportError("Expected name");
			skipUntilLineBreak();
			return;
		}
		String name = next.expectLiteral();
		tokenizer.discard();
		next = tokenizer.next(false, TokenizationMode.WORD);
		MacroType type;
		switch(next.type){
			case L_BRACE:
				expectNext(R_BRACE);
				next = tokenizer.next(false, TokenizationMode.UNEXPECTED);
				type = MacroType.OBJECT;
				break;
			case L_BRACKET:
				expectNext(R_BRACKET);
				next = tokenizer.next(false, TokenizationMode.UNEXPECTED);
				type = MacroType.LIST;
				break;
			case L_PAREN:
				expectNext(R_PAREN);
				next = tokenizer.next(false, TokenizationMode.UNEXPECTED);
				type = MacroType.OPERATION;
				break;
			case BR:
				type = MacroType.CONST;
				break;
			default:
				reportError("Expected '{', '[', '(' or line break");
				skipUntilLineBreak();
				return;
		}
		if(!next.is(BR)){
			reportError("Expected newline after undef statement");
			skipUntilLineBreak();
			return;
		}
		root.removeMacro(name, type);
	}

	private void expectNext(AmongToken.TokenType type){
		if(!tokenizer.next(true, TokenizationMode.UNEXPECTED).is(type)){
			reportError("Expected "+type);
			tryToRecover(TokenizationMode.UNEXPECTED, type, false, false);
		}
	}

	private void undefOperator(String name, boolean keyword){
		AmongToken next = tokenizer.next(false, TokenizationMode.NAME);
		if(!next.is(BR)){
			reportError("Expected newline after undef statement");
			skipUntilLineBreak();
			return;
		}
		root.operators().remove(name, keyword);
	}

	private Among exprOrError(boolean macro){
		Among among = expr(macro);
		return among==null ? Among.value("ERROR") : among;
	}
	@Nullable private Among expr(boolean macro){
		tokenizer.discard();
		Among a = nameable(TokenizationMode.NAME, macro);
		if(a!=null) return a;
		tokenizer.reset(true);
		AmongToken next = tokenizer.next(true, TokenizationMode.VALUE, macro);
		if(!next.isLiteral()){
			reportError("Expected value");
			tokenizer.reset(true);
			return null;
		}
		AmongPrimitive p = Among.value(next.expectLiteral());
		if(next.is(PARAM_REF)) p.setParamRef(true);
		return next.is(COMPLEX_PRIMITIVE) ? p : primitiveMacro(p);
	}

	@Nullable private Among nameable(TokenizationMode mode, boolean macro){
		tokenizer.discard();
		AmongToken next = tokenizer.next(true, mode, macro);
		boolean paramRef = false, applyMacro = false;
		Among nameable;
		switch(next.type){
			case PARAM_REF: paramRef = true;
			case NAME: applyMacro = true;
			case COMPLEX_PRIMITIVE:{
				// lookahead to find if it's nameable instance
				switch(tokenizer.next(false, mode, macro).type){
					case L_BRACE:{
						AmongObject o = obj(next.expectLiteral(), macro);
						if(applyMacro&&!paramRef) return objectMacro(o);
						else nameable = o;
						break;
					}
					case L_BRACKET:{
						AmongList l = list(next.expectLiteral(), macro);
						if(applyMacro&&!paramRef) return listMacro(l);
						else nameable = l;
						break;
					}
					case L_PAREN:{
						AmongList o = oper(next.expectLiteral(), macro);
						if(applyMacro&&!paramRef) return operationMacro(o);
						else nameable = engine.collapseUnaryOperation&&!o.hasName()&&o.size()==1 ? o.get(0) : o;
						break;
					}
					default:
						tokenizer.reset();
						return null;
				}
				nameable.setParamRef(paramRef);
				return nameable;
			}
			case L_BRACE: return obj(null, macro);
			case L_BRACKET: return list(null, macro);
			case L_PAREN:{
				AmongList o = oper(null, macro);
				return engine.collapseUnaryOperation&&!o.hasName()&&o.size()==1 ? o.get(0) : o;
			}
			default:
				tokenizer.reset();
				return null;
		}
	}

	private AmongObject obj(@Nullable String name, boolean macro){
		AmongObject object = Among.namedObject(name);
		L:
		while(true){
			AmongToken keyToken = tokenizer.next(true, TokenizationMode.KEY);
			switch(keyToken.type){
				case EOF: reportError("Unterminated object");
				case R_BRACE: break L;
			}
			if(!keyToken.isLiteral()){
				reportError("Expected property key");
				if(tryToRecover(TokenizationMode.KEY, R_BRACE, true)) break;
				else continue;
			}

			if(!tokenizer.next(true, TokenizationMode.UNEXPECTED).is(COLON)){
				reportError("Expected ':' after property key");
				if(tryToRecover(TokenizationMode.KEY, R_BRACE, true)) break;
				else continue;
			}
			String key = keyToken.expectLiteral();
			if(object.hasProperty(key))
				report(engine.allowDuplicateObjectProperty ? ReportType.WARN : ReportType.ERROR,
						"Property '"+key+"' is already defined", keyToken.start);

			Among expr = exprOrError(macro);

			if(!object.hasProperty(key)) object.setProperty(key, expr);
			AmongToken next = tokenizer.next(false, TokenizationMode.UNEXPECTED);
			switch(next.type){
				case BR:
					tokenizer.discard();
					next = tokenizer.next(true, TokenizationMode.KEY);
					if(!next.is(AmongToken.TokenType.COMMA)) tokenizer.reset();
					break;
				case COMMA: break;
				case EOF: reportError("Unterminated object");
				case R_BRACE: break L;
				default:
					reportError("Each object property should be separated with either line breaks or ','");
					if(tryToRecover(TokenizationMode.KEY, R_BRACE, true)) break;
			}
		}
		return object;
	}

	private AmongList list(@Nullable String name, boolean macro){
		AmongList list = Among.namedList(name);
		L:
		while(true){
			tokenizer.discard();
			switch(tokenizer.next(true, TokenizationMode.UNEXPECTED).type){
				case EOF: reportError("Unterminated list");
				case R_BRACKET: break L;
			}
			tokenizer.reset(true);
			Among expr = expr(macro);
			if(expr!=null) list.add(expr);
			AmongToken next = tokenizer.next(false, TokenizationMode.UNEXPECTED);
			switch(next.type){
				case BR:
					tokenizer.discard();
					next = tokenizer.next(true, TokenizationMode.UNEXPECTED);
					if(!next.is(AmongToken.TokenType.COMMA))
						tokenizer.reset(true);
					break;
				case COMMA: break;
				case EOF: reportError("Unterminated list");
				case R_BRACKET: break L;
				default:
					reportError("Each value should be separated with either line breaks or ','");
					if(tryToRecover(TokenizationMode.VALUE, R_BRACKET, true)) break;
			}
		}
		return list;
	}

	private AmongList oper(@Nullable String name, boolean macro){
		AmongList list = Among.namedList(name);
		L:
		while(true){
			tokenizer.discard();
			switch(tokenizer.next(true, TokenizationMode.OPERATION, macro).type){
				case EOF: reportError("Unterminated operation");
				case R_PAREN: break L;
			}
			tokenizer.reset();
			list.add(operationExpression(root.operators().priorityGroup(), 0, macro));
			switch(tokenizer.next(true, TokenizationMode.OPERATION, macro).type){
				case COMMA: continue;
				case EOF: reportError("Unterminated operation");
				case R_PAREN: break L;
				default:
					reportError("Each term should be separated with ','");
					tokenizer.reset();
			}
		}
		return list;
	}

	private Among operationExpression(List<OperatorRegistry.PriorityGroup> operators, int i, boolean macro){
		if(i<operators.size()){ // check for operators and keywords
			OperatorRegistry.PriorityGroup group = operators.get(i);
			switch(group.type()){
				case BINARY:{
					Among a = operationExpression(operators, i+1, macro);
					while(true){
						tokenizer.discard();
						AmongToken next = tokenizer.next(true, TokenizationMode.OPERATION);
						if(next.isOperatorOrKeyword()){
							AmongOperatorDef op = group.get(next.expectLiteral());
							if(op!=null){
								a = operationMacro(Among.namedList(op.name(), a, operationExpression(operators, i+1, macro)));
								continue;
							}
						}
						tokenizer.reset();
						return a;
					}
				}
				case POSTFIX:{
					Among a = operationExpression(operators, i+1, macro);
					while(true){
						tokenizer.discard();
						AmongToken next = tokenizer.next(true, TokenizationMode.OPERATION, macro);
						if(next.isOperatorOrKeyword()){
							AmongOperatorDef op = group.get(next.expectLiteral());
							if(op!=null){
								a = operationMacro(Among.namedList(op.name(), a));
								continue;
							}
						}
						tokenizer.reset();
						return a;
					}
				}
				case PREFIX: return prefix(operators, i, macro);
				default: throw new IllegalStateException("Unreachable");
			}
		}
		// read primitive
		tokenizer.discard();
		Among a = nameable(TokenizationMode.OPERATION, macro);
		if(a!=null) return a;
		tokenizer.reset();
		AmongToken next = tokenizer.next(true, TokenizationMode.OPERATION, macro);
		if(!next.isLiteral()){
			reportError("Expected value");
			return Among.value("ERROR");
		}
		AmongPrimitive p = Among.value(next.expectLiteral());
		if(next.is(PARAM_REF)) p.setParamRef(true);
		return next.is(COMPLEX_PRIMITIVE) ? p : primitiveMacro(p);
	}

	private Among prefix(List<OperatorRegistry.PriorityGroup> operators, int i, boolean macro){
		tokenizer.discard();
		AmongToken next = tokenizer.next(true, TokenizationMode.OPERATION, macro);
		if(next.isOperatorOrKeyword()){
			AmongOperatorDef op = operators.get(i).get(next.expectLiteral());
			if(op!=null) return operationMacro(Among.namedList(op.name(), prefix(operators, i, macro)));
		}
		tokenizer.reset();
		return operationExpression(operators, i+1, macro);
	}

	private Among primitiveMacro(AmongPrimitive primitive){ // TODO exception handling
		AmongMacroDef def = root.searchMacro(primitive.getValue(), MacroType.CONST);
		return def!=null ? def.apply(primitive) : primitive;
	}
	private Among objectMacro(AmongObject object){
		AmongMacroDef def = root.searchMacro(object.getName(), MacroType.OBJECT);
		return def!=null ? def.apply(object) : object;
	}
	private Among listMacro(AmongList list){
		AmongMacroDef def = root.searchMacro(list.getName(), MacroType.LIST);
		return def!=null ? def.apply(list) : list;
	}
	private Among operationMacro(AmongList operation){
		AmongMacroDef def = root.searchMacro(operation.getName(), MacroType.OPERATION);
		return def!=null ? def.apply(operation) : operation;
	}

	void reportWarning(String message, String... hints){
		report(ReportType.WARN, message);
	}
	void reportError(String message, String... hints){
		report(ReportType.ERROR, message);
	}
	void reportWarning(String message, int srcIndex, String... hints){
		report(ReportType.WARN, message, srcIndex, hints);
	}
	void reportError(String message, int srcIndex, String... hints){
		report(ReportType.ERROR, message, srcIndex, hints);
	}
	void reportWarning(String message, @Nullable Throwable ex, String... hints){
		report(ReportType.WARN, message, ex, hints);
	}
	void reportError(String message, @Nullable Throwable ex, String... hints){
		report(ReportType.ERROR, message, ex, hints);
	}

	void report(ReportType type, String message, String... hints){
		report(type, message, null, hints);
	}
	void report(ReportType type, String message, int srcIndex, String... hints){
		report(type, message, srcIndex, null, hints);
	}

	void report(ReportType type, String message, @Nullable Throwable ex, String... hints){
		AmongToken lastToken = tokenizer.lastToken();
		report(type, message, lastToken!=null ? lastToken.start : -1, ex, hints);
	}
	void report(ReportType type, String message, int srcIndex, @Nullable Throwable ex, String... hints){
		if(!recovering) reports.add(new Report(type, message, srcIndex, ex, hints));
	}

	private void skipUntilLineBreak(){
		while(true){
			switch(tokenizer.next(false, TokenizationMode.NAME).type){
				case BR: case EOF: return;
			}
		}
	}

	/**
	 * Attempt to continue the compilation process by throwing away some tokens. It will still try to account for
	 * object/list/whatever definitions and parse them recursively before throwing it away again.
	 *
	 * @param mode Tokenization mode to use for
	 */
	private void tryToRecover(TokenizationMode mode){
		tryToRecover(mode, null, false);
	}
	/**
	 * Attempt to continue the compilation process by throwing away some tokens. It will still try to account for
	 * object/list/whatever definitions and parse them recursively before throwing it away again.
	 *
	 * @param mode          Tokenization mode to use for
	 * @param closure       Closure token to search for
	 * @param returnOnComma This method return on comma if the value is {@code true}
	 * @return Whether it found the closure or not
	 */
	private boolean tryToRecover(TokenizationMode mode, @Nullable AmongToken.TokenType closure, boolean returnOnComma){
		return tryToRecover(mode, closure, returnOnComma, true);
	}
	/**
	 * Attempt to continue the compilation process by throwing away some tokens. It will still try to account for
	 * object/list/whatever definitions and parse them recursively before throwing it away again.
	 *
	 * @param mode              Tokenization mode to use for
	 * @param closure           Closure token to search for
	 * @param returnOnComma     Returns on comma if the value is {@code true}
	 * @param returnOnLineBreak Returns on line break if the value is {@code true}
	 * @return Whether it found the closure or not
	 */
	private boolean tryToRecover(TokenizationMode mode, @Nullable AmongToken.TokenType closure, boolean returnOnComma, boolean returnOnLineBreak){
		boolean prevRecovering = this.recovering;
		this.recovering = true;
		while(true){
			tokenizer.discard();
			AmongToken.TokenType t = tokenizer.next(false, mode).type;
			switch(t){
				case BR: if(!returnOnLineBreak) continue;
				case EOF:
					this.recovering = prevRecovering;
					return false; // continue from here (well, there might not be much to do if it's EOF lmao)
				case COMMA:
					if(returnOnComma){
						this.recovering = prevRecovering;
						return false;
					}else continue;
				case L_BRACE:
				case L_BRACKET:
				case L_PAREN:
					tokenizer.reset();
					nameable(TokenizationMode.NAME, false); // read object and throw it away
					continue;
				default: if(t==closure){
					this.recovering = prevRecovering;
					return true;
				}
			}
		}
	}

	/**
	 * Special exception for halting the process
	 */
	private static final class Halt extends Sussy{}
}

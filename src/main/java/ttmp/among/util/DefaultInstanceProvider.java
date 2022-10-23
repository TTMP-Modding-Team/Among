package ttmp.among.util;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.compile.ReportType;
import ttmp.among.definition.AmongDefinition;
import ttmp.among.definition.Macro;
import ttmp.among.definition.MacroType;
import ttmp.among.definition.OperatorPriorities;
import ttmp.among.definition.OperatorProperty;
import ttmp.among.definition.OperatorRegistry;
import ttmp.among.definition.OperatorType;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;

import java.util.function.BiConsumer;

/**
 * Provider for "native files" that can be imported from all among scripts. This object is automatically registered on
 * all instance of {@link AmongEngine}s.
 */
public final class DefaultInstanceProvider implements Provider<RootAndDefinition>{
	public static final String DEFAULT_OPERATOR = "among/default_operator";
	public static final String DEFAULT_OPERATORS = "among/default_operators";
	public static final String EVAL = "among/eval";

	private DefaultInstanceProvider(){}
	private static final DefaultInstanceProvider INSTANCE = new DefaultInstanceProvider();
	public static DefaultInstanceProvider instance(){
		return INSTANCE;
	}

	@Nullable @Override public RootAndDefinition resolve(String path){
		if(DEFAULT_OPERATOR.equals(path)||DEFAULT_OPERATORS.equals(path))
			return new RootAndDefinition(defaultOperators());
		if(EVAL.equals(path))
			return new RootAndDefinition(eval());
		return null;
	}

	/**
	 * Create a new definition with basic operators.
	 *
	 * @return New definition with basic operators
	 * @see <a href="https://youtu.be/J2C9oiP5j7Q">ok this link is supposed to be a link to the documentation about
	 * "basic operators" but i kinda dont have one now so i will just put a rickroll here if you dont mind yea just poke
	 * me if i have that doc up somewhere but didnt update this link ok thx</a>
	 */
	public static AmongDefinition defaultOperators(){
		AmongDefinition definition = new AmongDefinition();
		OperatorRegistry o = definition.operators();
		o.addOperator("=", OperatorType.BINARY, OperatorProperty.RIGHT_ASSOCIATIVE, OperatorPriorities.BINARY_ASSIGN);
		o.addOperator("||", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_OR);
		o.addOperator("&&", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_AND);
		o.addOperator("==", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_EQUALITY);
		o.addOperator("!=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_EQUALITY);
		o.addOperator(">", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("<", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator(">=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("<=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("|", OperatorType.BINARY, OperatorPriorities.BINARY_BITWISE);
		o.addOperator("&", OperatorType.BINARY, OperatorPriorities.BINARY_BITWISE);
		o.addOperator("+", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_ADDITION);
		o.addOperator("-", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_ADDITION);
		o.addOperator("*", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("/", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("^", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("**", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_POWER);
		o.addOperator("!", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("-", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("+", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator(".", OperatorType.BINARY, "", OperatorProperty.ACCESSOR, OperatorPriorities.BINARY_ACCESS);
		return definition;
	}

	/**
	 * Create new definition with eval function.
	 *
	 * @return New definition with eval function
	 */
	public static AmongDefinition eval(){
		AmongDefinition definition = defaultOperators();
		definition.macros().add(Macro.builder("eval", MacroType.OPERATION)
				.param("expr")
				.build((args, copyConstant, reportHandler) -> eval(args[0], reportHandler)));
		return definition;
	}

	@Nullable private static Among eval(Among among, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(among.isList()){
			AmongList list = among.asList();
			switch(list.getName()){
				case "||": return binaryBool(list, BinaryBoolOp.OR, reportHandler);
				case "&&": return binaryBool(list, BinaryBoolOp.AND, reportHandler);
				case "|": return binaryBool(list, BinaryBoolOp.OR_STRICT, reportHandler);
				case "&": return binaryBool(list, BinaryBoolOp.AND_STRICT, reportHandler);
				case "==": case "=": return binaryObj(list, BinaryObjOp.EQ, reportHandler);
				case "!=": return binaryObj(list, BinaryObjOp.NEQ, reportHandler);
				case ">": return binaryNum(list, BinaryNumOp.GT, reportHandler);
				case "<": return binaryNum(list, BinaryNumOp.LT, reportHandler);
				case ">=": return binaryNum(list, BinaryNumOp.GTEQ, reportHandler);
				case "<=": return binaryNum(list, BinaryNumOp.LTEQ, reportHandler);
				case "+": return list.size()<=1 ? unary(list, UnaryOp.PLUS, reportHandler) : binaryNum(list, BinaryNumOp.ADD, reportHandler);
				case "-": return list.size()<=1 ? unary(list, UnaryOp.NEGATE, reportHandler) : binaryNum(list, BinaryNumOp.SUB, reportHandler);
				case "*": return binaryNum(list, BinaryNumOp.MUL, reportHandler);
				case "/": return binaryNum(list, BinaryNumOp.DIV, reportHandler);
				case "^": case "**": return binaryNum(list, BinaryNumOp.POW, reportHandler);
				case "!": unary(list, UnaryOp.NOT, reportHandler);
			}
		}
		return among;
	}

	private enum BinaryObjOp{EQ, NEQ}
	@Nullable private static Among binaryObj(AmongList list, BinaryObjOp op, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(expect(list, 2, reportHandler)){
			Among a = eval(list.get(0), reportHandler);
			if(a==null) return null;
			Among b = eval(list.get(1), reportHandler);
			if(b==null) return null;
			switch(op){
				case EQ: return Among.value(eq(a, b));
				case NEQ: return Among.value(!eq(a, b));
			}
		}
		return null;
	}

	private static boolean eq(Among a, Among b){
		if(a.equals(b)) return true;
		Boolean aBool = toBool(a);
		if(aBool!=null) return aBool.equals(toBool(b));
		Double aNum = toNum(a);
		if(aNum!=null) return aNum.equals(toNum(b));
		return false;
	}

	private enum BinaryBoolOp{AND, OR, AND_STRICT, OR_STRICT}
	@Nullable private static Among binaryBool(AmongList list, BinaryBoolOp op, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(expect(list, 2, reportHandler)){
			Boolean a = evalBool(list.get(0), reportHandler);
			if(a==null) return null;
			switch(op){
				case AND_STRICT: if(!a) return Among.value(false); break;
				case OR_STRICT: if(a) return Among.value(true); break;
			}
			Boolean b = evalBool(list.get(1), reportHandler);
			if(b==null) return null;
			switch(op){
				case AND: case AND_STRICT: return Among.value(a&&b);
				case OR: case OR_STRICT: return Among.value(a||b);
			}
		}
		return null;
	}

	private enum BinaryNumOp{ADD, SUB, MUL, DIV, POW, GT, LT, GTEQ, LTEQ}
	@Nullable private static Among binaryNum(AmongList list, BinaryNumOp op, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(expect(list, 2, reportHandler)){
			Double a = evalNum(list.get(0), reportHandler);
			if(a==null) return null;
			Double b = evalNum(list.get(1), reportHandler);
			if(b==null) return null;
			switch(op){
				case ADD: return Among.value(a+b);
				case SUB: return Among.value(a-b);
				case MUL: return Among.value(a*b);
				case DIV: return Among.value(a/b);
				case POW: return Among.value(Math.pow(a, b));
				case GT: return Among.value(a>b);
				case LT: return Among.value(a<b);
				case GTEQ: return Among.value(a>=b);
				case LTEQ: return Among.value(a<=b);
			}
		}
		return null;
	}

	private enum UnaryOp{NEGATE, PLUS, NOT}
	@Nullable private static Among unary(AmongList list, UnaryOp op, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(expect(list, 1, reportHandler)){
			if(op==UnaryOp.NOT){
				Boolean a = evalBool(list.get(0), reportHandler);
				if(a==null) return null;
				return Among.value(!a);
			}
			Double a = evalNum(list.get(0), reportHandler);
			if(a==null) return null;
			switch(op){
				case NEGATE: return Among.value(-a);
				case PLUS: return Among.value(a);
			}
		}
		return null;
	}

	private static boolean expect(AmongList list, int size, @Nullable BiConsumer<ReportType, String> reportHandler){
		if(list.size()>=size) return true;
		if(reportHandler!=null)
			reportHandler.accept(ReportType.ERROR, "Invalid input '"+list+"': Not enough parameters, minimum "+size+" required");
		return false;
	}
	@Nullable private static Boolean evalBool(Among among, @Nullable BiConsumer<ReportType, String> reportHandler){
		Among a2 = eval(among, reportHandler);
		if(a2==null) return null;
		Boolean b = toBool(a2);
		if(b==null&&reportHandler!=null) reportHandler.accept(ReportType.ERROR, "Invalid input '"+a2+"': Expected boolean");
		return null;
	}
	@Nullable private static Double evalNum(Among among, @Nullable BiConsumer<ReportType, String> reportHandler){
		Among a2 = eval(among, reportHandler);
		if(a2==null) return null;
		Double n = toNum(a2);
		if(n==null&&reportHandler!=null) reportHandler.accept(ReportType.ERROR, "Invalid input '"+a2+"': Expected number");
		return n;
	}
	@Nullable private static Boolean toBool(Among among){
		if(among.isPrimitive()) try{
			return among.asPrimitive().getBoolValue();
		}catch(Sussy ignored){}
		return null;
	}
	@Nullable private static Double toNum(Among among){
		if(among.isPrimitive()) try{
			return among.asPrimitive().getDoubleValue();
		}catch(NumberFormatException ignored){}
		return null;
	}
}

package ttmp.among.obj;

import ttmp.among.exception.Sussy;
import ttmp.among.util.AmongMacroDefBuilder;
import ttmp.among.util.AmongUs;
import ttmp.among.util.AmongWalker;
import ttmp.among.util.MacroParameter;
import ttmp.among.util.MacroParameterList;
import ttmp.among.util.MacroReplacement;
import ttmp.among.util.MacroSignature;
import ttmp.among.util.MacroType;
import ttmp.among.util.NodePath;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Macro definitions. Snippet below shows macros with each type written in Among.
 * <pre>
 * def macro : "Hello!"
 * def macro{} : "Hello!"
 * def macro[] : "Hello!"
 * def macro() : "Hello!"
 * </pre>
 */
public final class AmongMacroDef implements ToPrettyString{
	public static AmongMacroDefBuilder builder(){
		return new AmongMacroDefBuilder();
	}

	private final MacroSignature signature;
	private final MacroParameterList parameter;
	private final Among object;
	private final List<MacroReplacement> replacements;

	// only initialized correctly in list/operation macros and only used on such instance
	private final int nonDefaultParams;

	/**
	 * Creates new macro definition.
	 *
	 * @param name   Name of the macro
	 * @param type   Type of the macro
	 * @param params Parameters of the macro
	 * @param object Result object of the macro; valid parameter references will be marked for replacements
	 * @throws NullPointerException If either of the parameters are {@code null}
	 * @throws Sussy If one of the arguments are invalid
	 */
	public AmongMacroDef(String name, MacroType type, MacroParameterList params, Among object){
		this(new MacroSignature(name, type), params, object);
	}
	/**
	 * Creates new macro definition.
	 *
	 * @param sig    Signature of the macro
	 * @param params Parameters of the macro
	 * @param object Result object of the macro; valid parameter references will be marked for replacements
	 * @throws NullPointerException If either of the parameters are {@code null}
	 * @throws Sussy If one of the arguments are invalid
	 */
	public AmongMacroDef(MacroSignature sig, MacroParameterList params, Among object){
		int nonDefaultParams = 0;
		switch(sig.type()){
			case CONST:
				if(!params.isEmpty())
					throw new Sussy("Constant definitions cannot have parameter");
				break;
			case LIST:
			case OPERATION:
				nonDefaultParams = params.checkConsecutiveOptionalParams();
				break;
		}
		this.signature = sig;
		this.parameter = params;
		this.object = Objects.requireNonNull(object);
		this.replacements = replacementFromObject(this.parameter, object);
		this.nonDefaultParams = nonDefaultParams;
	}

	public MacroSignature signature(){
		return signature;
	}
	public String name(){
		return signature.name();
	}
	public MacroType type(){
		return signature.type();
	}
	public MacroParameterList parameter(){
		return parameter;
	}

	/**
	 * Whether this macro has a characteristic of being a constant macro.<br>
	 * A macro is considered 'constant' when no element is modified with parameter. Macro with no parameter is always
	 * constant.<br>
	 * Not to be confused with {@link MacroType#CONST}.
	 *
	 * @return Whether this macro has a characteristic of being a constant macro
	 */
	public boolean isConstant(){
		return parameter.isEmpty()||replacements.isEmpty();
	}

	/**
	 * Applies this macro to given object. The object will not be modified; either new object or fixed 'constant' object
	 * will be given, based on context.<br>
	 * As a const object could be
	 *
	 * @return Template object itself (if def is constant), or deep copy of it
	 */
	public Among apply(Among among){
		return apply(among, false);
	}
	/**
	 * @return Template object itself (if def is constant and {@code copyConstant == false}), or deep copy of it
	 */
	public Among apply(Among among, boolean copyConstant){
		if(isConstant()) return copyConstant ? object.copy() : object;
		Among[] args = toArgs(among);
		Among o = object.copy();
		for(MacroReplacement r : replacements)
			o = r.apply(args, o);
		return o;
	}

	private Among[] toArgs(Among among){
		switch(this.type()){
			case CONST: return new Among[0];
			case OBJECT:{
				AmongObject o = among.asObj();
				List<Among> args = new ArrayList<>();
				for(int i = 0; i<this.parameter.size(); i++){
					MacroParameter p = this.parameter.getParam(i);
					Among val = o.getProperty(p.name());
					if(val==null){
						if(p.defaultValue()!=null) val = p.defaultValue();
						else throw new Sussy("Missing argument '"+p.name()+"'");
					}
					args.add(val);
				}
				return args.toArray(new Among[0]);
			}
			case LIST:
			case OPERATION:{
				AmongList l = among.asList();
				List<Among> args = new ArrayList<>();
				if(l.size()<nonDefaultParams) throw new Sussy("Not enough parameters");
				for(int i = 0, j = Math.min(parameter.size(), l.size()); i<j; i++)
					args.add(l.get(i));
				return args.toArray(new Among[0]);
			}
			default: throw new IllegalStateException("Unreachable");
		}
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		AmongMacroDef that = (AmongMacroDef)o;
		return signature.equals(that.signature)&&parameter.equals(that.parameter)&&object.equals(that.object);
	}
	@Override public int hashCode(){
		return Objects.hash(signature, parameter, object);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		stb.append("def ");
		AmongUs.nameToString(stb, name());
		switch(this.type()){
			case OBJECT:
				stb.append("{").append(parameter).append("}");
				break;
			case LIST:
				stb.append("[").append(parameter).append("]");
				break;
			case OPERATION:
				stb.append("(").append(parameter).append(")");
				break;
		}
		return stb.append(" : ").append(object).toString();
	}
	@Override public String toPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		stb.append("def ");
		AmongUs.nameToPrettyString(stb, name(), indents, indent);
		switch(this.type()){
			case OBJECT:
				stb.append("{").append(parameter.toPrettyString(indents, indent)).append("}");
				break;
			case LIST:
				stb.append("[").append(parameter.toPrettyString(indents, indent)).append("]");
				break;
			case OPERATION:
				stb.append("(").append(parameter.toPrettyString(indents, indent)).append(")");
				break;
		}
		return stb.append(" : ").append(object.toPrettyString(indents, indent)).toString();
	}

	public static List<MacroReplacement> replacementFromObject(MacroParameterList params, Among object){
		if(params.isEmpty()) return Collections.emptyList();
		List<MacroReplacement> replacements = new ArrayList<>();
		object.walk(new AmongWalker(){
			@Override public void walk(AmongPrimitive primitive, NodePath path){
				if(primitive.isParamRef()&&primitive.getValue().startsWith("$")){
					int i = params.indexOf(primitive.getValue().substring(1));
					if(i>=0) replacements.add(new MacroReplacement(path, i, MacroReplacement.Target.VALUE));
				}
			}
			@Override public boolean walk(AmongObject object, NodePath path){
				if(object.isParamRef()&&object.getName().startsWith("$")){
					int i = params.indexOf(object.getName().substring(1));
					if(i>=0) replacements.add(new MacroReplacement(path, i, MacroReplacement.Target.NAMEABLE_NAME));
				}
				return true;
			}
			@Override public boolean walk(AmongList list, NodePath path){
				if(list.isParamRef()&&list.getName().startsWith("$")){
					int i = params.indexOf(list.getName().substring(1));
					if(i>=0) replacements.add(new MacroReplacement(path, i, MacroReplacement.Target.NAMEABLE_NAME));
				}
				return true;
			}
		});
		return replacements;
	}
}

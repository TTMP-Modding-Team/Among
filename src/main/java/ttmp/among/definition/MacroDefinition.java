package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.compile.Report;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongObject;
import ttmp.among.obj.AmongPrimitive;
import ttmp.among.util.AmongUs;
import ttmp.among.util.AmongWalker;
import ttmp.among.util.NodePath;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Macro definitions. Snippet below shows macros with each type written in Among.
 * <pre>
 * macro macro : "Hello!"
 * macro macro{} : "Hello!"
 * macro macro[] : "Hello!"
 * macro macro() : "Hello!"
 * </pre>
 */
public final class MacroDefinition implements ToPrettyString{
	public static MacroDefinitionBuilder builder(){
		return new MacroDefinitionBuilder();
	}

	private final MacroSignature signature;
	private final MacroParameterList parameter;
	private final Among template;
	private final List<MacroReplacement> replacements;

	/**
	 * Creates new macro definition.
	 *
	 * @param name     Name of the macro
	 * @param type     Type of the macro
	 * @param params   Parameters of the macro
	 * @param template Result object of the macro; valid parameter references will be marked for replacements
	 * @throws NullPointerException If either of the parameters are {@code null}
	 * @throws Sussy                If one of the arguments are invalid
	 */
	public MacroDefinition(String name, MacroType type, MacroParameterList params, Among template){
		this(new MacroSignature(name, type), params, template);
	}
	/**
	 * Creates new macro definition.
	 *
	 * @param sig      Signature of the macro
	 * @param params   Parameters of the macro
	 * @param template Result object of the macro; valid parameter references will be marked for replacements
	 * @throws NullPointerException If either of the parameters are {@code null}
	 * @throws Sussy                If one of the arguments are invalid
	 */
	public MacroDefinition(MacroSignature sig, MacroParameterList params, Among template){
		switch(sig.type()){
			case CONST:
				if(!params.isEmpty())
					throw new Sussy("Constant definitions cannot have parameter");
				break;
			case LIST: case OPERATION:
				if(!params.hasConsecutiveOptionalParams())
					throw new Sussy("Optional parameters of "+(sig.type()==MacroType.LIST ? "list" : "operation")+
							" macro should be consecutive, placed at end of the parameter list");
		}
		this.signature = sig;
		this.parameter = params;
		this.template = Objects.requireNonNull(template);
		this.replacements = replacementFromObject(this.parameter, template);
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
	 * Returns deep copy of the raw template used in this macro. All parameter references will be gone. This method is strictly for debugging purposes.
	 */
	public Among template(){
		return template.copy();
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
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * This method always return new instance of the object. If you do not expect the result to be modified afterwards,
	 * you may specify {@code copyConstant} to be {@code false} for both faster and memory-efficient operation.
	 *
	 * @param argument Argument object
	 * @return Among object with macro applied
	 * @throws NullPointerException If {@code argument == null} and {@code isConstant() == false}
	 * @throws Sussy                If argument provided is invalid
	 * @see MacroDefinition#apply(Among, boolean)
	 */
	public Among apply(Among argument){
		return apply(argument, true);
	}
	/**
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * If {@code copyConstant} is {@code false}, and this macro is constant, this method will return the template
	 * object itself without copying. As the instance is shared among macro itself and possibly many other places where
	 * macro is used, modifying the result will bring consequences. This is intentional design choice to enable users
	 * to avoid possibly expensive deep copy process on right situations. Set {@code copyConstant} to {@code true} if
	 * you expect the object to be modified afterwards; otherwise you may set {@code copyConstant} to {@code false} for
	 * both faster and memory-efficient operation.
	 *
	 * @param argument     Argument object
	 * @param copyConstant If {@code true}, constant macro will return deep copy of template.
	 * @return Template object itself if {@code isConstant() == true && copyConstant == false}; deep copy of it otherwise
	 * @throws NullPointerException If {@code argument == null} and {@code isConstant() == false}
	 * @throws Sussy                If argument provided is invalid
	 * @see MacroDefinition#apply(Among, boolean, BiConsumer)
	 */
	public Among apply(Among argument, boolean copyConstant){
		return apply(argument, copyConstant, null);
	}
	/**
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * If {@code copyConstant} is {@code false}, and this macro is constant, this method will return the template
	 * object itself without copying. As the instance is shared among macro itself and possibly many other places where
	 * macro is used, modifying the result will bring consequences. This is intentional design choice to enable users
	 * to avoid possibly expensive deep copy process on right situations. Set {@code copyConstant} to {@code true} if
	 * you expect the object to be modified afterwards; otherwise you may set {@code copyConstant} to {@code false} for
	 * both faster and memory-efficient operation.
	 *
	 * @param argument      Argument object
	 * @param copyConstant  If {@code true}, constant macro will return deep copy of template.
	 * @param reportHandler Optional report handler for analyzing any compilation issues. Presence of the report handler
	 *                      does not change process.
	 * @return Template object itself if {@code isConstant() == true && copyConstant == false}; deep copy of it otherwise
	 * @throws NullPointerException If {@code argument == null} and {@code isConstant() == false}
	 * @throws Sussy                If argument provided is invalid
	 * @see MacroDefinition#apply(Among, boolean, BiConsumer)
	 */
	public Among apply(Among argument, boolean copyConstant, @Nullable BiConsumer<Report.ReportType, String> reportHandler){
		if(reportHandler!=null) analyzeAndReport(argument, reportHandler);
		if(isConstant()) return copyConstant ? template.copy() : template;
		Among[] args = toArgs(argument);
		Among o = template.copy();
		for(MacroReplacement r : replacements)
			o = r.apply(args, o);
		return o;
	}

	private void analyzeAndReport(Among argument, BiConsumer<Report.ReportType, String> reportHandler){
		switch(this.type()){
			case CONST: return; // nothing to tell ig
			case OBJECT:{
				if(!argument.isObj()){
					reportHandler.accept(Report.ReportType.ERROR, "Expected object as argument");
					return;
				}
				AmongObject o = argument.asObj();
				for(int i = 0; i<this.parameter.size(); i++){
					MacroParameter p = this.parameter.paramAt(i);
					if(p.defaultValue()==null&&!o.hasProperty(p.name()))
						reportHandler.accept(Report.ReportType.ERROR, "Missing argument '"+p.name()+'\'');
				}
				for(String key : argument.asObj().properties().keySet())
					if(this.parameter.indexOf(key)==-1)
						reportHandler.accept(Report.ReportType.WARN, "Unused argument '"+key+'\'');
				return;
			}
			case LIST: case OPERATION:{
				if(!argument.isList()){
					reportHandler.accept(Report.ReportType.ERROR, "Expected list as argument");
					return;
				}
				AmongList l = argument.asList();
				if(l.size()<parameter.requiredParameters())
					reportHandler.accept(Report.ReportType.ERROR, "Not enough parameters: minimum of "+parameter.requiredParameters()+" expected, "+l.size()+" provided");
				else if(l.size()>parameter.size())
					reportHandler.accept(Report.ReportType.WARN, "Unused parameters: maximum of "+parameter.requiredParameters()+" expected, "+l.size()+" provided");
				return;
			}
			default: throw new IllegalStateException("Unreachable");
		}
	}

	private Among[] toArgs(Among argument){
		switch(this.type()){
			case CONST: return new Among[0];
			case OBJECT:{
				AmongObject o = argument.asObj();
				List<Among> args = new ArrayList<>();
				for(int i = 0; i<this.parameter.size(); i++){
					MacroParameter p = this.parameter.paramAt(i);
					Among val = o.getProperty(p.name());
					if(val==null){
						if(p.defaultValue()!=null) val = p.defaultValue();
						else throw new Sussy("Missing argument '"+p.name()+'\'');
					}
					args.add(val);
				}
				return args.toArray(new Among[0]);
			}
			case LIST: case OPERATION:{
				AmongList l = argument.asList();
				List<Among> args = new ArrayList<>();
				if(l.size()<parameter.requiredParameters())
					throw new Sussy("Not enough parameters: minimum of "+parameter.requiredParameters()+" expected, "+l.size()+" provided");
				for(int i = 0; i<parameter.size(); i++)
					args.add(i<l.size() ?
							l.get(i) :
							Objects.requireNonNull(parameter.paramAt(i).defaultValue()));
				return args.toArray(new Among[0]);
			}
			default: throw new IllegalStateException("Unreachable");
		}
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MacroDefinition that = (MacroDefinition)o;
		return signature.equals(that.signature)&&parameter.equals(that.parameter)&&template.equals(that.template);
	}
	@Override public int hashCode(){
		return Objects.hash(signature, parameter, template);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		stb.append("macro ");
		AmongUs.nameToString(stb, name(), false);
		switch(this.type()){
			case OBJECT:
				stb.append('{').append(parameter).append('}');
				break;
			case LIST:
				stb.append('[').append(parameter).append(']');
				break;
			case OPERATION:
				stb.append('(').append(parameter).append(')');
				break;
		}
		return stb.append(':').append(template).toString();
	}
	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		stb.append("macro ");
		AmongUs.nameToPrettyString(stb, name(), false, indents, option);
		switch(this.type()){
			case OBJECT:
				stb.append('{').append(parameter.toPrettyString(indents, option)).append('}');
				break;
			case LIST:
				stb.append('[').append(parameter.toPrettyString(indents, option)).append(']');
				break;
			case OPERATION:
				stb.append('(').append(parameter.toPrettyString(indents, option)).append(')');
				break;
		}
		return stb.append(" : ").append(template.toPrettyString(indents, option)).toString();
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

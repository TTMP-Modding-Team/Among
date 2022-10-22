package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.compile.Report;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongObject;
import ttmp.among.util.AmongUs;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Base class for both user-defined macro and code-defined macro.
 */
public abstract class Macro implements ToPrettyString{
	public static MacroBuilder builder(){
		return new MacroBuilder();
	}

	private final MacroSignature signature;
	private final MacroParameterList parameter;

	protected Macro(MacroSignature signature, MacroParameterList parameter){
		switch(signature.type()){
			case CONST: case ACCESS:
				if(!parameter.isEmpty())
					throw new Sussy("Constant definitions cannot have parameter");
				break;
			case LIST: case OPERATION:
			case LIST_FN: case OPERATION_FN:
				if(!parameter.hasConsecutiveOptionalParams())
					throw new Sussy("Optional parameters of "+signature.type().friendlyName()+
							" macro should be consecutive, placed at end of the parameter list");
		}
		this.signature = signature;
		this.parameter = parameter;
	}

	public final MacroSignature signature(){
		return signature;
	}
	public final String name(){
		return signature.name();
	}
	public final MacroType type(){
		return signature.type();
	}
	public final MacroParameterList parameter(){
		return parameter;
	}

	/**
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * This method always return new instance of the object. If you do not expect the result to be modified afterwards,
	 * you may specify {@code copyConstant} to be {@code false} for both faster and memory-efficient operation.
	 *
	 * @param argument Argument object
	 * @return Among object with macro applied, or {@code null} if any error occurs
	 * @throws NullPointerException If {@code argument == null}. Note that if the macro is argument-independent, it might
	 *                              not throw an exception
	 * @throws RuntimeException     If an unexpected error occurs. The exception should be reported back as error.
	 * @see Macro#apply(Among, boolean)
	 * @see Macro#apply(Among, boolean, BiConsumer)
	 */
	public final Among apply(Among argument){
		return apply(argument, true);
	}

	/**
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * If {@code copyConstant} is {@code false}, and this macro is argument-independent, the returned instance may be
	 * shared between other places. As the instance is shared among macro itself and possibly many other places where
	 * macro is used, modifying the result will bring consequences. This is intentional design choice to enable users
	 * to avoid possibly expensive deep copy process on right situations. Set {@code copyConstant} to {@code true} if
	 * you expect the object to be modified afterwards; otherwise you may set {@code copyConstant} to {@code false} for
	 * both faster and memory-efficient operation.
	 *
	 * @param argument     Argument object
	 * @param copyConstant If {@code true}, constant macro will return deep copy of template.
	 * @return Among object with macro applied, or {@code null} if any error occurs. If the macro is argument-independent,
	 * the returned instance may be shared between other places, including the macro itself.
	 * @throws NullPointerException If {@code argument == null}. Note that if the macro is argument-independent, it might
	 *                              not throw an exception
	 * @throws RuntimeException     If an unexpected error occurs. The exception should be reported back as error.
	 * @see Macro#apply(Among, boolean, BiConsumer)
	 */
	@Nullable public final Among apply(Among argument, boolean copyConstant){
		return apply(argument, copyConstant, null);
	}

	/**
	 * Applies this macro to given object. The argument object will not be modified; either new object or fixed
	 * 'constant' object will be given, based on context.<br>
	 * If {@code copyConstant} is {@code false}, and this macro is argument-independent, the returned instance may be
	 * shared between other places. As the instance is shared among macro itself and possibly many other places where
	 * macro is used, modifying the result will bring consequences. This is intentional design choice to enable users
	 * to avoid possibly expensive deep copy process on right situations. Set {@code copyConstant} to {@code true} if
	 * you expect the object to be modified afterwards; otherwise you may set {@code copyConstant} to {@code false} for
	 * both faster and memory-efficient operation.
	 *
	 * @param argument      Argument object
	 * @param copyConstant  If {@code true}, constant macro will return deep copy of template.
	 * @param reportHandler Optional report handler for analyzing any compilation issues. Presence of the report handler
	 *                      does not change process.
	 * @return Among object with macro applied, or {@code null} if any error occurs. If the macro is argument-independent,
	 * the returned instance may be shared between other places, including the macro itself.
	 * @throws NullPointerException If {@code argument == null}. Note that if the macro is argument-independent, it might
	 *                              not throw an exception
	 * @throws RuntimeException     If an unexpected error occurs. The exception should be reported back as error.
	 */
	@Nullable public final Among apply(Among argument, boolean copyConstant, @Nullable BiConsumer<Report.ReportType, String> reportHandler){
		Among[] args = toArgs(argument, reportHandler);
		return args!=null ? applyMacro(args, copyConstant, reportHandler) : null;
	}
	protected abstract Among applyMacro(Among[] args, boolean copyConstant, @Nullable BiConsumer<Report.ReportType, String> reportHandler);

	@Nullable private Among[] toArgs(Among argument, @Nullable BiConsumer<Report.ReportType, String> reportHandler){
		switch(this.type()){
			case CONST: return new Among[0];
			case OBJECT: return objectMacro(argument, reportHandler, null);
			case LIST: case OPERATION: return listMacro(argument, reportHandler, null);
			case ACCESS: case OBJECT_FN: case LIST_FN: case OPERATION_FN:
				if(argument.isList()){
					AmongList l = argument.asList();
					int requiredSize = this.type()==MacroType.ACCESS ? 1 : 2;
					if(l.size()>=requiredSize){
						if(l.size()>requiredSize&&reportHandler!=null)
							reportHandler.accept(Report.ReportType.WARN, "Unused function parameters: "+l.size()+" provided");
						Among self = l.get(0);
						return this.type()==MacroType.ACCESS ? new Among[]{self} :
								this.type()==MacroType.OBJECT_FN ? objectMacro(l.get(1), reportHandler, self) :
										listMacro(l.get(1), reportHandler, self);
					}
				}
				if(reportHandler!=null) reportHandler.accept(Report.ReportType.ERROR, this.type()==MacroType.ACCESS ?
						"Expected 'self' as argument" :
						"Expected pair of 'self' and 'args' as argument");
				return null;
			default: throw new IllegalStateException("Unreachable");
		}
	}

	@Nullable private Among[] objectMacro(Among argument, @Nullable BiConsumer<Report.ReportType, String> reportHandler, @Nullable Among self){
		if(!argument.isObj()){
			if(reportHandler!=null) reportHandler.accept(Report.ReportType.ERROR, "Expected object as argument");
			return null;
		}
		AmongObject o = argument.asObj();
		List<Among> args = new ArrayList<>();
		if(self!=null) args.add(self);
		for(int i = 0; i<this.parameter().size(); i++){
			MacroParameter p = this.parameter().paramAt(i);
			Among val = o.getProperty(p.name());
			if(val==null){
				if(p.defaultValue()!=null) val = p.defaultValue();
				else{
					if(reportHandler!=null) reportHandler.accept(Report.ReportType.ERROR, "Missing argument '"+p.name()+'\'');
					else return null;
					args = null;
				}
			}
			if(args!=null) args.add(val);
		}
		if(reportHandler!=null)
			for(String key : argument.asObj().properties().keySet())
				if(this.parameter().indexOf(key)==-1)
					reportHandler.accept(Report.ReportType.WARN, "Unused argument '"+key+'\'');
		return args!=null ? args.toArray(new Among[0]) : null;
	}

	@Nullable private Among[] listMacro(Among argument, @Nullable BiConsumer<Report.ReportType, String> reportHandler, @Nullable Among self){
		if(!argument.isList()){
			if(reportHandler!=null) reportHandler.accept(Report.ReportType.ERROR, "Expected list as argument");
			return null;
		}
		AmongList l = argument.asList();
		if(l.size()<parameter().requiredParameters()){
			if(reportHandler!=null) reportHandler.accept(Report.ReportType.ERROR,
					"Not enough parameters: minimum of "+parameter().requiredParameters()+" expected, "+l.size()+" provided");
			return null;
		}else{
			if(l.size()>parameter().size()&&reportHandler!=null){
				reportHandler.accept(Report.ReportType.WARN,
						"Unused parameters: maximum of "+parameter().size()+" expected, "+l.size()+" provided");
			}
			List<Among> args = new ArrayList<>();
			if(self!=null) args.add(self);
			for(int i = 0; i<parameter().size(); i++)
				args.add(i<l.size() ?
						l.get(i) :
						Objects.requireNonNull(parameter().paramAt(i).defaultValue()));
			return args.toArray(new Among[0]);
		}
	}

	public final void signatureToString(StringBuilder stb){
		AmongUs.nameToString(stb, name(), false);
		switch(this.type()){
			case OBJECT: stb.append('{'); break;
			case LIST: stb.append('['); break;
			case OPERATION: stb.append('('); break;
			default: return;
		}
		stb.append(parameter);
		switch(this.type()){
			case OBJECT: stb.append('}'); break;
			case LIST: stb.append(']'); break;
			case OPERATION: stb.append(')'); break;
		}
	}

	public final void signatureToPrettyString(StringBuilder stb){
		signatureToPrettyString(stb, 0, PrettyFormatOption.DEFAULT);
	}
	public final void signatureToPrettyString(StringBuilder stb, int indents){
		signatureToPrettyString(stb, indents, PrettyFormatOption.DEFAULT);
	}
	public final void signatureToPrettyString(StringBuilder stb, PrettyFormatOption option){
		signatureToPrettyString(stb, 0, option);
	}
	public final void signatureToPrettyString(StringBuilder stb, int indents, PrettyFormatOption option){
		signatureToPrettyString(stb, indents, option, false);
	}
	public final void signatureToPrettyString(StringBuilder stb, int indents, PrettyFormatOption option, boolean replaceDefaultValueWithStubs){
		AmongUs.nameToPrettyString(stb, name(), false, indents, option);

		switch(this.type()){
			case OBJECT: stb.append('{'); break;
			case LIST: stb.append('['); break;
			case OPERATION: stb.append('('); break;
			default: return;
		}
		stb.append(parameter.toPrettyString(indents, option, replaceDefaultValueWithStubs));
		switch(this.type()){
			case OBJECT: stb.append('}'); break;
			case LIST: stb.append(']'); break;
			case OPERATION: stb.append(')'); break;
		}
	}
}

package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.compile.Report;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.util.PrettyFormatOption;

import java.util.ArrayList;
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
 *
 * Note that, due to the nature of replacement operations, the results of {@link MacroDefinition#toString()}
 * and {@link MacroDefinition#toPrettyString(int, PrettyFormatOption)} might not produce re-compilable macro script.
 */
public final class MacroDefinition extends Macro{

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
	public MacroDefinition(String name, MacroType type, MacroParameterList params, Among template, List<MacroReplacement> replacements){
		this(new MacroSignature(name, type), params, template, replacements);
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
	public MacroDefinition(MacroSignature sig, MacroParameterList params, Among template, List<MacroReplacement> replacements){
		super(sig, params);
		this.template = Objects.requireNonNull(template);
		this.replacements = new ArrayList<>(replacements);
		for(MacroReplacement r : this.replacements) Objects.requireNonNull(r);
	}

	/**
	 * Returns deep copy of the raw template used in this macro. All parameter references will be gone. This method is strictly for debugging purposes.
	 */
	public Among template(){
		return template.copy();
	}

	public List<MacroReplacement> replacements(){
		return replacements;
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
		return replacements.isEmpty();
	}

	@Override protected Among applyMacro(Among[] args, boolean copyConstant, @Nullable BiConsumer<Report.ReportType, String> reportHandler){
		if(isConstant()) return copyConstant ? template.copy() : template;
		Among o = template.copy();
		for(MacroReplacement r : replacements)
			o = r.apply(args, o, copyConstant, reportHandler);
		return o;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MacroDefinition that = (MacroDefinition)o;
		return signature().equals(that.signature())&&parameter().equals(that.parameter())&&template.equals(that.template);
	}
	@Override public int hashCode(){
		return Objects.hash(signature(), parameter(), template);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		stb.append(type().isFunctionMacro() ? "fn " : "macro ");
		signatureToString(stb);
		return stb.append(':').append(template).toString();
	}
	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		stb.append(type().isFunctionMacro() ? "fn " : "macro ");
		signatureToPrettyString(stb, indents, option);
		return stb.append(" : ").append(template.toPrettyString(indents, option)).toString();
	}
}

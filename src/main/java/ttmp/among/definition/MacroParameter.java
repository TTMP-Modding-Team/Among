package ttmp.among.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ttmp.among.obj.Among;
import ttmp.among.util.AmongUs;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.Objects;

/**
 * Parameter of the {@link MacroDefinition} - name, and default value(optional).
 *
 * @see MacroDefinition
 * @see MacroParameterList
 */
public final class MacroParameter implements Comparable<MacroParameter>, ToPrettyString{
	private final String name;
	@Nullable private final Among defaultValue;
	private final byte typeInference;

	/**
	 * Creates a new instance of macro parameter.
	 *
	 * @param name         Name of the parameter
	 * @param defaultValue Default value of the parameter. Value of {@code null} indicates the parameter is not
	 *                     optional.
	 * @see MacroParameter#MacroParameter(String, Among, byte)
	 */
	public MacroParameter(String name, @Nullable Among defaultValue){
		this(name, defaultValue, TypeInference.ANY);
	}
	/**
	 * Creates a new instance of macro parameter.
	 *
	 * @param name          Name of the parameter
	 * @param defaultValue  Default value of the parameter. Value of {@code null} indicates the parameter is not
	 *                      optional.
	 * @param typeInference Inferred type of the parameter
	 * @see TypeInference
	 */
	public MacroParameter(String name, @Nullable Among defaultValue, byte typeInference){
		this.name = name;
		this.defaultValue = defaultValue;
		this.typeInference = (byte)(typeInference&TypeInference.ANY);
	}

	/**
	 * @return Name of this parameter.
	 */
	public String name(){
		return name;
	}
	/**
	 * @return Default value of this parameter. Value of {@code null} indicates the parameter is not optional.
	 */
	@Nullable public Among defaultValue(){
		return defaultValue;
	}
	/**
	 * @return The type either inferred from usage or specified from code.
	 * @see TypeInference
	 */
	public byte typeInference(){
		return typeInference;
	}

	public boolean matchesInference(Among value){
		return TypeInference.matches(this.typeInference, value);
	}

	@Override public int compareTo(@NotNull MacroParameter o){
		return name().compareTo(o.name());
	}
	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MacroParameter parameter = (MacroParameter)o;
		return name().equals(parameter.name())&&Objects.equals(defaultValue(), parameter.defaultValue());
	}
	@Override public int hashCode(){
		return Objects.hash(name(), defaultValue());
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		AmongUs.paramToString(stb, name());
		if(defaultValue()!=null)
			stb.append('=').append(defaultValue());
		return stb.toString();
	}

	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		return toPrettyString(indents, option, false);
	}

	public String toPrettyString(int indents, PrettyFormatOption option, boolean replaceDefaultValueWithStubs){
		StringBuilder stb = new StringBuilder();
		AmongUs.paramToString(stb, name());
		if(defaultValue()!=null)
			stb.append(" = ").append(replaceDefaultValueWithStubs ? "/* default */" :
					defaultValue().toPrettyString(indents+1, option));
		return stb.toString();
	}
}

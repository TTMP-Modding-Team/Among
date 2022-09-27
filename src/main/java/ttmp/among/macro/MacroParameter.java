package ttmp.among.macro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ttmp.among.obj.Among;
import ttmp.among.util.AmongUs;
import ttmp.among.util.ToPrettyString;

import java.util.Objects;

/**
 * Parameter of the {@link MacroDefinition} - name, and default value(optional).
 * @see MacroDefinition
 * @see MacroParameterList
 */
public final class MacroParameter implements Comparable<MacroParameter>, ToPrettyString{
	private final String name;
	@Nullable private final Among defaultValue;

	public MacroParameter(String name, @Nullable Among defaultValue){
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String name(){
		return name;
	}
	@Nullable public Among defaultValue(){
		return defaultValue;
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
		if(defaultValue()!=null){
			stb.append(" = ");
			stb.append(defaultValue());
		}
		return stb.toString();
	}
	@Override public String toPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		AmongUs.paramToString(stb, name());
		if(defaultValue()!=null){
			stb.append(" = ");
			stb.append(defaultValue().toPrettyString(indents+1, indent));
		}
		return stb.toString();
	}
}

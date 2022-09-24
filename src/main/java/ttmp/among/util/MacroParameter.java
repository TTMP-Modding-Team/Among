package ttmp.among.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongMacroDef;

import java.util.Objects;

/**
 * Parameter of the {@link AmongMacroDef} - name, and default value(optional).
 * @see AmongMacroDef
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
		return name().equals(parameter.name());
	}
	@Override public int hashCode(){
		return Objects.hash(name());
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
		AmongUs.paramToPrettyString(stb, name(), indents, indent);
		if(defaultValue()!=null){
			stb.append(" = ");
			stb.append(defaultValue().toPrettyString(indents+1, indent));
		}
		return stb.toString();
	}
}

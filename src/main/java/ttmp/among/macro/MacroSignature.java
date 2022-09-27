package ttmp.among.macro;

import org.jetbrains.annotations.NotNull;
import ttmp.among.util.AmongUs;

import java.util.Objects;

/**
 * Signature of the macro - name and type.
 * @see MacroDefinition
 */
public final class MacroSignature implements Comparable<MacroSignature>{
	private final String name;
	private final MacroType type;

	public MacroSignature(String name, MacroType type){
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
	}

	public String name(){
		return name;
	}
	public MacroType type(){
		return type;
	}

	@Override public int compareTo(@NotNull MacroSignature o){
		int c = name.compareTo(o.name);
		if(c!=0) return c;
		return type.compareTo(o.type);
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MacroSignature that = (MacroSignature)o;
		return name.equals(that.name)&&type==that.type;
	}
	@Override public int hashCode(){
		return Objects.hash(name, type);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		AmongUs.nameToString(stb, name);
		switch(type){
			case OBJECT:
				stb.append("{}");
				break;
			case LIST:
				stb.append("[]");
				break;
			case OPERATION:
				stb.append("()");
				break;
			case CONST: break;
			default: throw new IllegalStateException("unreachable");
		}
		return stb.toString();
	}
}

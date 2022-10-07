package ttmp.among.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongNamed;
import ttmp.among.util.AmongUs;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.List;
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

	/**
	 * Flags indicating type of the object.
	 */
	public interface TypeInference{
		byte PRIMITIVE = 1;
		byte UNNAMED_OBJECT = 2;
		byte UNNAMED_LIST = 4;
		byte UNNAMED_OPERATION = 8;
		byte NAMED_OBJECT = 16;
		byte NAMED_LIST = 32;
		byte NAMED_OPERATION = 64;

		byte OBJECT = UNNAMED_OBJECT|NAMED_OBJECT;
		byte LIST = UNNAMED_LIST|NAMED_LIST;
		byte OPERATION = UNNAMED_OPERATION|NAMED_OPERATION;

		byte UNNAMED = UNNAMED_OBJECT|UNNAMED_LIST|UNNAMED_OPERATION;
		byte NAMED = NAMED_OBJECT|NAMED_LIST|NAMED_OPERATION;

		byte COLLECTION = UNNAMED|NAMED;

		byte ANY = PRIMITIVE|COLLECTION;

		static boolean matches(byte typeInference, Among value){
			if(typeInference==ANY) return true;
			byte flag;
			if(value.isPrimitive()) flag = PRIMITIVE;
			else{
				AmongNamed named = value.asNamed();
				if(named.isObj()) flag = named.hasName() ? NAMED_OBJECT : UNNAMED_OBJECT;
				else if(named.asList().isOperation()) flag = named.hasName() ? NAMED_OPERATION : UNNAMED_OPERATION;
				else flag = named.hasName() ? NAMED_LIST : UNNAMED_LIST;
			}
			return (typeInference&flag)!=0;
		}

		static String toString(byte flag){
			switch(flag&ANY){
				case ANY: return "Anything";
				case PRIMITIVE: return "Primitive";
				case UNNAMED_OBJECT: return "Unnamed Object";
				case UNNAMED_LIST: return "Unnamed List";
				case UNNAMED_OPERATION: return "Unnamed Operation";
				case NAMED_OBJECT: return "Named Object";
				case NAMED_LIST: return "Named List";
				case NAMED_OPERATION: return "Named Operation";
				case OBJECT: return "Object";
				case LIST: return "List";
				case OPERATION: return "Operation";
				case UNNAMED: return "Unnamed Collection";
				case NAMED: return "Named Collection";
				case COLLECTION: return "Collection";
				default:{
					List<String> l = new ArrayList<>();
					if(has(flag, PRIMITIVE)) l.add("Primitive");

					if(has(flag, COLLECTION)) l.add("Collection");
					else{
						if(has(flag, NAMED)){
							l.add("Named Collection");
							flag ^= NAMED;
						}else if(has(flag, UNNAMED)){
							l.add("Unnamed Collection");
							flag ^= UNNAMED;
						}

						if(has(flag, OBJECT)) l.add("Object");
						else if(has(flag, UNNAMED_OBJECT)) l.add("Unnamed Object");
						else if(has(flag, NAMED_OBJECT)) l.add("Named Object");
						if(has(flag, LIST)) l.add("List");
						else if(has(flag, UNNAMED_LIST)) l.add("Unnamed List");
						else if(has(flag, NAMED_LIST)) l.add("Named List");
						if(has(flag, OPERATION)) l.add("Operation");
						else if(has(flag, UNNAMED_OPERATION)) l.add("Unnamed Operation");
						else if(has(flag, NAMED_OPERATION)) l.add("Named Operation");
					}
					switch(l.size()){
						case 0: return "Invalid";
						case 1: return l.get(0);
						default: return String.join(" or ", l);
					}
				}
			}
		}

		static boolean has(byte flag, byte value){
			return (flag&value)==value;
		}
	}
}

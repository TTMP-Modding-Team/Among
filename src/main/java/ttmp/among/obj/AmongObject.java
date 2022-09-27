package ttmp.among.obj;

import org.jetbrains.annotations.Nullable;
import ttmp.among.util.AmongUs;
import ttmp.among.util.AmongWalker;
import ttmp.among.util.NodePath;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Object is a named {@link Among} node with properties. Property is a pair of a name(key) to a child node(values).
 * Snippet below shows an object written in Among.
 * <pre>
 * {
 *   Hello: "Hello!"
 *   ThisIsKey: "This is value"
 *   EachElementAre: "Divided by either line breaks or ','."
 *   NestedObject: {
 *     Look: "A nested list!"
 *   }
 * }
 * </pre>
 */
public class AmongObject extends AmongNamed{
	private final Map<String, Among> properties = new LinkedHashMap<>(); // Use linkedhashmap to preserve insertion order

	AmongObject(){}
	AmongObject(Map<String, Among> map){
		this(null, map);
	}
	AmongObject(@Nullable String name){
		super(name);
	}
	AmongObject(@Nullable String name, Map<String, Among> map){
		super(name);
		this.properties.putAll(map);
		for(Among among : this.properties.values())
			Objects.requireNonNull(among);
	}

	/**
	 * Sets the property with name {@code key} to {@code value}. This is utility method for method chaining.<br>
	 * As this method is supposed to be used primarily for object initialization in code, duplicated properties are not
	 * allowed.
	 *
	 * @param key   Key of the property
	 * @param value Value of the property. If the value is an instance of {@link Among}, it will be directly set as
	 *              property; otherwise it will be converted to its string representation via {@link Object#toString()}
	 *              before being wrapped around {@link AmongPrimitive}.
	 * @return This
	 * @throws IllegalStateException If property for given key was already defined
	 * @throws NullPointerException  If either {@code key == null} or {@code value == null}
	 */
	public AmongObject prop(String key, Object value){
		return prop(key, value instanceof Among ? (Among)value : value(value));
	}
	/**
	 * Sets the property with name {@code key} to {@code value}. This is utility method for method chaining.<br>
	 * As this method is supposed to be used primarily for object initialization in code, duplicated properties are not
	 * allowed.
	 *
	 * @param key   Key of the property
	 * @param value Value of the property
	 * @return This
	 * @throws IllegalStateException If property for given key was already defined
	 * @throws NullPointerException  If either {@code key == null} or {@code value == null}
	 */
	public AmongObject prop(String key, Among value){
		if(this.properties.putIfAbsent(key, Objects.requireNonNull(value))!=null)
			throw new IllegalStateException("Property '"+key+"' is already defined");
		return this;
	}

	/**
	 * @return Unmodifiable view of the properties
	 */
	public Map<String, Among> properties(){
		return Collections.unmodifiableMap(properties);
	}

	public boolean hasProperty(String key){
		return this.properties.containsKey(key);
	}

	@Nullable public Among getProperty(String key){
		return this.properties.get(key);
	}

	@Nullable public Among setProperty(String key, @Nullable Among value){
		if(value==null) return this.properties.remove(key);
		else return this.properties.put(key, value);
	}

	@Nullable public Among removeProperty(String key){
		return this.properties.remove(key);
	}

	public boolean isEmpty(){
		return properties.isEmpty();
	}
	public void clear(){
		properties.clear();
	}

	@Override public AmongObject asObj(){
		return this;
	}
	@Override public boolean isObj(){
		return true;
	}

	@Override public void walk(AmongWalker visitor, NodePath path){
		if(visitor.walk(this, path))
			for(Map.Entry<String, Among> e : this.properties.entrySet())
				e.getValue().walk(visitor, path.subPath(e.getKey()));
	}

	@Override public AmongObject copy(){
		AmongObject a = new AmongObject(this.getName(), this.properties);
		a.setParamRef(isParamRef());
		return a;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		AmongObject o2 = (AmongObject)o;
		return getName().equals(o2.getName())&&
				properties.equals(o2.properties);
	}
	@Override public int hashCode(){
		return Objects.hash(getName(), properties);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		if(hasName()) AmongUs.nameToString(stb, getName(), isParamRef());
		if(isEmpty()) stb.append("{}");
		else{
			stb.append('{');
			boolean first = true;
			for(Map.Entry<String, Among> e : properties.entrySet()){
				if(first) first = false;
				else stb.append(',');
				AmongUs.keyToString(stb, e.getKey(), false);
				stb.append(':');
				AmongUs.valueToString(stb, e.getValue());
			}
			stb.append('}');
		}
		return stb.toString();
	}

	@Override public String toPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		if(hasName()){
			AmongUs.nameToPrettyString(stb, getName(), isParamRef(), indents+1, indent);
			stb.append(' ');
		}
		if(isEmpty()) stb.append("{}");
		else{
			stb.append('{');
			for(Map.Entry<String, Among> e : properties.entrySet()){
				stb.append('\n');
				for(int i = 0; i<indents+1; i++) stb.append(indent);
				AmongUs.keyToPrettyString(stb, e.getKey(), false, indents+1, indent);
				stb.append(": ");
				AmongUs.valueToPrettyString(stb, e.getValue(), indents+1, indent);
			}
			stb.append('\n');
			for(int i = 0; i<indents; i++) stb.append(indent);
			stb.append('}');
		}
		return stb.toString();
	}
}

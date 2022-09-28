package ttmp.among.obj;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.compile.Source;
import ttmp.among.exception.SussyCast;
import ttmp.among.macro.MacroDefinition;
import ttmp.among.util.AmongWalker;
import ttmp.among.util.NodePath;
import ttmp.among.util.ToPrettyString;

import java.util.Map;

/**
 * Base class for all among nodes.
 *
 * @see AmongPrimitive
 * @see AmongObject
 * @see AmongList
 * @see AmongNamed
 */
public abstract class Among implements ToPrettyString{
	private boolean paramRef;

	/**
	 * Mark this as a reference to macro parameter. If it is {@code true}:<br>
	 * <ul>
	 *     <li>For primitive value, the value itself is considered as parameter reference.</li>
	 *     <li>For named instances, the name is considered as parameter reference.</li>
	 * </ul>
	 * This value does not change any behavior of this object; it is simply a flag used in {@link MacroDefinition}.
	 * Additionally, not all the parameter reference are valid.
	 *
	 * @return This
	 * @see Among#isParamRef()
	 */
	public final Among paramRef(){
		setParamRef(true);
		return this;
	}

	/**
	 * Whether this object is a reference to macro parameter. If it is {@code true}:<br>
	 * <ul>
	 *     <li>For primitive value, the value itself is considered as parameter reference.</li>
	 *     <li>For named instances, the name is considered as parameter reference.</li>
	 * </ul>
	 * This value does not change any behavior of this object; it is simply a flag used in {@link MacroDefinition}.
	 * Additionally, not all the parameter reference are valid.
	 *
	 * @return Whether this object is a reference to macro parameter
	 */
	public final boolean isParamRef(){
		return paramRef;
	}

	/**
	 * Mark this as a reference to macro parameter. If it is {@code true}:<br>
	 * <ul>
	 *     <li>For primitive value, the value itself is considered as parameter reference.</li>
	 *     <li>For named instances, the name is considered as parameter reference.</li>
	 * </ul>
	 * This value does not change any behavior of this object; it is simply a flag used in {@link MacroDefinition}.
	 * Additionally, not all the parameter reference are valid.
	 *
	 * @param paramRef Whether this object is a reference to macro parameter
	 * @see Among#isParamRef()
	 */
	public final void setParamRef(boolean paramRef){
		this.paramRef = paramRef;
	}

	/**
	 * Return this object as {@link AmongObject} instance.
	 *
	 * @return This object as {@link AmongObject} instance.
	 * @throws SussyCast If this object is not {@link AmongObject}
	 */
	public AmongObject asObj(){
		throw new SussyCast(AmongObject.class, this.getClass());
	}

	/**
	 * Returns whether this object is {@link AmongObject} instance.
	 *
	 * @return Whether this object is {@link AmongObject} instance.
	 */
	public boolean isObj(){
		return false;
	}

	/**
	 * Return this object as {@link AmongList} instance.
	 *
	 * @return This object as {@link AmongList} instance.
	 * @throws SussyCast If this object is not {@link AmongList}
	 */
	public AmongList asList(){
		throw new SussyCast(AmongList.class, this.getClass());
	}

	/**
	 * Returns whether this object is {@link AmongList} instance.
	 *
	 * @return Whether this object is {@link AmongList} instance.
	 */
	public boolean isList(){
		return false;
	}

	/**
	 * Return this object as {@link AmongPrimitive} instance.
	 *
	 * @return This object as {@link AmongPrimitive} instance.
	 * @throws SussyCast If this object is not {@link AmongPrimitive}
	 */
	public AmongPrimitive asPrimitive(){
		throw new SussyCast(AmongPrimitive.class, this.getClass());
	}

	/**
	 * Returns whether this object is {@link AmongPrimitive} instance.
	 *
	 * @return Whether this object is {@link AmongPrimitive} instance.
	 */
	public boolean isPrimitive(){
		return false;
	}

	/**
	 * Return this object as {@link AmongNamed} instance.
	 *
	 * @return This object as {@link AmongNamed} instance.
	 * @throws SussyCast If this object is not {@link AmongNamed}
	 */
	public AmongNamed asNamed(){
		throw new SussyCast(AmongNamed.class, this.getClass());
	}

	/**
	 * Returns whether this object is {@link AmongNamed} instance.
	 *
	 * @return Whether this object is {@link AmongNamed} instance.
	 */
	public boolean isNamed(){
		return false;
	}

	/**
	 * Returns a string representation of this object. Parsing the string with {@link AmongEngine#read(Source,
	 * AmongRoot)} will create identical copy of this object, excluding {@link Among#paramRef} field.
	 *
	 * @return String representation of this object
	 */
	@Override public abstract String toString();

	/**
	 * Create a deep copy of the object.
	 *
	 * @return Copy of the object
	 */
	public abstract Among copy();

	/**
	 * Visit the node tree of objects in depth-first order.
	 *
	 * @param visitor Specific operation to be performed on each node
	 */
	public final void walk(AmongWalker visitor){
		walk(visitor, NodePath.of());
	}

	/**
	 * Visit the node tree of objects in depth-first order.
	 *
	 * @param visitor Specific operation to be performed on each node
	 * @param path    The starting path - i.e. path of this object
	 */
	public abstract void walk(AmongWalker visitor, NodePath path);

	/**
	 * Create an instance of {@link AmongPrimitive} with empty value.
	 *
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(){
		return new AmongPrimitive();
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The boolean value will be converted to its string representation by {@link Boolean#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(boolean value){
		return new AmongPrimitive(Boolean.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The byte value will be converted to its string representation by {@link Character#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(char value){
		return new AmongPrimitive(Character.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The byte value will be converted to its string representation by {@link Byte#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(byte value){
		return new AmongPrimitive(Byte.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The short value will be converted to its string representation by {@link Short#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(short value){
		return new AmongPrimitive(Short.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The short value will be converted to its string representation by {@link Integer#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(int value){
		return new AmongPrimitive(Integer.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The short value will be converted to its string representation by {@link Long#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(long value){
		return new AmongPrimitive(Long.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The short value will be converted to its string representation by {@link Float#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(float value){
		return new AmongPrimitive(Float.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The short value will be converted to its string representation by {@link Double#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 */
	public static AmongPrimitive value(double value){
		return new AmongPrimitive(Double.toString(value));
	}

	/**
	 * Create an instance of {@link AmongPrimitive} with the value provided.
	 * The object will be converted to string its representation by {@link Object#toString()}.
	 *
	 * @param value Value
	 * @return A new instance of {@link AmongPrimitive}
	 * @throws NullPointerException If {@code value == null}
	 */
	public static AmongPrimitive value(Object value){
		return new AmongPrimitive(value.toString());
	}

	/**
	 * Create an empty instance of {@link AmongObject}.
	 *
	 * @return A new instance of {@link AmongObject}
	 */
	public static AmongObject object(){
		return new AmongObject();
	}

	/**
	 * Create an empty instance of {@link AmongObject}.
	 *
	 * @param properties Map of each property of the object
	 * @return A new instance of {@link AmongObject}
	 * @throws NullPointerException If {@code properties == null}
	 */
	public static AmongObject object(Map<String, Among> properties){
		return new AmongObject(null, properties);
	}

	/**
	 * Create an empty instance of {@link AmongObject} with a name. Providing {@code null} or empty string for the
	 * name essentially creates an unnamed object.
	 *
	 * @param name Name of the object
	 * @return A new instance of {@link AmongObject}
	 */
	public static AmongObject namedObject(@Nullable String name){
		return new AmongObject(name);
	}

	/**
	 * Create an instance of {@link AmongObject} with a name. Providing {@code null} or empty string for the name
	 * essentially creates an unnamed object.
	 *
	 * @param name       Name of the object
	 * @param properties Map of each property of the object
	 * @return A new instance of {@link AmongObject}
	 * @throws NullPointerException If {@code properties == null}
	 */
	public static AmongObject namedObject(@Nullable String name, Map<String, Among> properties){
		return new AmongObject(name, properties);
	}

	/**
	 * Create an empty instance of {@link AmongList}.
	 *
	 * @return A new instance of {@link AmongList}
	 */
	public static AmongList list(){
		return new AmongList();
	}

	/**
	 * Create an instance of {@link AmongList} with elements.
	 *
	 * @param elements Initial elements of the object
	 * @return A new instance of {@link AmongList}
	 */
	public static AmongList list(Object... elements){
		return namedList(null, elements);
	}

	/**
	 * Create an empty instance of {@link AmongList} with a name. Providing {@code null} or empty string for the name
	 * essentially creates an unnamed list.
	 *
	 * @param name Name of the object
	 * @return A new instance of {@link AmongList}
	 */
	public static AmongList namedList(@Nullable String name){
		return new AmongList(name);
	}

	/**
	 * Create an empty instance of {@link AmongList} with a name and elements. Providing {@code null} or empty string
	 * for the name
	 * essentially creates an unnamed list.
	 *
	 * @param name     Name of the object
	 * @param elements Initial elements of the object
	 * @return A new instance of {@link AmongList}
	 */
	public static AmongList namedList(@Nullable String name, Object... elements){
		AmongList l = new AmongList(name);
		for(Object o : elements){
			if(o instanceof Among) l.add((Among)o);
			else l.add(o.toString());
		}
		return l;
	}
}

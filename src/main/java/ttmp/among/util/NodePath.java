package ttmp.among.util;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.SussyCast;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Object representing relative path in node structure of {@link Among} instances.<br>
 * In a nutshell, it is an array of either {@code String} (property) or {@code int} (index)s. The properties are used to
 * access properties of {@link AmongObject} instances, and the indices are used to access elements of {@link
 * AmongList}.
 */
public final class NodePath implements Iterable<NodePath.Element>{
	/**
	 * Empty path. (meaning it has 0 (zero) elements(meaning it points to the node itself))
	 */
	public static final NodePath EMPTY = new NodePath(); // TODO encapsulate constructors

	private final Element[] path;

	public NodePath(Element... path){
		this.path = path.clone();
		for(Element e : this.path)
			Objects.requireNonNull(e);
	}
	public NodePath(NodePath path, String property){
		this(path, new Property(property));
	}
	public NodePath(NodePath path, int index){
		this(path, new Index(index));
	}
	public NodePath(NodePath path, Element... subpath){
		List<Element> list = new ArrayList<>();
		Collections.addAll(list, path.path);
		Collections.addAll(list, subpath);
		this.path = list.toArray(new Element[0]);
		for(Element e : this.path)
			Objects.requireNonNull(e);
	}

	public int size(){
		return path.length;
	}
	public boolean isEmpty(){
		return path.length==0;
	}

	public Element get(int index){
		return path[index];
	}

	/**
	 * Returns the object this path is pointing at.
	 *
	 * @param among The object containing result
	 * @return The object this path is pointing at, or {@code null} if there is no such element.
	 */
	@Nullable public Among resolveAndGet(Among among){
		return resolveAndGet(among, 0, path.length);
	}
	/**
	 * Returns the object this path is pointing at.
	 *
	 * @param among The object containing result
	 * @param start Start index of the path to be used, inclusive
	 * @param end   End index of the path to be used, exclusive
	 * @return The object this path is pointing at, or {@code null} if there is no such element.
	 */
	@Nullable public Among resolveAndGet(Among among, int start, int end){
		for(int i = Math.max(0, start); i<Math.min(path.length, end); i++){
			among = path[i].resolve(among);
			if(among==null) break;
		}
		return among;
	}

	/**
	 * Sets the element this path is pointing in {@code among} as {@code element}. If the parent node of last path
	 * cannot be resolved, the operation fails.<br>
	 * This operation always fails with empty path regardless of the parameter; because it means replacing itself, which
	 * is not something this method can achieve on its own.
	 *
	 * @param among   Object to put {@code element} into
	 * @param element Object to be set
	 * @return {@code element} if it overwrites itself (on empty path), {@code among} otherwise
	 */
	public boolean resolveAndSet(Among among, Among element){
		if(isEmpty()) return false; // Overwriting itself always fails
		Among a = resolveAndGet(among, 0, path.length-1);
		if(a!=null){
			path[path.length-1].set(a, element);
			return true;
		}else return false;
	}

	@Override public Iterator<Element> iterator(){
		return Arrays.stream(path).iterator();
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		NodePath nodePath = (NodePath)o;
		return Arrays.equals(path, nodePath.path);
	}
	@Override public int hashCode(){
		return Arrays.hashCode(path);
	}

	@Override public String toString(){
		return Arrays.stream(path).map(Object::toString).collect(Collectors.joining());
	}

	/**
	 * Base type of both {@link Property} and {@link Index}.
	 *
	 * @see Property
	 * @see Index
	 */
	public static abstract class Element{
		private Element(){}

		public boolean isProperty(){
			return false;
		}
		public String property(){
			throw new SussyCast(Property.class, this.getClass());
		}

		public boolean isIndex(){
			return false;
		}
		public int index(){
			throw new SussyCast(Index.class, this.getClass());
		}

		@Nullable public abstract Among resolve(Among among);
		public abstract void set(Among among, Among element);
	}

	/**
	 * Object representing a property name.
	 */
	public static final class Property extends Element{
		public final String property;

		public Property(String property){
			this.property = Objects.requireNonNull(property);
		}

		@Override public boolean isProperty(){
			return true;
		}
		@Override public String property(){
			return this.property;
		}

		@Nullable @Override public Among resolve(Among among){
			return among.isObj() ? among.asObj().getProperty(property) : null;
		}
		@Override public void set(Among among, Among element){
			if(among.isObj()) among.asObj().setProperty(property, element);
		}

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			Property property1 = (Property)o;
			return property.equals(property1.property);
		}
		@Override public int hashCode(){
			return Objects.hash(property);
		}

		@Override public String toString(){
			return "."+property;
		}
	}

	/**
	 * Object representing an index.
	 */
	public static class Index extends Element{
		public final int index;

		public Index(int index){
			this.index = index;
		}

		@Override public boolean isIndex(){
			return true;
		}
		@Override public int index(){
			return this.index;
		}

		@Nullable @Override public Among resolve(Among among){
			if(among.isList()){
				AmongList l = among.asList();
				if(index>=0&&index<l.size()) return l.get(index);
			}
			return null;
		}
		@Override public void set(Among among, Among element){
			if(among.isList()) among.asList().set(index, element);
		}

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			Index index1 = (Index)o;
			return index==index1.index;
		}
		@Override public int hashCode(){
			return Objects.hash(index);
		}

		@Override public String toString(){
			return "["+index+"]";
		}
	}
}

package ttmp.among.obj;

import ttmp.among.util.AmongUs;
import ttmp.among.util.AmongWalker;
import ttmp.among.util.NodePath;
import ttmp.among.util.PrettyFormatOption;

import java.util.Objects;

/**
 * {@link Among} node representing a string value. Snippet below shows a primitive written in Among.
 * <pre>
 * "Hello!"
 * </pre>
 */
public class AmongPrimitive extends Among{
	private String value;

	AmongPrimitive(){
		this("");
	}
	AmongPrimitive(String value){
		this.value = Objects.requireNonNull(value);
	}

	public String getValue(){
		return value;
	}
	public void setValue(String value){
		this.value = Objects.requireNonNull(value);
	}

	@Override public AmongPrimitive asPrimitive(){
		return this;
	}
	@Override public boolean isPrimitive(){
		return true;
	}

	@Override public void walk(AmongWalker visitor, NodePath path){
		visitor.walk(this, path);
	}

	@Override public AmongPrimitive copy(){
		AmongPrimitive a = new AmongPrimitive(this.value);
		a.setParamRef(isParamRef());
		return a;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		AmongPrimitive that = (AmongPrimitive)o;
		return Objects.equals(value, that.value);
	}
	@Override public int hashCode(){
		return Objects.hash(value);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		AmongUs.valueToString(stb, this);
		return stb.toString();
	}

	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		AmongUs.valueToPrettyString(stb, this, indents, option);
		return stb.toString();
	}
}

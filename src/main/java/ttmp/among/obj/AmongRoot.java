package ttmp.among.obj;

import ttmp.among.exception.Sussy;
import ttmp.among.util.AmongUs;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents all objects defined in single source.
 */
public final class AmongRoot implements ToPrettyString{
	private final List<Among> objects;

	/**
	 * Create an empty root.
	 */
	public AmongRoot(){
		this.objects = new ArrayList<>();
	}
	private AmongRoot(AmongRoot copyFrom){
		this.objects = new ArrayList<>(copyFrom.objects);
	}

	public List<Among> objects(){
		return Collections.unmodifiableList(objects);
	}
	public int objectSize(){
		return objects.size();
	}
	public Among getObject(int index){
		return objects.get(index);
	}
	public void addObject(Among among){
		this.objects.add(Objects.requireNonNull(among));
	}
	public Among removeObject(int index){
		return objects.remove(index);
	}
	public boolean isEmpty(){
		return objects.isEmpty();
	}
	public void clear(){
		objects.clear();
	}

	/**
	 * Returns the object defined. Only one object is expected; none or multiple objects will produce exception.
	 *
	 * @return The only object defined
	 * @throws Sussy If there's none or multiple object defined
	 */
	public Among singleObject(){
		switch(objects.size()){
			case 0: throw new Sussy("No objects defined");
			case 1: return objects.get(0);
			default: throw new Sussy("Expected one object");
		}
	}

	/**
	 * Creates a shallow copy of this object. Objects are re-added to the new root without copying.
	 *
	 * @return A shallow copy of this object
	 */
	public AmongRoot copy(){
		return new AmongRoot(this);
	}

	/**
	 * Returns a string representation of each object in this root, formatted in compact style. Re-parsing the result
	 * will produce identical object to this.
	 *
	 * @return String representation of this root
	 */
	@Override public String toString(){
		if(objects.isEmpty()) return "";
		StringBuilder stb = new StringBuilder();
		for(Among object : objects){
			if(object.isPrimitive()) AmongUs.primitiveToString(stb, object.asPrimitive().getValue());
			else stb.append(object);
		}
		return stb.toString();
	}

	/**
	 * Returns a string representation of each object in this root, formatted in human-readable form. Re-parsing the
	 * result will produce identical object to this.
	 *
	 * @param indents Number of indentations
	 * @param option  Option to use
	 * @return String representation of this root
	 */
	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		boolean first = true;
		for(Among object : objects){
			if(first) first = false;
			else stb.append('\n');
			if(object.isPrimitive())
				AmongUs.primitiveToPrettyString(stb, object.asPrimitive().getValue(), indents, option);
			else stb.append(object.toPrettyString(indents, option));
		}
		return stb.toString();
	}
}

package ttmp.among.obj;

import ttmp.among.util.AmongUs;
import ttmp.among.util.OperatorType;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Operator definitions. Snippet below shows various operator definitions written in Among.
 * <pre>
 * def ... as postfix operator
 * def is as binary keyword
 * </pre>
 */
public final class AmongOperatorDef{
	private static final DecimalFormat FORMAT = new DecimalFormat("0.#####");

	private final String name;
	private final boolean isKeyword;
	private final OperatorType type;
	private final double priority;

	/**
	 * Creates new operator definition.
	 *
	 * @param name      Name of the operator
	 * @param isKeyword Whether this defines keyword or operator
	 * @param type      Type of the operator
	 * @throws NullPointerException if {@code name == null} or {@code type == null}
	 * @see AmongOperatorDef#AmongOperatorDef(String, boolean, OperatorType, double)
	 */
	public AmongOperatorDef(String name, boolean isKeyword, OperatorType type){
		this(name, isKeyword, type, Double.NaN);
	}
	/**
	 * Creates new operator definition.
	 *
	 * @param name      Name of the operator
	 * @param isKeyword Whether this defines keyword or operator
	 * @param type      Type of the operator
	 * @param priority  Priority of the operator; if {@code NaN} is supplied, it will be replaced with default priority.
	 * @throws NullPointerException if {@code name == null} or {@code type == null}
	 */
	public AmongOperatorDef(String name, boolean isKeyword, OperatorType type, double priority){
		this.name = Objects.requireNonNull(name);
		this.isKeyword = isKeyword;
		this.type = Objects.requireNonNull(type);
		this.priority = Double.isNaN(priority) ? type.defaultPriority() : priority;
	}

	public String name(){
		return name;
	}
	public boolean isKeyword(){
		return isKeyword;
	}
	public OperatorType type(){
		return type;
	}
	public double priority(){
		return priority;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		AmongOperatorDef that = (AmongOperatorDef)o;
		return isKeyword()==that.isKeyword()&&
				Double.compare(that.priority, priority)==0&&
				name.equals(that.name)&&
				type==that.type;
	}
	@Override public int hashCode(){
		return Objects.hash(name, isKeyword(), type, priority);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder().append("def ");
		AmongUs.nameToString(stb, this.name);
		stb.append(" as ")
				.append(type==OperatorType.BINARY ? "binary" : type==OperatorType.POSTFIX ? "postfix" : "prefix")
				.append(isKeyword ? " keyword" : " operator");
		if(Double.compare(priority, type.defaultPriority())!=0)
			stb.append(" : ").append(FORMAT.format(priority));
		return stb.toString();
	}
}

package ttmp.among.obj;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.Sussy;
import ttmp.among.util.AmongUs;
import ttmp.among.util.MacroSignature;
import ttmp.among.util.MacroType;
import ttmp.among.util.OperatorRegistry;
import ttmp.among.util.OperatorType;
import ttmp.among.util.ToPrettyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Root object represents a whole file, including macro definitions, operator definitions, and objects.
 */
public final class AmongRoot implements ToPrettyString{
	/**
	 * Creates new empty root - no macros, no operators, nothing.
	 *
	 * @return New empty root
	 */
	public static AmongRoot empty(){
		return new AmongRoot();
	}

	/**
	 * Creates new root with basic operators.
	 *
	 * @return New root with basic operators
	 * @see <a href="https://youtu.be/J2C9oiP5j7Q">ok this link is supposed to be a link to the documentation about
	 * "basic operators" but i kinda dont have one now so i will just put a rickroll here if you dont mind yea just poke
	 * me if i have that doc up somewhere but didnt update this link ok thx</a>
	 */
	public static AmongRoot withDefaultOperators(){
		AmongRoot root = new AmongRoot();
		OperatorRegistry o = root.operators();
		o.addOperator("=", OperatorType.BINARY, 0);
		o.addOperator("||", OperatorType.BINARY, 1);
		o.addOperator("&&", OperatorType.BINARY, 2);
		o.addOperator("==", OperatorType.BINARY, 3);
		o.addOperator("!=", OperatorType.BINARY, 3);
		o.addOperator(">", OperatorType.BINARY, 4);
		o.addOperator("<", OperatorType.BINARY, 4);
		o.addOperator(">=", OperatorType.BINARY, 4);
		o.addOperator("<=", OperatorType.BINARY, 4);
		o.addOperator("~", OperatorType.BINARY, 5);
		o.addOperator("|", OperatorType.BINARY, 6);
		o.addOperator("&", OperatorType.BINARY, 7);
		o.addOperator("+", OperatorType.BINARY, 8);
		o.addOperator("-", OperatorType.BINARY, 8);
		o.addOperator("*", OperatorType.BINARY, 9);
		o.addOperator("/", OperatorType.BINARY, 9);
		o.addOperator("^", OperatorType.BINARY, 10);
		o.addOperator("**", OperatorType.BINARY, 10);
		o.addOperator("!", OperatorType.PREFIX, 13);
		o.addOperator("-", OperatorType.PREFIX, 13);
		o.addOperator("+", OperatorType.PREFIX, 13);
		o.addOperator("~", OperatorType.PREFIX, 13);
		o.addOperator(".", OperatorType.BINARY, 14);
		return root;
	}

	private final Map<MacroSignature, AmongMacroDef> macros;
	private final OperatorRegistry operators;
	private final List<Among> objects;

	private AmongRoot(){
		this.macros = new HashMap<>();
		this.operators = new OperatorRegistry();
		this.objects = new ArrayList<>();
	}
	private AmongRoot(AmongRoot copyFrom){
		this.macros = new HashMap<>(copyFrom.macros);
		this.operators = new OperatorRegistry(copyFrom.operators);
		this.objects = new ArrayList<>(copyFrom.objects);
	}

	public Map<MacroSignature, AmongMacroDef> macros(){
		return Collections.unmodifiableMap(macros);
	}
	@Nullable public AmongMacroDef searchMacro(String name, MacroType type){
		return searchMacro(new MacroSignature(name, type));
	}
	@Nullable public AmongMacroDef searchMacro(MacroSignature signature){
		return macros.get(signature);
	}
	public void addMacro(AmongMacroDef def){
		macros.put(def.signature(), def);
	}
	@Nullable public AmongMacroDef removeMacro(String name, MacroType type){
		return removeMacro(new MacroSignature(name, type));
	}
	@Nullable public AmongMacroDef removeMacro(MacroSignature signature){
		return macros.remove(signature);
	}
	public void clearMacros(){
		macros.clear();
	}

	public OperatorRegistry operators(){
		return operators;
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
	public void clearObjects(){
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
	 * Creates a shallow copy of this object.
	 * <ul>
	 *     <li>Macro instances are recycled.</li>
	 *     <li>Operator instances are recycled.</li>
	 *     <li>Objects are re-added to the new root without copying.</li>
	 * </ul>
	 *
	 * @return A shallow copy of this object
	 */
	public AmongRoot copy(){
		return new AmongRoot(this);
	}

	/**
	 * Returns a string representation of each objects in this root.
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
	 * Returns a string representation of each object in this root.
	 *
	 * @param indents Number of indentations
	 * @param indent  Indentation to use
	 * @return String representation of the objects
	 */
	@Override public String toPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		objectsToPrettyString(stb, indents, indent);
		return stb.toString();
	}

	/**
	 * Returns a string representation of each object and definition in this root. Both macro definitions and operator
	 * definitions are included.<br>
	 * Note that it might produce different result if re-parsed due to the presence of macro.
	 *
	 * @return String representation of the objects and the definitions
	 */
	public String objectsAndDefinitionsToPrettyString(){
		return objectsAndDefinitionsToPrettyString(0, ToPrettyString.DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of each object and definition in this root. Both macro definitions and operator
	 * definitions are included.<br>
	 * Note that it might produce different result if re-parsed due to the presence of macro.
	 *
	 * @param indents Number of indentations
	 * @return String representation of the objects and the definitions
	 */
	public String objectsAndDefinitionsToPrettyString(int indents){
		return objectsAndDefinitionsToPrettyString(indents, ToPrettyString.DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of each object and definition in this root. Both macro definitions and operator
	 * definitions are included.<br>
	 * Note that it might produce different result if re-parsed due to the presence of macro.
	 *
	 * @param indents Number of indentations
	 * @param indent  Indentation to use
	 * @return String representation of the objects and the definitions
	 */
	public String objectsAndDefinitionsToPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		definitionsToPrettyString(stb, indents, indent);
		if(!objects.isEmpty()){
			if(stb.length()>0) stb.append("\n\n");
			objectsToPrettyString(stb, indents, indent);
		}
		return stb.toString();
	}
	/**
	 * Returns a string representation of each definition in this root. Both macro definitions and operator definitions
	 * are included.<br>
	 * Note that if default operators are registered, they will be included in the result.
	 *
	 * @return String representation of the definitions
	 */
	public String definitionsToPrettyString(){
		return definitionsToPrettyString(0, ToPrettyString.DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of each definition in this root. Both macro definitions and operator definitions
	 * are included.<br>
	 * Note that if default operators are registered, they will be included in the result.
	 *
	 * @param indents Number of indentations
	 * @return String representation of the definitions
	 */
	public String definitionsToPrettyString(int indents){
		return definitionsToPrettyString(indents, ToPrettyString.DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of each definition in this root. Both macro definitions and operator definitions
	 * are included.<br>
	 * Note that if default operators are registered, they will be included in the result.
	 *
	 * @param indents Number of indentations
	 * @param indent  Indentation to use
	 * @return String representation of the definitions
	 */
	public String definitionsToPrettyString(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		definitionsToPrettyString(stb, indents, indent);
		return stb.toString();
	}

	private void objectsToPrettyString(StringBuilder stb, int indents, String indent){
		boolean first = true;
		for(Among object : objects){
			if(first) first = false;
			else stb.append('\n');
			if(object.isPrimitive())
				AmongUs.primitiveToPrettyString(stb, object.asPrimitive().getValue(), indents, indent);
			else stb.append(object.toPrettyString(indents, indent));
		}
	}
	private void definitionsToPrettyString(StringBuilder stb, int indents, String indent){
		List<String> lines = new ArrayList<>();
		for(AmongMacroDef def : macros.values())
			lines.add(def.toPrettyString(indents, indent));
		operators.forEachOperatorOrKeyword(def -> lines.add(def.toString()));
		boolean first = true;
		for(String line : lines){
			if(first) first = false;
			else stb.append('\n');
			stb.append(line);
		}
	}
}

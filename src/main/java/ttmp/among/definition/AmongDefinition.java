package ttmp.among.definition;

import ttmp.among.format.PrettifyOption;
import ttmp.among.format.ToPrettyString;

/**
 * Represents a definition side of the source, i.e. macro and operator definitions.
 */
public final class AmongDefinition implements ToPrettyString{
	private final MacroRegistry macros;
	private final OperatorRegistry operators;

	/**
	 * Create new empty definition - no macros, no operators, nothing.
	 */
	public AmongDefinition(){
		this.macros = new MacroRegistry();
		this.operators = new OperatorRegistry();
	}
	private AmongDefinition(AmongDefinition copyFrom){
		this.macros = new MacroRegistry(copyFrom.macros);
		this.operators = new OperatorRegistry(copyFrom.operators);
	}

	public boolean isEmpty(){
		return macros.isEmpty()&&operators.isEmpty();
	}
	public void clear(){
		macros.clear();
		operators.clear();
	}

	public MacroRegistry macros(){
		return macros;
	}
	public OperatorRegistry operators(){
		return operators;
	}

	/**
	 * Create a shallow copy of this object. Macro and operator instances are recycled.
	 *
	 * @return A shallow copy of this object
	 */
	public AmongDefinition copy(){
		return new AmongDefinition(this);
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder();
		macros.macros().forEach(macro -> {
			if(stb.length()>0) stb.append(',');
			stb.append(macro.toString());
		});
		operators.allOperators().forEach(def -> {
			if(stb.length()>0) stb.append(',');
			stb.append(def.toString());
		});
		return stb.toString();
	}
	@Override public String toPrettyString(int indents, PrettifyOption option){
		StringBuilder stb = new StringBuilder();
		macros.macros().forEach(macro -> {
			if(stb.length()>0) stb.append('\n');
			stb.append(macro.toPrettyString(indents, option));
		});
		operators.allOperators().forEach(def -> {
			if(stb.length()>0) stb.append('\n');
			stb.append(def.toPrettyString(indents, option));
		});
		return stb.toString();
	}
}

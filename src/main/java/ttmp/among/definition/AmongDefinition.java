package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.util.PrettyFormatOption;
import ttmp.among.util.ToPrettyString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a definition side of the source, i.e. macro and operator definitions.
 */
public final class AmongDefinition implements ToPrettyString{
	private final Map<MacroSignature, MacroDefinition> macros;
	private final OperatorRegistry operators;

	/**
	 * Create new empty definition - no macros, no operators, nothing.
	 */
	public AmongDefinition(){
		this.macros = new HashMap<>();
		this.operators = new OperatorRegistry();
	}
	private AmongDefinition(AmongDefinition copyFrom){
		this.macros = new HashMap<>(copyFrom.macros);
		this.operators = new OperatorRegistry(copyFrom.operators);
	}

	public boolean isEmpty(){
		return macros.isEmpty()&&operators.isEmpty();
	}
	public void clear(){
		macros.clear();
		operators.clear();
	}

	public Map<MacroSignature, MacroDefinition> macros(){
		return Collections.unmodifiableMap(macros);
	}
	@Nullable public MacroDefinition searchMacro(String name, MacroType type){
		return searchMacro(new MacroSignature(name, type));
	}
	@Nullable public MacroDefinition searchMacro(MacroSignature signature){
		return macros.get(signature);
	}
	@Nullable public MacroDefinition addMacro(MacroDefinition def){
		return macros.put(def.signature(), def);
	}
	@Nullable public MacroDefinition removeMacro(String name, MacroType type){
		return removeMacro(new MacroSignature(name, type));
	}
	@Nullable public MacroDefinition removeMacro(MacroSignature signature){
		return macros.remove(signature);
	}
	public void clearMacros(){
		macros.clear();
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
		for(MacroDefinition def : macros.values()){
			if(stb.length()>0) stb.append('\n');
			stb.append(def.toString());
		}
		operators.forEachOperatorAndKeyword(def -> {
			if(stb.length()>0) stb.append('\n');
			stb.append(def.toString());
		});
		return stb.toString();
	}
	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		for(MacroDefinition def : macros.values()){
			if(stb.length()>0) stb.append('\n');
			stb.append(def.toPrettyString(indents, option));
		}
		operators.forEachOperatorAndKeyword(def -> {
			if(stb.length()>0) stb.append('\n');
			stb.append(def.toString());
		});
		return stb.toString();
	}
}

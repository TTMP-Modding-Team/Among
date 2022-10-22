package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for {@link MacroDefinition}.
 */
public class MacroBuilder{
	@Nullable private String name;
	@Nullable private MacroType type;
	private final List<MacroParameter> parameters = new ArrayList<>();

	public MacroBuilder signature(String name, MacroType type){
		this.name = name;
		this.type = type;
		return this;
	}

	public MacroBuilder param(String paramName){
		return param(paramName, null);
	}
	public MacroBuilder param(String paramName, @Nullable Among defaultValue){
		return param(paramName, defaultValue, TypeInference.ANY);
	}
	public MacroBuilder param(String paramName, byte typeInference){
		return param(paramName, null, typeInference);
	}
	public MacroBuilder param(String paramName, @Nullable Among defaultValue, byte typeInference){
		this.parameters.add(new MacroParameter(paramName, defaultValue, typeInference));
		return this;
	}

	/**
	 * Build a replacement-based macro - the one you define with Among script.
	 *
	 * @return New macro definition
	 * @throws Sussy If one of the arguments are unspecified or invalid
	 */
	public MacroDefinition build(Among template, MacroReplacement... replacements){
		if(name==null) throw new Sussy("Name not defined");
		if(type==null) throw new Sussy("Type not defined");
		return new MacroDefinition(name, type, MacroParameterList.of(parameters), template, Arrays.asList(replacements));
	}
}

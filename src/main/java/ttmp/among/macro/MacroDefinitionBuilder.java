package ttmp.among.macro;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link MacroDefinition}.
 */
public class MacroDefinitionBuilder{
	@Nullable private String name;
	@Nullable private MacroType type;
	private final List<MacroParameter> parameters = new ArrayList<>();
	@Nullable private Among template;

	public MacroDefinitionBuilder signature(String name, MacroType type){
		this.name = name;
		this.type = type;
		return this;
	}

	public MacroDefinitionBuilder param(String paramName){
		return param(paramName, null);
	}
	public MacroDefinitionBuilder param(String paramName, @Nullable Among defaultValue){
		this.parameters.add(new MacroParameter(paramName, defaultValue));
		return this;
	}

	public MacroDefinitionBuilder template(Among template){
		this.template = template;
		return this;
	}

	/**
	 * Builds macro definition.
	 *
	 * @return New macro definition
	 * @throws Sussy If one of the arguments are unspecified or invalid
	 */
	public MacroDefinition build(){
		if(name==null) throw new Sussy("Name not defined");
		if(type==null) throw new Sussy("Type not defined");
		if(template==null) throw new Sussy("Object not defined");
		return new MacroDefinition(name, type, MacroParameterList.of(parameters), template);
	}
}

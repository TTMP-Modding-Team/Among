package ttmp.among.util;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongMacroDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link AmongMacroDef}.
 */
public class AmongMacroDefBuilder{
	@Nullable private String name;
	@Nullable private MacroType type;
	private final List<MacroParameter> parameters = new ArrayList<>();
	@Nullable private Among object;

	public AmongMacroDefBuilder signature(String name, MacroType type){
		this.name = name;
		this.type = type;
		return this;
	}

	public AmongMacroDefBuilder param(String paramName){
		return param(paramName, null);
	}
	public AmongMacroDefBuilder param(String paramName, @Nullable Among defaultValue){
		this.parameters.add(new MacroParameter(paramName, defaultValue));
		return this;
	}

	public AmongMacroDefBuilder object(Among object){
		this.object = object;
		return this;
	}

	/**
	 * Builds macro definition.
	 *
	 * @return New macro definition
	 * @throws Sussy If one of the arguments are unspecified or invalid
	 */
	public AmongMacroDef build(){
		if(name==null) throw new Sussy("Name not defined");
		if(type==null) throw new Sussy("Type not defined");
		if(object==null) throw new Sussy("Object not defined");
		return new AmongMacroDef(name, type, MacroParameterList.of(parameters), object);
	}
}

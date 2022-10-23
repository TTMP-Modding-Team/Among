package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.exception.Sussy;
import ttmp.among.obj.Among;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Builder for {@link MacroDefinition}.
 */
public class MacroBuilder{
	private final String name;
	private final MacroType type;
	private final List<MacroParameter> parameters = new ArrayList<>();
	@Nullable private List<Byte> typeInferences;

	public MacroBuilder(String name, MacroType type){
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
	}

	public MacroBuilder param(String paramName){
		return param(paramName, null);
	}
	public MacroBuilder param(String paramName, @Nullable Among defaultValue){
		return param(paramName, defaultValue, TypeFlags.ANY);
	}
	public MacroBuilder param(String paramName, byte typeInference){
		return param(paramName, null, typeInference);
	}
	public MacroBuilder param(String paramName, @Nullable Among defaultValue, byte typeInference){
		typeInference = TypeFlags.normalize(typeInference);
		if(typeInference==0) throw new Sussy("Type inference of parameter '"+paramName+"' has no valid input");
		if(typeInference!=TypeFlags.ANY) createTypeInference();
		parameters.add(new MacroParameter(paramName, defaultValue));
		if(typeInferences!=null) typeInferences.add(typeInference);
		return this;
	}

	public MacroBuilder inferSelfType(byte typeInference){
		if(!type.isFunctionMacro()) throw new Sussy("Cannot infer self type of non-function macros");
		typeInference = TypeFlags.normalize(typeInference);
		if(typeInference==0) throw new Sussy("Type inference of self has no valid input");
		createTypeInference();
		Objects.requireNonNull(typeInferences).set(0, typeInference);
		return this;
	}

	private void createTypeInference(){
		if(typeInferences==null){
			typeInferences = new ArrayList<>();
			for(int i = type.isFunctionMacro() ? -1 : 0; i<this.parameters.size(); i++)
				this.typeInferences.add(TypeFlags.ANY);
		}
	}

	private byte @Nullable [] buildTypeInference(){
		if(this.typeInferences==null) return null;
		byte[] typeInferences = new byte[this.typeInferences.size()];
		for(int i = 0; i<this.typeInferences.size(); i++)
			typeInferences[i] = this.typeInferences.get(i);
		return typeInferences;
	}

	/**
	 * Build a replacement-based macro - the one you define with Among script.
	 *
	 * @return New macro definition
	 * @throws Sussy If one of the arguments are unspecified or invalid
	 */
	public MacroDefinition build(Among template, MacroReplacement... replacements){
		return new MacroDefinition(new MacroSignature(name, type), MacroParameterList.of(parameters),
				template, Arrays.asList(replacements), buildTypeInference());
	}
}

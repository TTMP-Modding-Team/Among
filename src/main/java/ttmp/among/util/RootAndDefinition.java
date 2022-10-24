package ttmp.among.util;

import ttmp.among.definition.AmongDefinition;
import ttmp.among.format.AmongUs;
import ttmp.among.format.PrettifyContext;
import ttmp.among.format.PrettifyOption;
import ttmp.among.format.ToPrettyString;
import ttmp.among.obj.AmongRoot;

/**
 * A pair of {@link AmongRoot} and {@link AmongDefinition}.
 */
public final class RootAndDefinition extends ToPrettyString.Base{
	private final AmongRoot root;
	private final AmongDefinition definition;

	public RootAndDefinition(){
		this(new AmongRoot(), new AmongDefinition());
	}
	public RootAndDefinition(AmongRoot root){
		this(root, new AmongDefinition());
	}
	public RootAndDefinition(AmongDefinition definition){
		this(new AmongRoot(), definition);
	}
	public RootAndDefinition(AmongRoot root, AmongDefinition definition){
		this.root = root;
		this.definition = definition;
	}

	public AmongRoot root(){
		return root;
	}
	public AmongDefinition definition(){
		return definition;
	}

	@Override public void toString(StringBuilder stb, PrettifyOption option, PrettifyContext context){
		if(definition.isEmpty()){
			if(!root.isEmpty()) root.toString(stb, option, PrettifyContext.NONE);
		}else{
			definition.toString(stb, option, PrettifyContext.NONE);
			if(!root.isEmpty()) root.toString(stb.append(','), option, PrettifyContext.NONE);
		}
	}

	@Override public void toPrettyString(StringBuilder stb, int indents, PrettifyOption option, PrettifyContext context){
		if(definition.isEmpty()){
			if(!root.isEmpty()) root.toPrettyString(stb, indents, option, PrettifyContext.NONE);
		}else{
			definition.toPrettyString(stb, indents, option, PrettifyContext.NONE);
			if(!root.isEmpty()){
				AmongUs.newlineAndIndent(stb, indents, option);
				root.toPrettyString(stb, indents, option, PrettifyContext.NONE);
			}
		}
	}
}

package ttmp.among.util;

import ttmp.among.definition.AmongDefinition;
import ttmp.among.obj.AmongRoot;

/**
 * A pair of {@link AmongRoot} and {@link AmongDefinition}.
 */
public final class RootAndDefinition implements ToPrettyString{
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

	@Override public String toString(){
		return definition.isEmpty() ?
				root.isEmpty() ? "" : root.toString() :
				root.isEmpty() ? definition.toString() :
						definition+","+root;
	}

	@Override public String toPrettyString(int indents, PrettyFormatOption option){
		return definition.isEmpty() ?
				root.isEmpty() ? "" : root.toPrettyString(indents, option) :
				root.isEmpty() ? definition.toPrettyString(indents, option) :
						definition.toPrettyString(indents, option)+
								AmongUs.newlineAndIndent(indents, option)+
								root.toPrettyString(indents, option);
	}
}

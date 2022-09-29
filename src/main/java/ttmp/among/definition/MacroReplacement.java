package ttmp.among.definition;

import ttmp.among.obj.Among;
import ttmp.among.util.NodePath;

/**
 * Object representing one replacement operation.
 *
 * @see MacroDefinition
 */
public final class MacroReplacement{
	private final NodePath path;
	private final int param;
	private final Target target;

	public MacroReplacement(NodePath path, int param, Target target){
		this.path = path;
		this.param = param;
		this.target = target;
	}

	public NodePath path(){
		return path;
	}
	public int param(){
		return param;
	}
	public Target target(){
		return target;
	}

	/**
	 * Applies the replacement.
	 *
	 * @param args   Argument for the replacement
	 * @param target Target
	 * @return Object after replacement; it will just return {@code target} most of the time
	 */
	public Among apply(Among[] args, Among target){
		switch(this.target){
			case VALUE:
				if(path.isEmpty()) return args[this.param];
				path.resolveAndSet(target, args[this.param]);
				return target;
			case NAMEABLE_NAME:{
				Among resolved = path.resolveAndGet(target);
				if(resolved!=null&&resolved.isNamed())
					resolved.asNamed()
							.setName(args[this.param].asPrimitive().getValue());
				return target;
			}
			default: throw new IllegalStateException("Unreachable");
		}
	}

	public enum Target{
		/**
		 * Replace the element with arg.
		 */
		VALUE,
		/**
		 * Rename the object to arg.
		 */
		NAMEABLE_NAME
	}
}

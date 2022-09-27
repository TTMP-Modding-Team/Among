package ttmp.among.util;

import ttmp.among.obj.MacroDefinition;

/**
 * Types of macro. Snippet below shows macros with each type written in Among.
 * <pre>
 * macro macro : "Hello!"  // CONST
 * macro macro{} : "Hello!"  // OBJECT
 * macro macro[] : "Hello!"  // LIST
 * macro macro() : "Hello!"  // OPERATION
 * </pre>
 */
public enum MacroType{
	/**
	 * Macro defined without any parameter blocks. Not to be confused with {@link MacroDefinition#isConstant()}.
	 */
	CONST,
	/**
	 * Macro defined with object ('{}') as parameter blocks.
	 */
	OBJECT,
	/**
	 * Macro defined with list ('[]') as parameter blocks.
	 */
	LIST,
	/**
	 * Macro defined with operation ('()') as parameter blocks.
	 */
	OPERATION
}

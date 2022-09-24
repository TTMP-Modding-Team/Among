package ttmp.among.util;

import ttmp.among.obj.AmongMacroDef;

/**
 * Types of macro. Snippet below shows macros with each type written in Among.
 * <pre>
 * def macro : "Hello!"  // CONST
 * def macro{} : "Hello!"  // OBJECT
 * def macro[] : "Hello!"  // LIST
 * def macro() : "Hello!"  // OPERATION
 * </pre>
 */
public enum MacroType{
	/**
	 * Macro defined without any parameter blocks. Not to be confused with {@link AmongMacroDef#isConstant()}.
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

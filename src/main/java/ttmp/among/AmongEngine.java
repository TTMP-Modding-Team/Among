package ttmp.among;

import org.jetbrains.annotations.Nullable;
import ttmp.among.compile.AmongParser;
import ttmp.among.compile.CompileResult;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongMacroDef;
import ttmp.among.obj.AmongOperatorDef;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.OperatorRegistry;
import ttmp.among.util.Source;

/**
 * Absolutely Mental Object Notation. (G is silent (that's how acronyms work right?))<br>
 */
public final class AmongEngine{
	/**
	 * If enabled, any single-element, unnamed operations will be 'collapsed'; that is, being replaced with its child
	 * element. Snipped below demonstrates compilation result with and without the option.
	 * <pre>
	 * // Sample code
	 * ((x-y)/2)
	 *
	 * [ / [[- [x, y]], 2]]  // Raw compilation result of the code above, notice the presence of single-element lists
	 * / [- [x, y], 2]  // Compilation result with the option turned on
	 *
	 *
	 * // Multi-element operations, lists, empty operations and named operations are not affected
	 * ((1, 2, 3) + [list] + () + fib(3))
	 *
	 * // Compilation result of the code above
	 * + [
	 *   + [
	 *     + [
	 *       [
	 *         1
	 *         2
	 *         3
	 *       ]
	 *       [
	 *         list
	 *       ]
	 *     ]
	 *     []
	 *   ]
	 *   fib [
	 *     3
	 *   ]
	 * ]
	 * </pre>
	 */
	public boolean collapseUnaryOperation = true;

	/**
	 * If enabled, duplicate properties (Multiple properties with identical key) will not produce compilation error.
	 * Instead, any duplicate properties following the first will be ignored, and reported as warning.
	 */
	public boolean allowDuplicateObjectProperty = false;

	/**
	 * If enabled, invalid registration of operators will not produce compilation error.
	 * Instead, it will only produce warning.
	 *
	 * @see OperatorRegistry#add(AmongOperatorDef)
	 */
	public boolean allowInvalidOperatorRegistration = false;

	/**
	 * If enabled, constant macro will return deep copied object, rather than template itself. Disabling this option
	 * makes the same instance to be shared between each macro usage along with the macro itself, avoiding potentially
	 * expensive deep copy operation and saving memory. But, modifying the compilation result poses a risk of undefined
	 * behavior, due to the possibility of instance being shared across multiple places.<br>
	 * It is advised to disable this option only if the result is not expected to be modified afterwards.
	 *
	 * @see AmongMacroDef#apply(Among, boolean)
	 */
	public boolean copyMacroConstant = true;

	/**
	 * Specifies error handling behavior for invalid unicode escape. It specifically refers to invalid trailing value
	 * for {@code \u005Cu} and {@code \u005CU} notation, which should be a hexadecimal with 4 characters (for {@code
	 * \u005Cu}) and 8 characters (for {@code \u005CU}). This behavior is also used when the codepoint supplied by
	 * {@code \u005CU} notation is outside the unicode definition (larger than {@code 10FFFF}).<br>
	 * Regardless of the setting, the compiler will process the input as the {@code \u005Cu} notation never existed;
	 * for example, invalid input {@code '\u005Cuabcd'} will produce {@code 'uabcd'}.<br>
	 * If any other value is provided, {@link ErrorHandling#ERROR ERROR} will be used.
	 *
	 * @see ErrorHandling
	 */
	public int invalidUnicodeHandling = ErrorHandling.ERROR;

	/**
	 * Reads and parses the source into newly created {@link AmongRoot}.
	 *
	 * @param source Source to be read from
	 * @return Result with new root containing objects parsed from {@code source}
	 * @see AmongEngine#read(Source, AmongRoot)
	 */
	public CompileResult read(Source source){
		return read(source, null);
	}

	/**
	 * Reads and parses the source into given {@link AmongRoot}, or new one if {@code null} is supplied.
	 *
	 * @param source Source to be read from
	 * @param root   Root to be used
	 * @return Result with {@code root} (or new root if it was {@code null}) containing objects parsed from {@code source}
	 */
	public CompileResult read(Source source, @Nullable AmongRoot root){
		return new AmongParser(source, this,
				root==null ? AmongRoot.withDefaultOperators() : root)
				.parse();
	}

	/**
	 * Specifies error handling mode for various invalid inputs.
	 * <table>
	 *   <tr>
	 *     <td>Value</td><td>Behavior</td>
	 *   </tr>
	 *   <tr>
	 *     <td>{@link ErrorHandling#ERROR}</td> <td>All invalid inputs will be reported as compilation errors.</td>
	 *   </tr>
	 *   <tr>
	 *     <td>{@link ErrorHandling#WARN}</td> <td>All invalid inputs will be reported as warnings.</td>
	 *   </tr>
	 *   <tr>
	 *     <td>{@link ErrorHandling#IGNORE}</td> <td>All invalid inputs will be ignored.</td>
	 *   </tr>
	 * </table>
	 * <br>
	 * This value does not modify the way invalid inputs are being read; it will produce identical results regardless
	 * of the mode used.<br>
	 * If any other value is provided, the default value will be used; see the individual use cases for details.
	 */
	public interface ErrorHandling{
		/**
		 * All invalid inputs will be reported as compilation errors.
		 */
		int ERROR = 0;
		/**
		 * All invalid inputs will be reported as warnings.
		 */
		int WARN = 1;
		/**
		 * All invalid inputs will be ignored.
		 */
		int IGNORE = 2;
	}
}

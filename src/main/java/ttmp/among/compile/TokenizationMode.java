package ttmp.among.compile;

/**
 * Mode for interpreting literal expressions.<br>
 * Literals can be read as separate expressions or one expression, depending on the context; snippet below shows the
 * example of such behavior.
 * <pre>
 * macro name=name(name=name): $name { name=name: ( name=name ) }
 * </pre>
 * <ul>
 *     <li>The first {@code 'name=name'} in definition name is read with {@link TokenizationMode#NAME},
 *     which produces literal of {@code 'name=name'}.</li>
 *     <li>The front half of the second {@code 'name=name'} in parameter is read with {@link TokenizationMode#PARAM},
 *     which produces literal of {@code 'name'}. The literal behind {@code =} is read with {@link TokenizationMode#VALUE},
 *     which produces literal of {@code 'name'}.</li>
 *     <li>The third {@code 'name=name'} is read with {@link TokenizationMode#KEY}, which produces {@code 'name=name'}.</li>
 *     <li>And, the last {@code 'name=name'} is read with {@link TokenizationMode#OPERATION},
 *     which separates {@code =} as binary operator and returns literal {@code 'name'}, operator {@code '='},
 *     and literal {@code 'name'} consecutively.</li>
 * </ul>
 *
 * @see AmongTokenizer#next(boolean, TokenizationMode)
 */
public enum TokenizationMode{
	/**
	 * Any encountered literals will produce ERROR tokens.
	 */
	UNEXPECTED,
	/**
	 * Any encountered literals will be parsed as names; see grammar for specific definition.
	 */
	NAME,
	/**
	 * Any encountered literals will be parsed as names. Literals with no backslashes will be parsed as words.
	 */
	WORD,
	/**
	 * Any encountered literals will be parsed as keys; see grammar for specific definition.<br>
	 * '(', ')', '[' and ']' tokens are not parsed in this mode, as they are part of the key.
	 */
	KEY,
	/**
	 * Any encountered literals will be parsed as parameter names; see grammar for specific definition.<br>
	 * Note that '=' token can only be parsed with this mode.
	 */
	PARAM,
	/**
	 * Any encountered literals will be parsed as values; see grammar for specific definition.
	 */
	VALUE,
	/**
	 * Makes the tokenizer search for operator/keyword definition while parsing literals.
	 * Operator/keyword tokens will only be parsed with this mod.
	 */
	OPERATION
}

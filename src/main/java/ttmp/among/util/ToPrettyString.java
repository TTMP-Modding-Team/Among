package ttmp.among.util;

public interface ToPrettyString{
	String DEFAULT_INDENT = "  ";

	/**
	 * Returns a string representation of this object, formatted in human-readable form.
	 *
	 * @return String representation of this object
	 * @see ToPrettyString#toPrettyString(int, String)
	 */
	default String toPrettyString(){
		return toPrettyString(0, DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of this object, formatted in human-readable form.
	 *
	 * @param indents Number of indentations
	 * @return String representation of this object
	 * @see ToPrettyString#toPrettyString(int, String)
	 */
	default String toPrettyString(int indents){
		return toPrettyString(indents, DEFAULT_INDENT);
	}
	/**
	 * Returns a string representation of this object, formatted in human-readable form.
	 *
	 * @param indents Number of indentations
	 * @param indent  Indentation to use
	 * @return String representation of this object
	 */
	String toPrettyString(int indents, String indent);
}

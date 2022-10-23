package ttmp.among.format;

import java.util.Objects;

/**
 * Formatting option used in {@link ToPrettyString}.
 */
public final class PrettifyOption{
	public static final PrettifyOption DEFAULT = new PrettifyOption("  ", 2, 3);

	/**
	 * Indentation to be used.
	 */
	public final String indent;
	/**
	 * Inclusive maximum size for objects to be formatted 'compact' - the object will be written in one line, without
	 * excessive whitespaces or line breaks. Compact formatting does not affect child elements.
	 */
	public final int compactObjectSize;
	/**
	 * Inclusive maximum size for lists to be formatted 'compact' - the list will be written in one line, without
	 * excessive whitespaces or line breaks. Compact formatting does not affect child elements.
	 */
	public final int compactListSize;

	public PrettifyOption(String indent, int compactObjectSize, int compactListSize){
		this.indent = Objects.requireNonNull(indent);
		this.compactObjectSize = compactObjectSize;
		this.compactListSize = compactListSize;
	}

	@Override public String toString(){
		return "PrettifyOption{"+
				"indent='"+indent+'\''+
				", compactObjectSize="+compactObjectSize+
				", compactArraySize="+compactListSize+
				'}';
	}
}

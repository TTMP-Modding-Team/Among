package ttmp.among.util;

import java.util.Objects;

/**
 * Formatting option used in {@link ToPrettyString}.
 */
public final class PrettyFormatOption{
	public static final PrettyFormatOption DEFAULT = new PrettyFormatOption("  ", 2, 3);

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

	public PrettyFormatOption(String indent, int compactObjectSize, int compactListSize){
		this.indent = Objects.requireNonNull(indent);
		this.compactObjectSize = compactObjectSize;
		this.compactListSize = compactListSize;
	}

	@Override public String toString(){
		return "PrettyFormatOption{"+
				"indent='"+indent+'\''+
				", compactObjectSize="+compactObjectSize+
				", compactArraySize="+compactListSize+
				'}';
	}
}

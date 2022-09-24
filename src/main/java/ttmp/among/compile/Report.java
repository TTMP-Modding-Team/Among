package ttmp.among.compile;

import org.jetbrains.annotations.Nullable;
import ttmp.among.util.LnCol;
import ttmp.among.util.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Message with type(info, warn or error) and position in source (optional), and exception (optional).
 */
public class Report{
	private final ReportType type;
	private final String message;
	private final int sourcePosition;
	@Nullable private final Throwable exception;
	private final List<String> hints;

	public Report(ReportType type, String message, int sourcePosition, @Nullable Throwable exception, String... hints){
		this.type = type;
		this.message = message;
		this.sourcePosition = sourcePosition;
		this.exception = exception;
		this.hints = new ArrayList<>();
		Collections.addAll(this.hints, hints);
	}

	public ReportType type(){
		return type;
	}
	public String message(){
		return message;
	}
	public int sourcePosition(){
		return sourcePosition;
	}
	public boolean hasSourcePosition(){
		return sourcePosition>=0;
	}
	@Nullable public Throwable exception(){
		return exception;
	}
	public List<String> hints(){
		return Collections.unmodifiableList(hints);
	}

	@Nullable public LnCol getLineColumn(Source source){
		return hasSourcePosition() ? source.getLnCol(sourcePosition) : null;
	}
	@Nullable public String getLineSnippet(Source source){
		if(!hasSourcePosition()) return null;
		StringBuilder stb = new StringBuilder();
		// TODO gotta go sleep
		stb.append("I NEED CLUSSY SNAKE");
		return stb.toString();
	}

	public void print(Source source, Consumer<String> logger){
		LnCol lc = getLineColumn(source);
		String snippet = getLineSnippet(source);

		logger.accept(lc!=null ? "["+lc+"] "+message : message);
		if(exception!=null){
			logger.accept(exception.toString());
			exception.printStackTrace();
		}
		if(snippet!=null) logger.accept(snippet);
		for(String hint : this.hints) logger.accept("hint: "+hint);
	}

	public enum ReportType{
		/**
		 * Report type indicating a simple, general purpose information.
		 */
		INFO,
		/**
		 * Report type indicating a negligible, yet suspicious observation.
		 */
		WARN,
		/**
		 * Report type indicating a critical failure occurred during operation.
		 */
		ERROR
	}
}

package ttmp.among.util;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.obj.AmongRoot;

/**
 * Provider for "native files" that can be imported from all among scripts. This object is automatically registered on
 * all instance of {@link AmongEngine}s.
 */
public final class DefaultInstanceProvider implements Provider<AmongRoot>{
	public static final String DEFAULT_OPERATOR = "among/default_operator";
	public static final String DEFAULT_OPERATORS = "among/default_operators";

	private DefaultInstanceProvider(){}
	private static final DefaultInstanceProvider INSTANCE = new DefaultInstanceProvider();
	public static DefaultInstanceProvider instance(){
		return INSTANCE;
	}

	@Nullable @Override public AmongRoot resolve(String path){
		if(DEFAULT_OPERATOR.equals(path)||DEFAULT_OPERATORS.equals(path)) return AmongRoot.withDefaultOperators();
		return null;
	}
}

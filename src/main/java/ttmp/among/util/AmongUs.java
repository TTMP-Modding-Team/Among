package ttmp.among.util;

import ttmp.among.obj.Among;

import java.util.regex.Pattern;

/**
 * Absolutely Mental Object Notation Utilities. The G and S are both silent.
 */
public class AmongUs{
	private static final Pattern NEWLINE = Pattern.compile("\r\n?|\n");
	private static final Pattern BACKSPACE = Pattern.compile("\b");
	private static final Pattern FORMAT = Pattern.compile("\f");

	private static final Pattern SIMPLE_NAME = Pattern.compile("[^\\s:,{}\\[\\]()]+");
	private static final Pattern SIMPLE_KEY = Pattern.compile("^(?!\\s)[^:{}]+(?<!\\s)$");
	private static final Pattern SIMPLE_PARAM = Pattern.compile("[^\\s:,{}\\[\\]()=]+");
	private static final Pattern SIMPLE_VALUE = Pattern.compile("^(?!\\s)[^:{}\r\n]+(?<!\\s)$");

	private static final Pattern PRIMITIVE_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\"]|/[*/]");

	private static final Pattern KEY_SPECIALS = Pattern.compile("[\\\\{}\"']|/[*/]");
	private static final Pattern NAME_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\"']|/[*/]");
	private static final Pattern PARAM_SPECIALS = Pattern.compile("[\\s\\\\{}\\[\\]()\"'=]|/[*/]");
	private static final Pattern VALUE_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\",]|/[*/]");

	public static boolean isSimpleName(String name){
		return SIMPLE_NAME.matcher(name).matches();
	}
	public static boolean isSimpleKey(String key){
		return SIMPLE_KEY.matcher(key).matches();
	}
	public static boolean isSimpleParam(String param){
		return SIMPLE_PARAM.matcher(param).matches();
	}
	public static boolean isSimpleValue(String value){
		return SIMPLE_VALUE.matcher(value).matches();
	}

	public static void keyToString(StringBuilder stb, String key, boolean paramRef){
		if(paramRef||isSimpleKey(key)) stb.append(standardReplace(KEY_SPECIALS, key, true));
		else primitiveToString(stb, key);
	}

	public static void keyToPrettyString(StringBuilder stb, String key, boolean paramRef, int indents, PrettyFormatOption option){
		if(paramRef||isSimpleKey(key)) stb.append(standardReplace(KEY_SPECIALS, key, true));
		else primitiveToPrettyString(stb, key, indents, option);
	}

	public static void nameToString(StringBuilder stb, String name, boolean paramRef){
		if(paramRef||isSimpleName(name)) stb.append(standardReplace(NAME_SPECIALS, name, true));
		else primitiveToString(stb, name);
	}

	public static void nameToPrettyString(StringBuilder stb, String name, boolean paramRef, int indents, PrettyFormatOption option){
		if(paramRef||isSimpleName(name)) stb.append(standardReplace(NAME_SPECIALS, name, true));
		else primitiveToPrettyString(stb, name, indents, option);
	}

	public static void paramToString(StringBuilder stb, String param){
		stb.append(standardReplace(PARAM_SPECIALS, param, true));
	}

	public static void valueToString(StringBuilder stb, Among value){
		if(!value.isPrimitive()) stb.append(value);
		else{
			String s = value.asPrimitive().getValue();
			if(isSimpleValue(s)) stb.append(standardReplace(VALUE_SPECIALS, s, true));
			else primitiveToString(stb, s);
		}
	}

	public static void valueToPrettyString(StringBuilder stb, Among value, int indents, PrettyFormatOption option){
		if(!value.isPrimitive()) stb.append(value.toPrettyString(indents, option));
		else{
			String s = value.asPrimitive().getValue();
			if(isSimpleValue(s)) stb.append(standardReplace(VALUE_SPECIALS, s, true));
			else primitiveToPrettyString(stb, s, indents, option);
		}
	}

	public static void primitiveToString(StringBuilder stb, String primitive){
		stb.append('"').append(standardReplace(PRIMITIVE_SPECIALS, primitive, true)).append('"');
	}

	public static void primitiveToPrettyString(StringBuilder stb, String primitive, int indents, PrettyFormatOption option){
		primitive = NEWLINE.matcher(standardReplace(PRIMITIVE_SPECIALS, primitive, false)).replaceAll(newlineAndIndent(indents+2, option)+'|');
		stb.append('"').append(primitive).append('"');
	}

	public static String newlineAndIndent(int indents, PrettyFormatOption option){
		StringBuilder stb = new StringBuilder();
		newlineAndIndent(stb, indents, option);
		return stb.toString();
	}

	public static void newlineAndIndent(StringBuilder stb, int indents, PrettyFormatOption option){
		stb.append('\n');
		for(int i = 0; i<indents; i++) stb.append(option.indent);
	}

	private static String standardReplace(Pattern specialPattern, String value, boolean newline){
		value = specialPattern.matcher(value).replaceAll("\\\\$0");
		if(newline) value = NEWLINE.matcher(value).replaceAll("\\\\n");
		value = BACKSPACE.matcher(value).replaceAll("\\\\b");
		return FORMAT.matcher(value).replaceAll("\\\\f");
	}
}

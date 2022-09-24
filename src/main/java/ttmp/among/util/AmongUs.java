package ttmp.among.util;

import ttmp.among.obj.Among;

import java.util.regex.Pattern;

/**
 * Absolutely Mental Object Notation Utilities. The G and S are both silent.
 */
// TODO paramRef is not considered - needs checking
public class AmongUs{
	private static final Pattern NEWLINE = Pattern.compile("\r\n?|\n");

	private static final Pattern SIMPLE_NAME = Pattern.compile("[^\\s:,{}\\[\\]()]+");
	private static final Pattern SIMPLE_KEY = Pattern.compile("^(?!\\s)[^:{}]+(?<!\\s)$");
	private static final Pattern SIMPLE_PARAM = Pattern.compile("[^\\s:,{}\\[\\]()=]+");
	private static final Pattern SIMPLE_VALUE = Pattern.compile("^(?!\\s)[^:{}\r\n]+(?<!\\s)$");

	private static final Pattern PRIMITIVE_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\"]");

	private static final Pattern KEY_SPECIALS = Pattern.compile("[\\\\{}\"']|/[*/]");
	private static final Pattern NAME_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\"']|/[*/]");
	private static final Pattern PARAM_SPECIALS = Pattern.compile("[\\s\\\\{}\\[\\]()\"'=]|/[*/]");
	private static final Pattern VALUE_SPECIALS = Pattern.compile("[\\\\{}\\[\\]()\"]|/[*/]");

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

	public static void keyToString(StringBuilder stb, String key){
		if(isSimpleKey(key)) stb.append(KEY_SPECIALS.matcher(key).replaceAll("\\\\$0"));
		else primitiveToString(stb, key);
	}

	public static void keyToPrettyString(StringBuilder stb, String key, int indents, String indent){
		if(isSimpleKey(key)) stb.append(KEY_SPECIALS.matcher(key).replaceAll("\\\\$0"));
		else primitiveToPrettyString(stb, key, indents, indent);
	}

	public static void nameToString(StringBuilder stb, String name){
		if(isSimpleName(name)) stb.append(NAME_SPECIALS.matcher(name).replaceAll("\\\\$0"));
		else primitiveToString(stb, name);
	}

	public static void nameToPrettyString(StringBuilder stb, String name, int indents, String indent){
		if(isSimpleName(name)) stb.append(NAME_SPECIALS.matcher(name).replaceAll("\\\\$0"));
		else primitiveToPrettyString(stb, name, indents, indent);
	}

	public static void paramToString(StringBuilder stb, String param){
		param = PARAM_SPECIALS.matcher(param).replaceAll("\\\\$0");
		param = NEWLINE.matcher(param).replaceAll("\\\\n");
		stb.append(param);
	}
	public static void paramToPrettyString(StringBuilder stb, String param, int indents, String indent){
		paramToString(stb, param);
	}

	public static void valueToString(StringBuilder stb, Among value){
		if(!value.isPrimitive()) stb.append(value);
		else{
			String s = value.asPrimitive().getValue();
			if(isSimpleValue(s)) stb.append(VALUE_SPECIALS.matcher(s).replaceAll("\\\\$0"));
			else primitiveToString(stb, s);
		}
	}

	public static void valueToPrettyString(StringBuilder stb, Among value, int indents, String indent){
		if(!value.isPrimitive()) stb.append(value.toPrettyString(indents, indent));
		else{
			String s = value.asPrimitive().getValue();
			if(isSimpleValue(s)) stb.append(VALUE_SPECIALS.matcher(s).replaceAll("\\\\$0"));
			else primitiveToPrettyString(stb, s, indents, indent);
		}
	}

	public static void primitiveToString(StringBuilder stb, String primitive){
		primitive = PRIMITIVE_SPECIALS.matcher(primitive).replaceAll("\\\\$0");
		primitive = NEWLINE.matcher(primitive).replaceAll("\\\\n");
		stb.append('"').append(primitive).append('"');
	}

	public static void primitiveToPrettyString(StringBuilder stb, String primitive, int indents, String indent){
		primitive = PRIMITIVE_SPECIALS.matcher(primitive).replaceAll("\\\\$0");
		primitive = NEWLINE.matcher(primitive).replaceAll("\n"+repeat(indents+2, indent)+'|');
		stb.append('"').append(primitive).append('"');
	}

	private static String repeat(int indents, String indent){
		StringBuilder stb = new StringBuilder();
		for(int i = 0; i<indents; i++) stb.append(indent);
		return stb.toString();
	}
}

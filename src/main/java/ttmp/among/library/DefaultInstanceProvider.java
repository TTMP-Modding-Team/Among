package ttmp.among.library;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.definition.AmongDefinition;
import ttmp.among.definition.Macro;
import ttmp.among.definition.MacroType;
import ttmp.among.definition.OperatorPriorities;
import ttmp.among.definition.OperatorProperty;
import ttmp.among.definition.OperatorRegistry;
import ttmp.among.definition.OperatorType;
import ttmp.among.definition.TypeFlags;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongNameable;
import ttmp.among.obj.AmongObject;
import ttmp.among.util.RootAndDefinition;

import java.util.Map;

/**
 * Provider for "native files" that can be imported from all among scripts. This object is automatically registered on
 * all instance of {@link AmongEngine}s.
 */
public final class DefaultInstanceProvider implements Provider<RootAndDefinition>{
	public static final String DEFAULT_OPERATOR = "default_operator";
	public static final String DEFAULT_OPERATORS = "default_operators";
	public static final String EVAL = "eval";
	public static final String COLLECTION = "collection";
	public static final String COLLECTIONS = "collections";
	public static final String FORMAT = "format";

	private DefaultInstanceProvider(){}
	private static final DefaultInstanceProvider INSTANCE = new DefaultInstanceProvider();
	public static DefaultInstanceProvider instance(){
		return INSTANCE;
	}

	@Nullable @Override public RootAndDefinition resolve(String path){
		switch(path){
			case DEFAULT_OPERATOR: case DEFAULT_OPERATORS:
				return new RootAndDefinition(defaultOperators());
			case EVAL:
				return new RootAndDefinition(eval());
			case COLLECTION: case COLLECTIONS:
				return new RootAndDefinition(collection());
			case FORMAT:
				return new RootAndDefinition(format());
			default: return null;
		}
	}

	/**
	 * Create a new definition with basic operators.
	 *
	 * @return New definition with basic operators
	 * @see <a href="https://youtu.be/J2C9oiP5j7Q">ok this link is supposed to be a link to the documentation about
	 * "basic operators" but i kinda dont have one now so i will just put a rickroll here if you dont mind yea just poke
	 * me if i have that doc up somewhere but didnt update this link ok thx</a>
	 */
	public static AmongDefinition defaultOperators(){
		AmongDefinition definition = new AmongDefinition();
		OperatorRegistry o = definition.operators();
		o.addOperator("=", OperatorType.BINARY, OperatorProperty.RIGHT_ASSOCIATIVE, OperatorPriorities.BINARY_ASSIGN);
		o.addOperator("||", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_OR);
		o.addOperator("&&", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_AND);
		o.addOperator("==", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_EQUALITY);
		o.addOperator("!=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_EQUALITY);
		o.addOperator(">", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("<", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator(">=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("<=", OperatorType.BINARY, OperatorPriorities.BINARY_LOGICAL_COMPARE);
		o.addOperator("|", OperatorType.BINARY, OperatorPriorities.BINARY_BITWISE);
		o.addOperator("&", OperatorType.BINARY, OperatorPriorities.BINARY_BITWISE);
		o.addOperator("+", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_ADDITION);
		o.addOperator("-", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_ADDITION);
		o.addOperator("*", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("/", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("^", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_POWER);
		o.addOperator("**", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_POWER);
		o.addOperator("!", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("-", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("+", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator(".", OperatorType.BINARY, "", OperatorProperty.ACCESSOR, OperatorPriorities.BINARY_ACCESS);
		return definition;
	}

	/**
	 * Create new definition with eval function.
	 *
	 * @return New definition with eval function
	 */
	public static AmongDefinition eval(){
		AmongDefinition definition = defaultOperators();
		definition.macros().add(Macro.builder("eval", MacroType.OPERATION)
				.param("expr")
				.build((args, copyConstant, reportHandler) -> EvalLib.eval(args[0], reportHandler)));
		return definition;
	}

	public static AmongDefinition collection(){
		AmongDefinition definition = defaultOperators();
		definition.macros().add(Macro.builder("named", MacroType.OPERATION_FN)
				.param("name", TypeFlags.PRIMITIVE)
				.inferSelfType(TypeFlags.NAMEABLE)
				.build((args, copyConstant, reportHandler) -> {
					AmongNameable copy = args[0].asNameable().copy();
					copy.setName(args[1].asPrimitive().getValue());
					return copy;
				}));
		definition.macros().add(Macro.builder("name", MacroType.ACCESS)
				.inferSelfType(TypeFlags.NAMEABLE)
				.build((args, copyConstant, reportHandler) -> Among.value(args[0].asNameable().getName())));
		definition.macros().add(Macro.builder("concat", MacroType.OPERATION_FN)
				.param("other", TypeFlags.LIST|TypeFlags.OPERATION)
				.inferSelfType(TypeFlags.LIST|TypeFlags.OPERATION)
				.build((args, copyConstant, reportHandler) -> {
					AmongList copy = args[0].asList().copy();
					for(Among a : args[1].asList()) copy.add(a);
					return copy;
				}));
		definition.macros().add(Macro.builder("merge", MacroType.OPERATION_FN)
				.param("other", TypeFlags.OBJECT)
				.inferSelfType(TypeFlags.OBJECT)
				.build((args, copyConstant, reportHandler) -> {
					AmongObject copy = args[0].asObj().copy();
					for(Map.Entry<String, Among> e : args[1].asObj().properties().entrySet()){
						if(!copy.hasProperty(e.getKey())) copy.setProperty(e.getKey(), e.getValue());
					}
					return copy;
				}));
		return definition;
	}

	public static AmongDefinition format(){
		AmongDefinition definition = defaultOperators();
		definition.operators().addOperator("%", OperatorType.BINARY, "format", 0.5);
		definition.macros().add(Macro.builder("format", MacroType.OPERATION)
				.param("format", TypeFlags.PRIMITIVE)
				.param("argument", Among.list())
				.build((args, copyConstant, reportHandler) -> {
					String fmt = args[0].asPrimitive().getValue();
					Among a = args[1];
					return Among.value(a.isPrimitive() ? FormatLib.format(fmt, a.asPrimitive().getValue()) :
							a.isList() ? FormatLib.format(fmt, a.asList().values().toArray()) :
									FormatLib.format(fmt, a.asObj().properties()));
				}));
		return definition;
	}
}

package ttmp.among.util;

import org.jetbrains.annotations.Nullable;
import ttmp.among.AmongEngine;
import ttmp.among.definition.AmongDefinition;
import ttmp.among.definition.OperatorPriorities;
import ttmp.among.definition.OperatorProperty;
import ttmp.among.definition.OperatorRegistry;
import ttmp.among.definition.OperatorType;

/**
 * Provider for "native files" that can be imported from all among scripts. This object is automatically registered on
 * all instance of {@link AmongEngine}s.
 */
public final class DefaultInstanceProvider implements Provider<RootAndDefinition>{
	public static final String DEFAULT_OPERATOR = "among/default_operator";
	public static final String DEFAULT_OPERATORS = "among/default_operators";

	private DefaultInstanceProvider(){}
	private static final DefaultInstanceProvider INSTANCE = new DefaultInstanceProvider();
	public static DefaultInstanceProvider instance(){
		return INSTANCE;
	}

	@Nullable @Override public RootAndDefinition resolve(String path){
		if(DEFAULT_OPERATOR.equals(path)||DEFAULT_OPERATORS.equals(path))
			return new RootAndDefinition(defaultOperators());
		return null;
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
		o.addOperator("%", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("^", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_PRODUCT);
		o.addOperator("**", OperatorType.BINARY, OperatorPriorities.BINARY_ARITHMETIC_POWER);
		o.addOperator("!", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("-", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("+", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator("~", OperatorType.PREFIX, OperatorPriorities.PREFIX);
		o.addOperator(".", OperatorType.BINARY, OperatorProperty.ACCESSOR, OperatorPriorities.BINARY_ACCESS);
		return definition;
	}
}

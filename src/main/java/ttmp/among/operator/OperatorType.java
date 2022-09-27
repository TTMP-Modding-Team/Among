package ttmp.among.operator;

/**
 * Type of the operator
 */ // TODO docs
public enum OperatorType{
	BINARY, POSTFIX, PREFIX;

	public double defaultPriority(){
		switch(this){
			case BINARY: return OperatorPriorities.BINARY_CUSTOM;
			case POSTFIX: return OperatorPriorities.POSTFIX_CUSTOM;
			case PREFIX: default: return OperatorPriorities.PREFIX;
		}
	}
}

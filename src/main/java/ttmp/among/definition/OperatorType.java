package ttmp.among.definition;

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

	@Override public String toString(){
		switch(this){
			case BINARY: return "binary";
			case POSTFIX: return "postfix";
			case PREFIX: return "prefix";
			default: throw new IllegalStateException("Unreachable");
		}
	}
}

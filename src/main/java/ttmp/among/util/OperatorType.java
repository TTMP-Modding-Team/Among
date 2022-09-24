package ttmp.among.util;

/**
 * Type of the operator
 */ // TODO docs
public enum OperatorType{
	BINARY, POSTFIX, PREFIX;

	public double defaultPriority(){
		switch(this){
			case BINARY: return 11;
			case POSTFIX: return 12;
			case PREFIX: default: return 13;
		}
	}
}

package ttmp.among.util;

import ttmp.among.exception.Sussy;
import ttmp.among.obj.AmongMacroDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Immutable list of {@link MacroParameter}s. Provides both search-by-index and search-by-name functionality.
 *
 * @see AmongMacroDef
 * @see MacroParameter
 */
public final class MacroParameterList implements ToPrettyString{
	/**
	 * Empty list.
	 */
	private static final MacroParameterList EMPTY = new MacroParameterList();

	/**
	 * @return Empty parameter list
	 */
	public static MacroParameterList of(){
		return EMPTY;
	}
	/**
	 * @return Parameter list with given parameters
	 * @throws NullPointerException If the array or one of the parameters are {@code null}
	 * @throws Sussy                If two parameters have same name
	 */
	public static MacroParameterList of(MacroParameter... parameters){
		return parameters.length==0 ? EMPTY : new MacroParameterList(parameters);
	}
	/**
	 * @return Parameter list with given parameters
	 * @throws NullPointerException If the collection or one of the parameters are {@code null}
	 * @throws Sussy                If two parameters have same name
	 */
	public static MacroParameterList of(Collection<MacroParameter> parameters){
		return parameters.isEmpty() ? EMPTY : new MacroParameterList(parameters);
	}

	private final List<MacroParameter> params = new ArrayList<>();
	private final Map<String, Integer> nameToIndex = new HashMap<>();

	private MacroParameterList(){}
	private MacroParameterList(MacroParameter... parameters){
		for(MacroParameter p : parameters){
			if(nameToIndex.putIfAbsent(p.name(), params.size())!=null)
				throw new Sussy("Duplicated registration of parameter with name of '"+p.name()+"'");
			params.add(p);
		}
	}
	private MacroParameterList(Collection<MacroParameter> parameters){
		for(MacroParameter p : parameters){
			if(nameToIndex.putIfAbsent(p.name(), params.size())!=null)
				throw new Sussy("Duplicated registration of parameter with name of '"+p.name()+"'");
			params.add(p);
		}
	}

	public int size(){
		return params.size();
	}
	public boolean isEmpty(){
		return params.isEmpty();
	}

	public MacroParameter getParam(int index){
		return params.get(index);
	}
	/**
	 * @return Index of the parameter with given name, or {@code -1} if there isn't
	 */
	public int indexOf(String paramName){
		Integer i = nameToIndex.get(paramName);
		return i!=null ? i : -1;
	}

	/**
	 * Checks for consecutive optional parameters; used for list/operation macros.
	 *
	 * @return Number of required parameters (non-default parameters)
	 */
	public int checkConsecutiveOptionalParams(){
		boolean defaultParamSeen = false;
		int nonDefaultParamCount = 0;
		for(int i = 0; i<params.size(); i++){
			MacroParameter p = params.get(i);
			if(defaultParamSeen){
				if(p.defaultValue()==null)
					throw new Sussy("Non-object macros should have consecutive optional parameters (i.e. Needs to have all optional parameters at the end of the parameter list, not in between)");
			}else{
				if(p.defaultValue()!=null){
					defaultParamSeen = true;
					nonDefaultParamCount = i;
				}
			}
		}
		return nonDefaultParamCount;
	}

	@Override public String toString(){
		return params.stream()
				.map(MacroParameter::toString)
				.collect(Collectors.joining(","));
	}

	@Override public String toPrettyString(int indents, String indent){
		return params.stream()
				.map(p -> p.toPrettyString(indents+1, indent))
				.collect(Collectors.joining(", "));
	}
}

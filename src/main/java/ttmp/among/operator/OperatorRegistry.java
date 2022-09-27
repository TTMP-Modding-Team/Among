package ttmp.among.operator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static ttmp.among.operator.OperatorType.BINARY;
import static ttmp.among.operator.OperatorType.POSTFIX;

/**
 * Collection of {@link OperatorDefinition}s organized in various format to ensure faster access during compilation.<br>
 * Just to be clear, absolutely everything in this class is not thread safe.
 */
public final class OperatorRegistry{
	private final Map<String, NameGroup> operators = new HashMap<>();
	private final Map<Double, PriorityGroup> priorityGroup = new HashMap<>();
	@Nullable private List<PriorityGroup> priorityGroupList;

	public OperatorRegistry(){}
	public OperatorRegistry(OperatorRegistry copyFrom){
		copyFrom.forEachOperatorAndKeyword(this::add);
	}

	/**
	 * Starting codepoint to set of name groups, sorted by name length in descending order (for maximal munch rule)
	 */
	private final Map<Integer, Set<NameGroup>> operatorByStartingCodepoint = new HashMap<>();
	/**
	 * Starting codepoint to set of name groups, sorted by name length in descending order (for maximal munch rule)
	 */
	private final Map<Integer, Set<NameGroup>> keywordByStartingCodepoint = new HashMap<>();

	public Collection<NameGroup> getAllNameGroups(){
		return Collections.unmodifiableCollection(operators.values());
	}

	public RegistrationResult addOperator(String name, OperatorType type){
		return addOperator(name, type, Double.NaN);
	}
	public RegistrationResult addOperator(String name, OperatorType type, double priority){
		return add(new OperatorDefinition(name, false, type, priority));
	}

	public RegistrationResult addKeyword(String name, OperatorType type){
		return addKeyword(name, type, Double.NaN);
	}
	public RegistrationResult addKeyword(String name, OperatorType type, double priority){
		return add(new OperatorDefinition(name, false, type, priority));
	}

	public RegistrationResult add(OperatorDefinition definition){
		if(isPriorityOccupiedByWrongType(definition))
			return RegistrationResult.PRIORITY_OCCUPIED_BY_DIFFERENT_TYPE;
		NameGroup o = operators.get(definition.name());
		if(o==null){
			operators.put(definition.name(), o = new NameGroup(definition));
			if(!definition.name().isEmpty())
				(definition.isKeyword() ?
						keywordByStartingCodepoint :
						operatorByStartingCodepoint)
						.computeIfAbsent(o.codePointAt(0), i -> new TreeSet<>())
						.add(o);
		}else{
			RegistrationResult result = o.add(definition);
			if(!result.isSuccess()) return result;
		}
		addToPriorityGroup(definition);
		return RegistrationResult.OK;
	}

	public void remove(String operatorName, boolean keyword){
		NameGroup op = operators.get(operatorName);
		if(op!=null&&op.isKeyword==keyword){
			operators.remove(operatorName);
			if(!operatorName.isEmpty()){
				Map<Integer, Set<NameGroup>> m = keyword ?
						keywordByStartingCodepoint :
						operatorByStartingCodepoint;
				Set<NameGroup> operators = m.get(op.codePointAt(0));
				operators.remove(op);
				if(operators.isEmpty()) m.remove(op.codePointAt(0));
			}
			for(OperatorDefinition def : op.defByType.values()){
				removeFromParsingOrder(def);
			}
		}
	}

	private boolean isPriorityOccupiedByWrongType(OperatorDefinition definition){
		PriorityGroup g = this.priorityGroup.get(definition.priority());
		return g!=null&&g.type()!=definition.type();
	}
	private void addToPriorityGroup(OperatorDefinition definition){
		PriorityGroup g = this.priorityGroup.get(definition.priority());
		if(g==null) this.priorityGroup.put(definition.priority(), new PriorityGroup(definition));
		else g.add(definition);
		priorityGroupList = null;
	}

	private void removeFromParsingOrder(OperatorDefinition def){
		PriorityGroup g = this.priorityGroup.get(def.priority());
		if(g!=null){
			g.remove(def);
			if(g.isEmpty()){
				this.priorityGroup.remove(def.priority());
				this.priorityGroupList = null;
			}
		}
	}

	public Set<NameGroup> getOperators(int startingCodePoint){
		Set<NameGroup> operators = operatorByStartingCodepoint.get(startingCodePoint);
		return operators==null ? Collections.emptySet() : Collections.unmodifiableSet(operators);
	}

	public Set<NameGroup> getKeywords(int startingCodePoint){
		Set<NameGroup> keywords = keywordByStartingCodepoint.get(startingCodePoint);
		return keywords==null ? Collections.emptySet() : Collections.unmodifiableSet(keywords);
	}

	public List<PriorityGroup> priorityGroup(){
		if(priorityGroupList==null){
			List<PriorityGroup> groups = new ArrayList<>(this.priorityGroup.values());
			groups.sort(null);
			priorityGroupList = Collections.unmodifiableList(groups);
		}
		return priorityGroupList;
	}

	public void forEachOperatorAndKeyword(Consumer<OperatorDefinition> consumer){
		for(NameGroup op : operators.values())
			op.defByType.values().forEach(consumer);
	}

	public Set<OperatorDefinition> allOperatorsAndKeywords(){
		Set<OperatorDefinition> set = new HashSet<>();
		for(NameGroup op : operators.values())
			set.addAll(op.defByType.values());
		return set;
	}

	@Override public boolean equals(Object obj){
		if(this==obj) return true;
		if(!(obj instanceof OperatorRegistry)) return false;
		OperatorRegistry reg = (OperatorRegistry)obj;
		return this.allOperatorsAndKeywords().equals(reg.allOperatorsAndKeywords());
	}

	/**
	 * Result of {@link OperatorRegistry#add(OperatorDefinition)}. Values other than {@link RegistrationResult#OK}
	 * represents different reasons for failure.
	 */
	public enum RegistrationResult{
		/**
		 * Success.
		 */
		OK,
		/**
		 * Defined as both operator and keyword.
		 */
		BOTH_OPERATOR_AND_KEYWORD,
		/**
		 * Defined twice as same type.
		 */
		DUPLICATE,
		/**
		 * Defined as both binary and postfix, which is incompatible.<br>
		 * Being them together should not produce weird crashes or anything, it just does not behave in the way average
		 * user would expect. This restriction is in place to prevent such scenario from get-go.
		 */
		BOTH_BINARY_AND_POSTFIX,
		/**
		 * Defined with priority value that is already being in use with different type.
		 */
		PRIORITY_OCCUPIED_BY_DIFFERENT_TYPE;

		public boolean isSuccess(){
			return this==OK;
		}

		public String message(OperatorDefinition def){
			switch(this){
				case BOTH_OPERATOR_AND_KEYWORD: return "Word '"+def.name()+"' is defined as both operator and keyword";
				case DUPLICATE: return (def.isKeyword() ? "Keyword '" : "Operator '")+def.name()+"' is defined twice";
				case BOTH_BINARY_AND_POSTFIX: return (def.isKeyword() ? "Keyword '" : "Operator '")+def.name()+"' is both defined as binary and postfix";
				case PRIORITY_OCCUPIED_BY_DIFFERENT_TYPE: return "Priority "+def.priority()+" is already in use with different type";
				default: return "";
			}
		}
	}

	/**
	 * Operators grouped by their name. Used in tokenization.
	 */
	public static final class NameGroup implements Comparable<NameGroup>{
		private final String name;
		private final int[] codePoints;
		private final boolean isKeyword;
		private final EnumMap<OperatorType, OperatorDefinition> defByType = new EnumMap<>(OperatorType.class);

		public NameGroup(OperatorDefinition initial){
			this.name = initial.name();
			this.isKeyword = initial.isKeyword();
			this.codePoints = name.codePoints().toArray();
			add(initial);
		}

		public String name(){
			return name;
		}
		public boolean isKeyword(){
			return isKeyword;
		}

		public int codePointLength(){
			return codePoints.length;
		}
		public int codePointAt(int index){
			return codePoints[index];
		}

		public Collection<OperatorDefinition> definitions(){
			return Collections.unmodifiableCollection(defByType.values());
		}

		private RegistrationResult add(OperatorDefinition def){
			if(this.isKeyword!=def.isKeyword())
				return RegistrationResult.BOTH_OPERATOR_AND_KEYWORD;
			if(defByType.containsKey(def.type()))
				return RegistrationResult.DUPLICATE;
			if(def.type()==BINARY||def.type()==POSTFIX){
				if(defByType.containsKey(def.type()==BINARY ? POSTFIX : BINARY))
					return RegistrationResult.BOTH_BINARY_AND_POSTFIX;
			}
			defByType.put(def.type(), def);
			return RegistrationResult.OK;
		}

		@Override public int compareTo(@NotNull OperatorRegistry.NameGroup o){
			int c = Integer.compare(o.name.length(), name.length());
			if(c!=0) return c;
			c = this.name.compareTo(o.name);
			return c;
		}

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			NameGroup operator = (NameGroup)o;
			return Objects.equals(name, operator.name);
		}
		@Override public int hashCode(){
			return Objects.hash(name);
		}

		@Override public String toString(){
			return name;
		}
	}

	/**
	 * Operators grouped by their priority. Used in parsing.
	 */
	public static final class PriorityGroup implements Comparable<PriorityGroup>{
		private final double priority;
		private final OperatorType type;
		private final Map<String, OperatorDefinition> operators = new HashMap<>();

		public PriorityGroup(OperatorDefinition initial){
			this.priority = initial.priority();
			this.type = initial.type();
			add(initial);
		}

		private void add(OperatorDefinition def){
			if(Double.compare(this.priority, def.priority())!=0)
				throw new IllegalStateException("Trying to register operator with wrong priority to group");
			if(this.type!=def.type())
				throw new IllegalStateException("Trying to register operator with wrong type to group");
			if(operators.putIfAbsent(def.name(), def)!=null)
				throw new IllegalStateException("Duplicated registration of operator '"+def.name()+"'");
		}

		private void remove(OperatorDefinition def){
			if(Double.compare(this.priority, def.priority())==0&&this.type==def.type())
				operators.remove(def.name());
		}

		public double priority(){
			return priority;
		}
		public OperatorType type(){
			return type;
		}

		public boolean isEmpty(){
			return operators.isEmpty();
		}

		@Nullable public OperatorDefinition get(String name){
			return operators.get(name);
		}

		@Override public int compareTo(@NotNull OperatorRegistry.PriorityGroup o){
			return Double.compare(priority, o.priority);
		}

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			PriorityGroup that = (PriorityGroup)o;
			return Double.compare(that.priority, priority)==0;
		}
		@Override public int hashCode(){
			return Objects.hash(priority);
		}

		@Override public String toString(){
			return priority+":"+type;
		}
	}
}
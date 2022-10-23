package ttmp.among.definition;

import org.jetbrains.annotations.Nullable;
import ttmp.among.compile.ReportType;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongList;
import ttmp.among.obj.AmongObject;
import ttmp.among.util.PrettyFormatOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class MacroRegistry{
	private final Map<MacroSignature, Group> groups = new HashMap<>();

	public MacroRegistry(){}
	public MacroRegistry(MacroRegistry macros){
		groups.putAll(macros.groups);
	}

	public boolean isEmpty(){
		return groups.isEmpty();
	}
	public void clear(){
		groups.clear();
	}

	public void add(Macro macro){
		add(macro, null);
	}
	public void add(Macro macro, @Nullable BiConsumer<ReportType, String> reportHandler){
		Group g = groups.computeIfAbsent(macro.signature(), s -> {
			switch(s.type()){
				case CONST: case ACCESS: return new ConstGroup();
				case OBJECT: return new ObjectGroup();
				case LIST: case OPERATION: return new ListGroup();
				case OBJECT_FN: return new ObjectFunctionGroup();
				case LIST_FN: case OPERATION_FN: return new ListFunctionGroup();
				default: throw new IllegalStateException("Unreachable");
			}
		});
		g.add(macro, reportHandler);
	}

	public void remove(String name, MacroType type){
		remove(new MacroSignature(name, type));
	}
	public void remove(MacroSignature signature){
		groups.remove(signature);
	}

	@Nullable public Group groupFor(Among argument){
		return groupFor(MacroSignature.of(argument));
	}
	@Nullable public Group groupFor(String name, MacroType type){
		return groupFor(new MacroSignature(name, type));
	}
	@Nullable public Group groupFor(MacroSignature signature){
		return groups.get(signature);
	}

	public Stream<Macro> macros(){
		return groups.values().stream().flatMap(Group::macros);
	}
	public Stream<MacroSignature> macroSignatures(){
		return groups.keySet().stream();
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MacroRegistry that = (MacroRegistry)o;
		return groups.equals(that.groups);
	}
	@Override public int hashCode(){
		return Objects.hash(groups);
	}

	public static abstract class Group{
		@Nullable public abstract Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler);
		protected abstract void add(Macro macro, @Nullable BiConsumer<ReportType, String> reportHandler);
		protected abstract Stream<Macro> macros();
	}

	public static final class ConstGroup extends Group{
		@Nullable private Macro macro;

		@Override @Nullable public Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			return macro;
		}
		@Override protected void add(Macro macro, @Nullable BiConsumer<ReportType, String> reportHandler){
			this.macro = macro; // TODO report override
		}
		@Override protected Stream<Macro> macros(){
			return Stream.of(macro);
		}

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ConstGroup that = (ConstGroup)o;
			return Objects.equals(macro, that.macro);
		}
		@Override public int hashCode(){
			return Objects.hash(macro);
		}
	}

	public static abstract class MatchBasedGroup<T extends Among> extends Group{
		private final List<Macro> macros = new ArrayList<>();

		@Nullable protected Macro searchInternal(T argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			@Nullable Macro matched = null;
			int overmatched = 0;
			@Nullable List<Macro> ambiguousMacros = null;
			for(Macro m : macros){
				int match = match(m, argument);
				if(match<0) continue;
				if(matched==null){
					matched = m;
					overmatched = match;
				}else if(overmatched>match){
					matched = m;
					overmatched = match;
					if(ambiguousMacros!=null) ambiguousMacros.clear();
				}else if(overmatched==match){
					if(ambiguousMacros==null){
						ambiguousMacros = new ArrayList<>();
						ambiguousMacros.add(matched);
					}
					ambiguousMacros.add(m);
				}
			}
			if(ambiguousMacros!=null&&!ambiguousMacros.isEmpty()){
				reportAmbiguousUsage(reportHandler, ambiguousMacros.get(0).signature(), ambiguousMacros);
				return null;
			}
			if(matched==null) reportNoMatch(reportHandler, macros);
			return matched;
		}
		@Override protected void add(Macro macro, @Nullable BiConsumer<ReportType, String> reportHandler){
			macros.add(macro); // TODO report obvious conflicts?
		}
		@Override protected Stream<Macro> macros(){
			return macros.stream();
		}

		/**
		 * @return {@code -1} if doesn't match, {@code 0} if matches perfectly, positive number {@code n} if {@code n} arguments are oversupplied
		 */
		protected abstract int match(Macro macro, T args);

		@Override public boolean equals(Object o){
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			MatchBasedGroup<?> that = (MatchBasedGroup<?>)o;
			return macros.equals(that.macros);
		}
		@Override public int hashCode(){
			return Objects.hash(macros);
		}
	}

	public static class ListGroup extends MatchBasedGroup<AmongList>{
		@Override @Nullable public Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			return searchInternal(argument.asList(), reportHandler);
		}
		@Override protected int match(Macro macro, AmongList args){
			if(args.size()<macro.parameter().requiredParameters()) return -1;
			return Math.max(0, args.size()-macro.parameter().size());
		}
	}

	public static class ObjectGroup extends MatchBasedGroup<AmongObject>{
		@Override @Nullable public Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			return searchInternal(argument.asObj(), reportHandler);
		}
		@Override protected int match(Macro macro, AmongObject args){
			if(args.size()<macro.parameter().requiredParameters()) return -1;
			int defaultArgsProvided = 0;
			for(int i = 0; i<macro.parameter().size(); i++){
				MacroParameter p = macro.parameter().paramAt(i);
				if(p.defaultValue()==null){
					if(!args.hasProperty(p.name())) return -1;
				}else if(args.hasProperty(p.name())) defaultArgsProvided++;
			}
			return Math.max(0, args.size()-macro.parameter().requiredParameters()-defaultArgsProvided);
		}
	}

	public static class ListFunctionGroup extends ListGroup{
		@Override @Nullable public Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			return searchInternal(argument.asList().get(1).asList(), reportHandler);
		}
	}

	public static class ObjectFunctionGroup extends ObjectGroup{
		@Override @Nullable public Macro search(Among argument, @Nullable BiConsumer<ReportType, String> reportHandler){
			return searchInternal(argument.asList().get(1).asObj(), reportHandler);
		}
	}

	private static void reportAmbiguousUsage(@Nullable BiConsumer<ReportType, String> reportHandler,
	                                         MacroSignature signature, Iterable<Macro> ambiguousMacros){
		if(reportHandler==null) return;
		StringBuilder stb = new StringBuilder("Ambiguous usage of macro ").append(signature).append(':');
		for(Macro m : ambiguousMacros){
			stb.append("\n  ");
			m.signatureToPrettyString(stb, 0, PrettyFormatOption.DEFAULT, true);
		}
		reportHandler.accept(ReportType.ERROR, stb.toString());
	}

	private static void reportNoMatch(@Nullable BiConsumer<ReportType, String> reportHandler,
	                                  Collection<Macro> macros){
		if(reportHandler==null) return;
		if(macros.isEmpty()){
			reportHandler.accept(ReportType.ERROR, "No macro defined, this shouldn't happen");
		}else{
			StringBuilder stb = new StringBuilder("Wrong usage, expected:");
			for(Macro m : macros){
				stb.append("\n  ");
				m.signatureToPrettyString(stb, 0, PrettyFormatOption.DEFAULT, true);
			}
			reportHandler.accept(ReportType.ERROR, stb.toString());
		}
	}
}

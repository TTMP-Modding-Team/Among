package test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ttmp.among.obj.Among;
import ttmp.among.obj.MacroDefinition;
import ttmp.among.obj.OperatorDefinition;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.MacroDefinitionBuilder;
import ttmp.among.util.MacroType;
import ttmp.among.util.OperatorPriorities;
import ttmp.among.util.OperatorRegistry;
import ttmp.among.util.OperatorType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ttmp.among.obj.Among.*;

public class RecompileTests{
	@TestFactory
	public List<DynamicTest> recompileTests(){
		List<DynamicTest> list = new ArrayList<>();
		list.add(recompileTest("Object 1", value(3),
				value("The quick brown fox jumps over the lazy dog"),
				value("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut"+
						" labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco"+
						" laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in"+
						" voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat"+
						" cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."),
				value("The Industrial Revolution and its consequences have been a disaster for the\nhuman race."),
				value("According to all known laws of aviation, there is no way a bee should be able to fly.\n"+
						"Its wings are too small to get its fat little body off the ground.\n"+
						"The bee, of course, flies anyway because bees don't care what humans think is impossible.\n"+
						"Yellow, black. Yellow, black.\n"+
						"Yellow, black. Yellow, black.\n"+
						"Ooh, black and yellow!\n"+
						"Let's shake it up a l\n")));
		list.add(recompileTest("Object 2", object().prop("Hello", "World!")));
		list.add(recompileTest("Object 3", list(1, 2, 3)));
		list.add(recompileTest("Object 4", object()
				.prop("L1", list(1, 2, 3, 4, 5))
				.prop("L2", namedList("Name2",
						namedList("1", 1, 2),
						namedList("2", 3, 4),
						namedList("3", 5, 6)))
				.prop("O1", namedObject("Obj"))
				.prop("O2", object())
				.prop("O3", namedObject("Values")
						.prop("A", "A")
						.prop("B", "B")
						.prop("C", "C"))));
		list.add(recompileTest("Object 5", object()
						.prop("1", "ASDF;lc::::===12354\n\n\n**&&%%^&(/**/8*/%//{}{}[][]),,,,.,.,...///12#f\\\\\\\\!!!!!!")
						.prop("2", "Amogus\"); DROP TABLE Everything;/*")
						.prop("3", "////////// lol"),
				value("ASDF;lc::::===12354\n\n\n**&&%%^&(/**/8*/%//{}{}[][]),,,,.,.,...///12#f\\\\\\\\!!!!!!"),
				value("Amogus\"); DROP TABLE Everything;/*"),
				value("////////// lol")));

		list.add(recompileTest("Macro 1", MacroDefinition.builder()
				.signature("macro", MacroType.CONST).template(object()
						.prop("Hello", "Macro!"))));
		list.add(recompileTest("Macro 2",
				MacroDefinition.builder().signature("macro1", MacroType.LIST)
						.template(value("Macro with zero parameters")),
				MacroDefinition.builder().signature("macro2", MacroType.LIST)
						.param("1")
						.template(list(value("Macro with 1 parameter"), object()
								.prop("param 1", value("$1").paramRef())
						)),
				MacroDefinition.builder().signature("macro3", MacroType.LIST)
						.param("1").param("2", value("default"))
						.template(list(value("Macro with 2 parameters"), object()
								.prop("param 1", value("$1").paramRef())
								.prop("param 2", value("$2").paramRef())
						)),
				MacroDefinition.builder().signature("macro4", MacroType.LIST)
						.param("1").param("2", value("default")).param("3", namedList("default 2", 1, 2, 3))
						.template(list(value("Macro with 3 parameters"), object()
								.prop("param 1", value("$1").paramRef())
								.prop("param 2", value("$2").paramRef())
								.prop("param 3", value("$3").paramRef())
						))));

		list.add(recompileTest("Operator 1", new OperatorDefinition("yo", true, OperatorType.PREFIX)));
		list.add(recompileTest("Operator 2",
				new OperatorDefinition("yo", true, OperatorType.PREFIX),
				new OperatorDefinition("***", false, OperatorType.BINARY),
				new OperatorDefinition("..", false, OperatorType.BINARY, OperatorPriorities.BINARY_ACCESS)
		));

		return list;
	}

	private static DynamicTest recompileTest(String name, Among... original){
		return DynamicTest.dynamicTest(name, () -> {
			AmongRoot root = AmongRoot.empty();
			for(Among v : original) root.addObject(v);
			System.out.println("========== Original ==========");
			System.out.println(root.toPrettyString());
			assertArrayEquals(original, TestUtil.make(root.toString(), AmongRoot.empty()).objects().toArray(new Among[0]));
			assertArrayEquals(original, TestUtil.make(root.toPrettyString(), AmongRoot.empty()).objects().toArray(new Among[0]));
		});
	}

	private static DynamicTest recompileTest(String name, MacroDefinitionBuilder... original){
		return DynamicTest.dynamicTest(name, () -> {
			AmongRoot root = AmongRoot.empty();
			for(MacroDefinitionBuilder v : original){
				MacroDefinition orig = root.addMacro(v.build());
				if(orig!=null)
					throw new RuntimeException("Macro definition '"+v+"' is duplicate of '"+orig+"'");
			}
			System.out.println("========== Original ==========");
			System.out.println(root.definitionsToPrettyString());
			assertEquals(root.macros(), TestUtil.make(root.definitionsToPrettyString(), AmongRoot.empty()).macros());
		});
	}

	private static DynamicTest recompileTest(String name, OperatorDefinition... original){
		return DynamicTest.dynamicTest(name, () -> {
			AmongRoot root = AmongRoot.empty();
			for(OperatorDefinition v : original){
				OperatorRegistry.RegistrationResult r = root.operators().add(v);
				if(!r.isSuccess())
					throw new RuntimeException("Cannot register operator '"+v+"': "+r.message(v));
			}
			System.out.println("========== Original ==========");
			System.out.println(root.definitionsToPrettyString());
			assertEquals(root.operators().allOperatorsAndKeywords(), TestUtil.make(root.definitionsToPrettyString(), AmongRoot.empty()).operators().allOperatorsAndKeywords());
		});
	}
}

package test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongMacroDef;
import ttmp.among.obj.AmongOperatorDef;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.AmongMacroDefBuilder;
import ttmp.among.util.MacroType;
import ttmp.among.util.OperatorPriorities;
import ttmp.among.util.OperatorRegistry;
import ttmp.among.util.OperatorType;
import ttmp.among.util.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ttmp.among.obj.Among.*;

public class Tests{
	@TestFactory
	public List<DynamicTest> simpleEqualityTests(){
		List<DynamicTest> list = new ArrayList<>();

		list.add(simpleEqualityTest("objTest",
				object().prop("Hello", "World!"),
				object().prop("1", 1).prop("2", 2).prop("3", 3),
				object().prop("Hello", "World!"),
				object().prop("1", 1).prop("2", 2).prop("3", 3)));
		list.add(simpleEqualityTest("listTest",
				list("one", "two", "oatmeal"),
				list("one two oatmeal", "kirby is a pink guy", "one two oatmeal", "because kirby is very cute")));
		list.add(simpleEqualityTest("operationTest",
				list(1, 1, 2, 3, 5, 8, 13, 21),
				list(1, 1, 2, 3, 5, 8, 13, 21)));
		list.add(simpleEqualityTest("primitiveTest",
				value("The Industrial Revolution and its consequences have been a disaster for the\nhuman race."),
				value("According to all known laws of aviation, there is no way a bee should be able to fly.\n"+
						"Its wings are too small to get its fat little body off the ground.\n"+
						"The bee, of course, flies anyway because bees don't care what humans think is impossible.\n"+
						"Yellow, black. Yellow, black.\n"+
						"Yellow, black. Yellow, black.\n"+
						"Ooh, black and yellow!\n"+
						"Let's shake it up a l\n"),
				value("Look ma, I'm on TV!")));
		list.add(simpleEqualityTest("macroTest",
				value("This is macro"),
				namedList("macro"),
				object().prop("Macro", "Hi!"),
				object().prop("Macro", namedList("*", "amo", "gus"))));
		list.add(simpleEqualityTest("collapseUnaryOperation",
				namedList("/",
						namedList("-", "x", "y"),
						"2"),
				namedList("+",
						namedList("+",
								namedList("+",
										list(1, 2, 3),
										list("list")),
								list()),
						namedList("fib", 3))));
		list.add(simpleEqualityTest("undefTest",
				value("Yes, I am a macro, indeed......"),
				value("Yes, I am a macro, indeed......"),
				namedList("areYouMacro"),
				namedList("!!", "a", "b"),
				namedList("!!", namedList("!!", "a"), "b"),
				list("!!", "!!")));

		list.add(simpleEqualityTest("1",
				object().prop("Property", "Value")
						.prop("P2", "123")
						.prop("P3", "1 2 3")
						.prop("P4", "Except for this expression\nThe line break inbetween is a part of this text msg")
						.prop("More complex key", ":thanosdaddy:")));
		list.add(simpleEqualityTest("2",
				object().prop("Object", object())
						.prop("NamedObj", namedObject("Named"))
						.prop("List", list(1, 2, 3))
						.prop("NamedList", namedList("Named", 1, 2, 3))
						.prop("ObjectWithField", object().prop("1", 1).prop("2", 2).prop("3", 3))));
		list.add(simpleEqualityTest("3",
				object().prop("Operation",
								namedList("=",
										namedList("+", 1, 2),
										3))
						.prop("Op2",
								namedList(">", "a", "b"))
						.prop("Op3",
								namedList("=",
										namedList("^", "c", 2),
										namedList("+",
												namedList("^", "a", 2),
												namedList("^", "b", 2))))
						.prop("Op4", namedList("+",
								1,
								namedList("*",
										2,
										namedList("+", 3, 4))))
						.prop("InsanelyNestedOperation", "This is fine.")));
		list.add(simpleEqualityTest("4",
				object()
						.prop("NamedOperation", namedList("Among", "Us"))
						.prop("2", namedList("=",
								namedList("fib", "a"),
								namedList("+",
										namedList("fib", namedList("-", "a", 2)),
										namedList("fib", namedList("-", "a", 1)))))));
		list.add(simpleEqualityTest("5",
				object().prop("Team", list(
						namedObject("Pokemon")
								.prop("Name", "Pikachu")
								.prop("Type", list("Electric")))),
				object().prop("BatMan", list("bat", "man"))
						.prop("2", namedList("*",
								namedList("sqrt", 2),
								namedList("sqrt", 2)))
						.prop("16", namedList("*",
								namedList("*", 2, 2),
								namedList("*", 2, 2))),
				object().prop("That's a lot of X's", list("x", "Indeed!", "$x")),
				object().prop("Fib1",
						namedList("+",
								namedList("fib", namedList("-", 1, 2)),
								namedList("fib", namedList("-", 1, 1)))),
				object().prop("Fib1", namedList("fib", 1)),
				object().prop("Vector1", object()
								.prop("X", 1).prop("Y", 2).prop("Z", 3))
						.prop("The Truth", 69420)));
		list.add(simpleEqualityTest("6",
				object().prop("A", namedList("...", "a", "b"))
						.prop("B", namedList("eat", "shit")),
				object().prop("Operator", namedList("...", "amo", "gus"))
						.prop("Keyword", "great")));
		list.add(simpleEqualityTest("7",
				namedList("awsdsf", 1, 2, 3),
				namedList("=", namedList("+", 1, 2), 3),
				object().prop("$key", "us")));
		return list;
	}

	private static DynamicTest simpleEqualityTest(String name, Among... expected){
		return DynamicTest.dynamicTest(name, () -> {
			String url = "equality_tests/"+name+".among";
			InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
			assertNotNull(file, "File not found at '"+url+"'");
			Source src = Source.read(new InputStreamReader(file, StandardCharsets.UTF_8));
			assertArrayEquals(expected, TestUtil.make(src).objects().toArray(new Among[0]));
		});
	}

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

		list.add(recompileTest("Macro 1", AmongMacroDef.builder()
				.signature("macro", MacroType.CONST).object(object()
						.prop("Hello", "Macro!"))));
		list.add(recompileTest("Macro 2",
				AmongMacroDef.builder().signature("macro1", MacroType.LIST)
						.object(value("Macro with zero parameters")),
				AmongMacroDef.builder().signature("macro2", MacroType.LIST)
						.param("1")
						.object(list(value("Macro with 1 parameter"), object()
								.prop("param 1", value("$1").paramRef())
						)),
				AmongMacroDef.builder().signature("macro3", MacroType.LIST)
						.param("1").param("2", value("default"))
						.object(list(value("Macro with 2 parameters"), object()
								.prop("param 1", value("$1").paramRef())
								.prop("param 2", value("$2").paramRef())
						)),
				AmongMacroDef.builder().signature("macro4", MacroType.LIST)
						.param("1").param("2", value("default")).param("3", namedList("default 2", 1, 2, 3))
						.object(list(value("Macro with 3 parameters"), object()
								.prop("param 1", value("$1").paramRef())
								.prop("param 2", value("$2").paramRef())
								.prop("param 3", value("$3").paramRef())
						))));

		list.add(recompileTest("Operator 1", new AmongOperatorDef("yo", true, OperatorType.PREFIX)));
		list.add(recompileTest("Operator 2",
				new AmongOperatorDef("yo", true, OperatorType.PREFIX),
				new AmongOperatorDef("***", false, OperatorType.BINARY),
				new AmongOperatorDef("..", false, OperatorType.BINARY, OperatorPriorities.BINARY_ACCESS)
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

	private static DynamicTest recompileTest(String name, AmongMacroDefBuilder... original){
		return DynamicTest.dynamicTest(name, () -> {
			AmongRoot root = AmongRoot.empty();
			for(AmongMacroDefBuilder v : original){
				AmongMacroDef orig = root.addMacro(v.build());
				if(orig!=null)
					throw new RuntimeException("Macro definition '"+v+"' is duplicate of '"+orig+"'");
			}
			System.out.println("========== Original ==========");
			System.out.println(root.definitionsToPrettyString());
			assertEquals(root.macros(), TestUtil.make(root.definitionsToPrettyString(), AmongRoot.empty()).macros());
		});
	}

	private static DynamicTest recompileTest(String name, AmongOperatorDef... original){
		return DynamicTest.dynamicTest(name, () -> {
			AmongRoot root = AmongRoot.empty();
			for(AmongOperatorDef v : original){
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

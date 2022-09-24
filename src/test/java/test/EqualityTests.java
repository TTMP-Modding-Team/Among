package test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ttmp.among.obj.Among;
import ttmp.among.util.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ttmp.among.obj.Among.*;

public class EqualityTests{
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
}

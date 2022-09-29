package test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ttmp.among.AmongEngine;
import ttmp.among.definition.AmongDefinition;
import ttmp.among.definition.MacroDefinition;
import ttmp.among.definition.MacroType;
import ttmp.among.obj.Among;
import ttmp.among.util.RootAndDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ttmp.among.obj.Among.*;

public class ImportTest{
	@TestFactory
	public List<DynamicTest> importTests(){
		AmongEngine engine = new AmongEngine();
		engine.addSourceProvider(path -> TestUtil.sourceFrom("import_tests", path));
		engine.addInstanceProvider(path -> {
			switch(path){
				case "provided_instance/1":{
					AmongDefinition definition = new AmongDefinition();
					definition.addMacro(MacroDefinition.builder()
							.signature("filename", MacroType.OPERATION)
							.template(value("Provided Instance #1"))
							.build());
					return new RootAndDefinition(definition);
				}
				case "provided_instance/2":{
					AmongDefinition definition = new AmongDefinition();
					definition.addMacro(MacroDefinition.builder()
							.signature("filename", MacroType.OPERATION)
							.template(value("Provided Instance #2"))
							.build());
					return new RootAndDefinition(definition);
				}
				default: return null;
			}
		});

		List<DynamicTest> list = new ArrayList<>();

		list.add(eq(engine, "defOp",
				value("+"),
				value("++"),
				value("+++"),
				namedList("+", 21),
				namedList("+", namedList("+", 1, 3), 5)));
		list.add(eq(engine, "importTest1",
				namedObject("none")
						.prop("filename", namedList("filename"))
						.prop("number", "NUMBER"),
				namedObject("1")
						.prop("filename", "import_tests/import1.among")
						.prop("number", 1),
				namedObject("2")
						.prop("filename", "import_tests/import2.among")
						.prop("number", 2)));
		list.add(eq(engine, "importTest2",
				value("coolMacro"),
				value("coolMacro"),
				value("Cool Macro"),
				value("coolMacro"),
				value("Cool Macro")));
		list.add(eq(engine, "importTest3",
				value("Provided Instance #1"),
				value("Provided Instance #2"),
				value("Provided Instance #1"),
				value("Provided Instance #2")));

		list.add(err(engine, "invalidRef"));
		list.add(err(engine, "selfRef"));
		list.add(err(engine, "circRef1"));

		return list;
	}

	private static DynamicTest eq(AmongEngine engine, String name, Among... expected){
		return DynamicTest.dynamicTest(name, () -> {
			long t = System.currentTimeMillis();
			RootAndDefinition root = engine.getOrReadFrom(name);
			t = System.currentTimeMillis()-t;
			assertNotNull(root, "Compilation failed");
			TestUtil.log(root, t);
			assertArrayEquals(expected, root.root().objects().toArray(new Among[0]));
		});
	}
	private static DynamicTest err(AmongEngine engine, String name){
		return DynamicTest.dynamicTest(name, () ->
				assertNull(engine.getOrReadFrom(name), "Cannot even fail smh smh"));
	}
}

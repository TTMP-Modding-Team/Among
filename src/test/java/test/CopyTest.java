package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ttmp.among.definition.AmongDefinition;
import ttmp.among.definition.MacroDefinition;
import ttmp.among.definition.MacroType;
import ttmp.among.definition.OperatorType;
import ttmp.among.obj.AmongRoot;

import static ttmp.among.obj.Among.*;

public class CopyTest{
	@Test
	public void copyRoot(){
		AmongRoot r1 = new AmongRoot();
		r1.addObject(value("asdfadvbvkcl'''n\";safa;f"));
		r1.addObject(namedList("asdfadvbvkcl'''n;safa;f", "1213sfadvadfcv"));

		System.out.println("========== Original ==========");
		System.out.println(r1.toPrettyString());

		AmongRoot r2 = r1.copy();
		System.out.println("========== Copy ==========");
		System.out.println(r2.toPrettyString());

		Assertions.assertEquals(r1.objects(), r2.objects());
	}
	@Test
	public void copyDefinition(){
		AmongDefinition def = new AmongDefinition();
		def.addMacro(MacroDefinition.builder()
				.signature("This is macro", MacroType.CONST)
				.template(object()
						.prop("P1", "1")
						.prop("P2", "2"))
				.build());
		def.addMacro(MacroDefinition.builder()
				.signature("Macro2", MacroType.OBJECT)
				.param("p1")
				.param("p2", value("default"))
				.param("p3")
				.template(object()
						.prop("P1", value("p1").paramRef())
						.prop("P2", value("p2").paramRef())
						.prop("P3", value("p3").paramRef()))
				.build());

		def.operators().addOperator("~~", OperatorType.PREFIX);
		def.operators().addOperator("~~", OperatorType.POSTFIX);
		def.operators().addKeyword("sus", OperatorType.POSTFIX);
		def.operators().addKeyword("x", OperatorType.BINARY);

		System.out.println("========== Original ==========");
		System.out.println(def.toPrettyString());

		AmongDefinition def2 = def.copy();
		System.out.println("========== Copy ==========");
		System.out.println(def2.toPrettyString());

		Assertions.assertEquals(def.macros(), def.macros());
		Assertions.assertEquals(def.operators(), def.operators());
	}
}

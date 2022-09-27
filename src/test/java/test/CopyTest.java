package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ttmp.among.macro.MacroDefinition;
import ttmp.among.obj.AmongRoot;
import ttmp.among.macro.MacroType;
import ttmp.among.operator.OperatorType;

import static ttmp.among.obj.Among.*;

public class CopyTest{
	@Test
	public void copyRoot(){
		AmongRoot r1 = AmongRoot.empty();
		r1.addMacro(MacroDefinition.builder()
				.signature("This is macro", MacroType.CONST)
				.template(object()
						.prop("P1", "1")
						.prop("P2", "2"))
				.build());
		r1.addMacro(MacroDefinition.builder()
				.signature("Macro2", MacroType.OBJECT)
				.param("p1")
				.param("p2", value("default"))
				.param("p3")
				.template(object()
						.prop("P1", value("$p1").paramRef())
						.prop("P2", value("$p2").paramRef())
						.prop("P3", value("$p3").paramRef()))
				.build());

		r1.operators().addOperator("~~", OperatorType.PREFIX);
		r1.operators().addOperator("~~", OperatorType.POSTFIX);
		r1.operators().addKeyword("sus", OperatorType.POSTFIX);
		r1.operators().addKeyword("x", OperatorType.BINARY);

		r1.addObject(value("asdfadvbvkcl'''n\";safa;f"));
		r1.addObject(namedList("asdfadvbvkcl'''n;safa;f", "1213sfadvadfcv"));

		System.out.println("========== Original ==========");
		System.out.println(r1.objectsAndDefinitionsToPrettyString());

		AmongRoot r2 = r1.copy();
		System.out.println("========== Copy ==========");
		System.out.println(r2.objectsAndDefinitionsToPrettyString());

		Assertions.assertEquals(r1.macros(), r2.macros());
		Assertions.assertEquals(r1.operators(), r2.operators());
		Assertions.assertEquals(r1.objects(), r2.objects());
	}
}

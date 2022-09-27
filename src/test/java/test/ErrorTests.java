package test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;

public class ErrorTests{
	@TestFactory
	public List<DynamicTest> simpleEqualityTests(){
		List<DynamicTest> list = new ArrayList<>();
		list.add(errorTest("unterminated1"));
		list.add(errorTest("unterminated2"));
		list.add(errorTest("unterminated3"));
		list.add(errorTest("expectValue1"));
		list.add(errorTest("invalidCharEscape"));
		return list;
	}

	private static DynamicTest errorTest(String name){
		return DynamicTest.dynamicTest(name, () -> TestUtil.expectError(TestUtil.sourceFrom("error_tests", name)));
	}
}

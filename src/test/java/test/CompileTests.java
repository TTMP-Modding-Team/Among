package test;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CompileTests{
	@Test public void importing() throws IOException{
		expectNoError("importing");
	}
	@Test public void macroRedef() throws IOException{
		expectWarning("macroRedef");
	}

	private static void expectNoError(String fileName) throws IOException{
		TestUtil.expectNoError(TestUtil.expectSourceFrom("compile_tests", fileName), TestUtil.ExpectWarning.NO_WARNING);
	}

	private static void expectWarning(String fileName) throws IOException{
		TestUtil.expectNoError(TestUtil.expectSourceFrom("compile_tests", fileName), TestUtil.ExpectWarning.WARNING);
	}
}

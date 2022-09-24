package test;

import ttmp.among.AmongEngine;
import ttmp.among.compile.CompileResult;
import ttmp.among.obj.Among;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.Source;

public class TestUtil{
	private static final boolean log = true;
	private static final AmongEngine engine = new AmongEngine();

	public static Among makeSingle(String src){
		return make(Source.of(src)).singleObject();
	}
	public static Among makeSingle(Source src){
		return make(src).singleObject();
	}
	public static AmongRoot make(Source src){
		long t = System.currentTimeMillis();
		CompileResult result = engine.read(src);
		t = System.currentTimeMillis()-t;
		result.printReports();

		AmongRoot root = result.expectSuccess();
		if(log){
			System.out.println("Parsed in "+t+"ms");
			System.out.println("========== Compact String ==========");
			System.out.println(root);
			System.out.println("========== Pretty String ==========");
			System.out.println(root.objectsAndDefinitionsToPrettyString());
		}
		return root;
	}
}

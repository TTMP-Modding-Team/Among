package test;

import ttmp.among.AmongEngine;
import ttmp.among.compile.CompileResult;
import ttmp.among.obj.AmongRoot;
import ttmp.among.util.Source;

public class TestUtil{
	private static final boolean log = true;
	private static final AmongEngine engine = new AmongEngine();

	public static AmongRoot make(String src){
		return make(Source.of(src));
	}
	public static AmongRoot make(Source src){
		return make(src, null);
	}
	public static AmongRoot make(String src, AmongRoot root){
		return make(Source.of(src), root);
	}
	public static AmongRoot make(Source src, AmongRoot root){
		long t = System.currentTimeMillis();
		CompileResult result = engine.read(src, root);
		t = System.currentTimeMillis()-t;
		result.printReports();

		root = result.expectSuccess();
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

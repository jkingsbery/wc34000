package net.kingsbery.wc34000.runtime;

import static org.junit.Assert.*;
import net.kingsbery.wc34000.AssemblyTree;

import org.junit.Test;

public class SampleMethodInterpreterTest {

	@Test
	public void foo() throws Exception{
		String fileName = "sampleproc.asm";
		StringBuffer buffer = AssemblyTree.readFile(fileName);
		AssemblyTree tree = new AssemblyTree(buffer.toString());
		tree.parse();
		tree.getLinesNumbers();
		tree.generate();
		
		Wc34Interpreter chip = new Wc34Interpreter(tree.getCodes());
		chip.run();
		assertEquals("16",chip.getOutput());
	}
}

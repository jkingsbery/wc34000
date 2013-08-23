package net.kingsbery.wc34000.os;

import net.kingsbery.wc34000.AssemblyTree;
import net.kingsbery.wc34000.runtime.Wc34Interpreter;

public class CowOs {

	public static void main(String args[]) throws Exception{
		String fileName = "os.asm";
		StringBuffer buffer = AssemblyTree.readFile(fileName);
		AssemblyTree tree = new AssemblyTree(buffer.toString());

		tree.parse();
		tree.getLinesNumbers();
		tree.generate();

		Wc34Interpreter chip = new Wc34Interpreter(tree.getCodes());
		chip.run();
	}
}

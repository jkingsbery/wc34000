package net.kingsbery.wc34000;

import static net.kingsbery.wc34000.Wc34Asm.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class AddToTailGenerateTest {



	@Test
	public void foo() throws IOException {
		String fileName = "addToTail.asm";
		StringBuffer buffer = AssemblyTree.readFile(fileName);
		AssemblyTree tree = new AssemblyTree(buffer.toString());
		tree.parse();
		tree.getLinesNumbers();
		tree.generate();
		List<Integer> codes = tree.getCodes().subList(2, tree.getCodes().size());
		System.out.println(codes);
		assertEquals("0001000110111100", toBinary(codes.get(0)));
		assertEquals("1111111111111111", toBinary(codes.get(1)));
		
		assertEquals("0011001100111100", toBinary(codes.get(2)));
		assertEquals("0000000001000000", toBinary(codes.get(3)));
		
		assertEquals("0100001000101110", toBinary(codes.get(4)));
		assertEquals("0000000000000010", toBinary(codes.get(5)));
		
		assertEquals("0100101110010000", toBinary(codes.get(6)));
		
		assertEquals("0111101110111100", toBinary(codes.get(8)));
		assertEquals("0000000000000000", toBinary(codes.get(9)));
		assertEquals("1111111111111111", toBinary(codes.get(10)));
		
		assertEquals("0010111000111010", toBinary(codes.get(11)));
		assertEquals("0000000000010001", toBinary(codes.get(12)));
		
		assertEquals("0100100111111100", toBinary(codes.get(13)));
		assertEquals("0000000000000010", toBinary(codes.get(14)));
		
		assertEquals("0010110000111010", toBinary(codes.get(15)));
		assertEquals("0000000000011010", toBinary(codes.get(16)));
		
		assertEquals("0100001001101110", toBinary(codes.get(17)));
		assertEquals("0000000000000010", toBinary(codes.get(18)));
		
		//move a0,(a1)
		assertEquals("0100010001001000", toBinary(codes.get(19)));
		
		assertEquals("0100101110001000", toBinary(codes.get(20)));
		assertEquals("1111111111111111", toBinary(codes.get(21)));
		
		assertEquals("0100101000101110", toBinary(codes.get(22)));
		assertEquals("0000000000000011", toBinary(codes.get(23)));
		assertEquals("0000000000000000", toBinary(codes.get(24)));
		
		//clr next(a0)
		assertEquals("0010000100101000", toBinary(codes.get(25)));
		assertEquals("0000000000000001", toBinary(codes.get(26)));
		
		assertEquals("0010101100111010", toBinary(codes.get(27)));
		assertEquals("0000000000001001", toBinary(codes.get(28)));
		
		//elsepart
		assertEquals("0100100111101110", toBinary(codes.get(29)));
		assertEquals("0000000000000011", toBinary(codes.get(30)));
		
		assertEquals("0100001000101110", toBinary(codes.get(31)));
		assertEquals("1111111111111111", toBinary(codes.get(32)));
		
		assertEquals("0010101000101000", toBinary(codes.get(33)));
		assertEquals("0000000000000001", toBinary(codes.get(34)));
		
		assertEquals("0010110000111010", toBinary(codes.get(35)));
		assertEquals("1111111111011100", toBinary(codes.get(36)));
		
		
		assertEquals("0011010000111100", toBinary(codes.get(37)));
		assertEquals("0000000001000000", toBinary(codes.get(38)));
		
		assertEquals("0010100100001110", toBinary(codes.get(39)));
		
		assertEquals("0010100000111000", toBinary(codes.get(40)));
		assertEquals("0000000000000010", toBinary(codes.get(41)));
		
		
	}

	
}

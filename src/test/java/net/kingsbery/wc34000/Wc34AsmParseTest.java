package net.kingsbery.wc34000;

import static org.junit.Assert.*;

import java.util.HashMap;

import net.kingsbery.wc34000.Wc34Asm.AddressMode;
import net.kingsbery.wc34000.Wc34Asm.ExtensionOperand;
import net.kingsbery.wc34000.Wc34Asm.Instruction;
import net.kingsbery.wc34000.Wc34Asm.RegisterDirectOperand;

import org.junit.Test;

public class Wc34AsmParseTest {

	@Test
	public void move() {
		AssemblyTree tree = new AssemblyTree("MOVE D0,D1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals("MOVE", result.inst);
	}
	
	@Test
	public void registerDirectOperandMode() {
		AssemblyTree tree = new AssemblyTree("ADD D0,D1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(AddressMode.DATA_REGISTER_DIRECT, result.b.getAddressMode());
	}

	@Test
	public void registerDirect() {
		AssemblyTree tree = new AssemblyTree("ADD D0,D1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(1, ((RegisterDirectOperand)result.a).getRegister());
	}
	
	@Test
	public void add() {
		AssemblyTree tree = new AssemblyTree("ADD D0,D1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals("ADD", result.inst);
	}

	@Test
	public void link() {
		AssemblyTree tree = new AssemblyTree("LINK\ta6,#-1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals("LINK", result.inst);
	}
	
	@Test
	public void pushreg(){
		AssemblyTree tree = new AssemblyTree("pshreg #64 ; bit 6=a0\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals("PSHREG", result.inst);
	}

	@Test
	public void pushregOperandMode(){
		AssemblyTree tree = new AssemblyTree("pshreg #64 ; bit 6=a0\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(AddressMode.IMMEDIATE, result.a.getAddressMode());
	}
	
	@Test
	public void pushregOperandValue(){
		AssemblyTree tree = new AssemblyTree("pshreg #64 ; bit 6=a0\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(64, ((ExtensionOperand)result.a).getExtension(new HashMap<String,Integer>(), 0));
	}
	
	@Test
	public void linkOpMode1() {
		AssemblyTree tree = new AssemblyTree("LINK\ta6,#-1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(AddressMode.ADDRESS_REGISTER_DIRECT, result.b.getAddressMode());
	}
	
	@Test
	public void linkOpMode2() {
		AssemblyTree tree = new AssemblyTree("LINK\ta6,#-1\n");
		tree.parse();
		Instruction result = tree.instructions.get(0);
		assertEquals(AddressMode.IMMEDIATE, result.a.getAddressMode());
	}

}

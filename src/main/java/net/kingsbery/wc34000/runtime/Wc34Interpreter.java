package net.kingsbery.wc34000.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.kingsbery.wc34000.AssemblyTree.NotYetImplementedException;

public class Wc34Interpreter {

//	private List<Integer> codes;
	private StringBuffer buffer = new StringBuffer();

	public Wc34Interpreter(List<Integer> list) {
//		this.codes = list;
		for(int i=0; i<list.size(); i++){
			memory[i]=list.get(i);
		}
		registers[SP] = memory.length;
		registers[FP] = memory.length;
	}

	public static void main(String args[]) throws Exception {

	}

	int pc;
	int registers[] = new int[16];
	int[] memory = new int[1<<16];
	boolean halted;
	private boolean zbit=false;
	private boolean nbit=false;

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface OpCode {

		int value();
	}

	private Method getMethod(int code) {
		for (Method method : this.getClass().getDeclaredMethods()) {
			OpCode annotation = method.getAnnotation(OpCode.class);
			if (annotation != null && annotation.value() == code) {
				System.out.println(method.getName());
				return method;
			}
		}
		throw new IllegalArgumentException("Line " + pc
				+ ": No instruction for " + code);
	}

	public void run() throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		pc = memory[0];
//		System.out.println(codes);
		while (!halted) {
			System.out.println("PC: " + pc+", SP: " + registers[SP] + ", FP: " +registers[SP-1] );
			int currentCode = currentCode();
			try {
				if (currentCode == 0) {
					halted = true;
				} else if (currentCode >> 9 == 8) {
					link(currentCode);
				} else if (currentCode >> 14 == 0) {
					Operand op = getLowOperand(currentCode);
					getMethod(currentCode >> 8).invoke(this,op);
				} else {
					// Two Operand
					Operand lowOperand = getLowOperand(currentCode);
					Operand highOperand = getHighOperand(currentCode);
					getMethod(currentCode >> 12).invoke(this,lowOperand,highOperand);
				}
			} catch (Exception e) {
				throw new RuntimeException("Problem with opcode " + currentCode
						+ " at " + pc, e);
			}
			pc++;
//			if(registers[SP]<codes.size()){
//				throw new RuntimeException("Problem at " + pc);
//			}
		}
	}

	private void link(int currentCode) {
		printStack();
		int opcode = currentCode;
		int register = getLinkRegister(opcode);
		Operand dest = getLowOperand(opcode);
		registers[SP]--;
		memory[registers[SP]]=registers[register];
		registers[register] = registers[SP];
		registers[SP] += dest.getValue();
	}

	private void printStack() {
		System.out.println("Stack pointer: " + registers[SP]);
		for(int i=registers[SP]; i<memory.length; i++){
			System.out.println(i+": " + memory[i]);
		}
	}

	public int getLinkRegister(int opcode) {
		return ((opcode >> 6) & 7) + 8;
	}

	// Always returns the lowest 6 bits
	public Operand getLowOperand(int opcode) {
		int x = opcode & 63;
		int mode = x >> 3;
		int reg = x & 7;
		return getOperand(x, mode, reg);
	}

	public Operand getHighOperand(int opcode) {
		int x = (opcode >> 6) & 63;
		int mode = x >> 3;
		int reg = x & 7;
		return getOperand(x, mode, reg);
	}

	private Operand getOperand(int x, int mode, int reg) {
		if (mode == 0 || mode == 1) {
			return new RegisterOperand(x);
		} else if (mode == 7 && reg == 4) {
			pc++;
			ImmediateOperand result = new ImmediateOperand(currentCode());
			return result;
		} else if (mode == 2) {
			return new AddressRegIndirectOperand(reg);
		} else if (mode == 7 && reg == 0) {
			pc++;
			return new AbsoluteOperand(currentCode());
		} else if (mode == 7 && reg == 2) {
			pc++;
			return new ProgramCounterIndirectOperand(currentCode());
		} else if(mode==5){
			pc++;
			return new AddressRegisterIndirectDisplacementOperand(reg + 8,currentCode());
		}else if (mode == 4) {
			return new AddressRegisterIndirectPreDecrementOperand(reg + 8);
		} else {
			throw new NotYetImplementedException("Not yet implemented: " + x);
		}
	}

	private static interface Operand {

		public int getValue();

		public void set(int value);

	}

	private class RegisterOperand implements Operand {

		private int reg;

		public RegisterOperand(int x) {
			this.reg = x;
		}

		@Override
		public int getValue() {
			return registers[reg];
		}

		@Override
		public void set(int value) {
			registers[reg] = value;

		}

	}

	private class ImmediateOperand implements Operand {

		private int value;

		public ImmediateOperand(int value) {
			this.value = value;
		}

		@Override
		public int getValue() {
			return this.value;
		}

		@Override
		public void set(int value) {
			throw new IllegalArgumentException(
					"Cannot set an immediate operand");
		}

	}

	private class AbsoluteOperand implements Operand {

		private int location;

		public AbsoluteOperand(int value) {
			this.location = value;
		}

		@Override
		public int getValue() {
			return memory[this.location];
		}

		@Override
		public void set(int value) {
			memory[this.location] = value;
		}

	}

	private class AddressRegIndirectOperand implements Operand {

		private int register;

		public AddressRegIndirectOperand(int value) {
			this.register = value;
		}

		@Override
		public int getValue() {
			return memory[registers[this.register]];
		}

		@Override
		public void set(int value) {
			memory[registers[this.register]] = value;
		}

	}

	private class AddressRegisterIndirectPreDecrementOperand implements Operand {

		private int register;

		public AddressRegisterIndirectPreDecrementOperand(int value) {
			this.register = value;
		}

		@Override
		public int getValue() {
			registers[this.register]--;
			if (registers[this.register] < 0) {
				throw new RuntimeException("Can't decrement register A"
						+ this.register);
			}
			return memory[registers[this.register]];
		}

		@Override
		public void set(int value) {
			registers[this.register]--;
			memory[registers[this.register]] = value;
		}

	}

	private class AddressRegisterIndirectDisplacementOperand implements Operand {

		private int register;
		private int displacement;

		public AddressRegisterIndirectDisplacementOperand(int value,int displacement) {
			this.register = value;
			this.displacement=displacement;
		}

		@Override
		public int getValue() {
			return memory[registers[this.register]+displacement];
		}

		@Override
		public void set(int value) {
			memory[registers[this.register]+displacement] = value;
		}

	}

	
	private class ProgramCounterIndirectOperand implements Operand {

		private int displacement;

		public ProgramCounterIndirectOperand(int displacement) {
			this.displacement = displacement;
		}

		@Override
		public int getValue() {
			return this.displacement + pc;
		}

		// Should not really ever happen.
		@Override
		public void set(int value) {
			memory[this.displacement + pc] = value;
		}

	}

	@OpCode(4)
	public void move(Operand lowOperand,Operand highOperand) {
		highOperand.set(lowOperand.getValue());
	}

	@OpCode(5)
	public void add(Operand lowOperand,Operand highOperand) {
		highOperand.set(lowOperand.getValue()+highOperand.getValue());
	}
	
	@OpCode(6)
	public void sub(Operand lowOperand,Operand highOperand) {
		highOperand.set(highOperand.getValue()-lowOperand.getValue());
	}
	
	@OpCode(8)
	public void muls(Operand lowOperand,Operand highOperand) {
		highOperand.set(lowOperand.getValue()*highOperand.getValue());
	}
	
	@OpCode(8+2)
	public void and(Operand lowOperand,Operand highOperand) {
		highOperand.set(lowOperand.getValue() & highOperand.getValue());
	}

	@OpCode(8+4+2)
	public void asl(Operand lowOperand,Operand highOperand) {
		highOperand.set(highOperand.getValue() << lowOperand.getValue());
	}
	
	
	@OpCode(8+4+2+1)
	public void asr(Operand lowOperand,Operand highOperand) {
		highOperand.set(highOperand.getValue() >> lowOperand.getValue());
	}
	
	
	
	@OpCode(32 + 4)
	public void outnum(Operand op) {
		buffer.append(op.getValue());
		System.out.println("OUTNUM: " + op.getValue());
	}

	private static int SP = 15;
	private static int FP = 14;
	
	@OpCode(7)
	public void cmp(Operand lowOperand,Operand highOperand){
		this.zbit=highOperand.getValue()==lowOperand.getValue();
		this.nbit=highOperand.getValue()-lowOperand.getValue()<0;
	}
	
	@OpCode(4 + 8 + 32)
	public void jsr(Operand op) {
		registers[SP]--;
		memory[registers[SP]] = pc;
		pc = op.getValue();
		pc--; // we'll pc++ after this, so this compensates.
	}

	@OpCode(1+2+8+32)
	public void jmp(Operand op){
		pc=op.getValue();
		pc--;
	}
	
	@OpCode(1+4+8+32)
	public void beq(Operand op){
		if(zbit){
			pc=op.getValue();
			pc--;
		}
	}
	
	@OpCode(1 + 8 + 32)
	public void unlk(Operand dest) {
		printStack();
		registers[SP] = dest.getValue();
		dest.set(memory[registers[SP]]);
		registers[SP]++;

	}

	@OpCode(8 + 32)
	public void rtd(Operand dest) {
		pc = memory[registers[SP]];
		System.out.println("POPPED: " + pc);
		registers[SP] += 1 + dest.getValue();
	}

	private Integer currentCode() {
		return memory[pc];
	}

	public String getOutput() {
		return buffer.toString();
	}

}

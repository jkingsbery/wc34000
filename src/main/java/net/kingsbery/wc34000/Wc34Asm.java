package net.kingsbery.wc34000;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kingsbery.wc34000.AssemblyTree.NotYetImplementedException;

//Needed Components:
//(1) Parse tree
//(2) 
//(3) Code Generator

public class Wc34Asm {

	protected static List<String> directives=new ArrayList<String>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		add("EQU");
		add("GLOBAL");
		add("STAB");
	}};

	public static void main(String args[]) {

	}

	public static final Map<String, Integer> opcodes = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("MOVE", 4);
			put("ADD",5);
			put("SUB",6);
			put("CMP",7);
			put("MULS",8);
			put("DIVS",9);
			put("AND",10);
			put("OR",11);
			put("EOR",12);
			put("LEA",13);
			put("ASL",14);
			put("ASR",15);
			put("CLR",33);
			put("NEG",34);
			put("NOT",35);
			put("OUTNUM",36);
			put("GETNUM",37);
			put("OUTCH",38);
			put("GETCH",39);
			put("RTD",40);
			put("UNLK",41);
			put("PEA",42);
			put("JMP",43);
			put("JSR",44);
			put("BEQ",45);
			put("BNE",46);
			put("BLT",47);
			put("BGT",48);
			put("BGE",49);
			put("BLE",50);
			put("PSHREG", 51);
			put("POPREG",52);
		}
	};

	public static final List<String> twoOperands = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("MOVE");
			add("ADD");
			add("SUB");
			add("CMP");
			add("MULS");
			add("DIVS");
			add("AND");
			add("OR");
			add("EOR");
			add("LEA");
			add("ASL");
			add("ASR");
		}
	};

	public static enum AddressMode {
		DATA_REGISTER_DIRECT(0), ADDRESS_REGISTER_DIRECT(1), ADDRESS_REGISTER_INDIRECT(
				2), ADDRESS_REGISTER_INDIRECT_POSTINCREMENT(3), ADDRESS_REGISTER_INDIRECT_PREDECREMENT(
				4), ADDRESS_REGISTER_INDIRECT_DISPLACEMENT(5), ABSOLUTE(7), PC_INDIRECT_DISPLACEMENT(
				7), IMMEDIATE(7);

		private int code;

		private AddressMode(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	public static class Instruction {
		String label;
		String inst;
		Operand a=Wc34Asm.NONE;
		Operand b=Wc34Asm.NONE;
		private int address;

		public boolean twoOperands() {

			return twoOperands.contains(inst);
		}

		public boolean isLink() {
			return inst.toUpperCase().equals("LINK");
		}

		public void setAddress(int address) {
			this.address=address;
		}

		public int getAddress() {
			return address;
		}

		public boolean isHalt() {
			return inst.toUpperCase().equals("HALT");
		}

	}

	public static final Operand NONE = new Operand(){

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.DATA_REGISTER_DIRECT;
		}

		@Override
		public int generate() {
			throw new IllegalArgumentException("Placeholder Operand should never be generated.");
			
		}};
		
	public static interface Operand{
		
		AddressMode getAddressMode();
		int generate();
	}
	
	public static class LabelOperand implements Operand,ExtensionOperand{

		private String token;
		private Integer labelLocation;
		private AddressMode addressMode;
		
		
		public LabelOperand(String token) {
			this.token=token;
		}
		
		public void update(Map<String, Integer> integerConstants, Map<String, Integer> labelLocations){
			if(labelLocations.keySet().contains(token)){
				this.labelLocation=labelLocations.get(token);
				this.addressMode=AddressMode.PC_INDIRECT_DISPLACEMENT;
			}			
		}

		@Override
		public AddressMode getAddressMode() {
			return addressMode;
		}

		@Override
		public int generate() {
			if(addressMode==AddressMode.PC_INDIRECT_DISPLACEMENT){
				return (7<<3)+2;
			}else{
				throw new NotYetImplementedException("Token: " + token);
			}
		}

		@Override
		public int getExtension(Map<String, Integer> map,int pc) {
			if(addressMode==AddressMode.PC_INDIRECT_DISPLACEMENT){
				return labelLocation-(pc+1);
			}else{
				throw new NotYetImplementedException();
			}
		}
		
	}
	
	public static interface ExtensionOperand{

		int getExtension(Map<String,Integer> map, int pc);
		
	}

	public static class AbsoluteOperand implements Operand,ExtensionOperand{

		private int extension;

		public AbsoluteOperand(int parseInt) {
			extension = parseInt;
		}

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.ABSOLUTE;
		}

		@Override
		public int generate() {
			return (7<<3);
		}

		@Override
		public int getExtension(Map<String,Integer> map,int pc) {
			return extension;
		}
		
	}
	
	public static class ImmediateOperand implements Operand,ExtensionOperand{

		private int extension;

		public ImmediateOperand(int parseInt) {
			extension = parseInt;
		}

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.IMMEDIATE;
		}

		@Override
		public int generate() {
			return 60;
		}

		@Override
		public int getExtension(Map<String,Integer> map,int pc) {
			return extension;
		}
		
	}
	
	public static class RegisterDirectOperand implements Operand{

		private int register;

		public RegisterDirectOperand(int i) {
			register = i;
		}

		public int getRegister() {
			return this.register;
		}

		@Override
		public AddressMode getAddressMode() {
			return register>=8 ? AddressMode.ADDRESS_REGISTER_DIRECT : AddressMode.DATA_REGISTER_DIRECT;
		}

		@Override
		public int generate() {
			return register;
		}
		
	}
	
	public static class IndirectDisplacementOperand implements Operand,ExtensionOperand{

		private int register;
		private String label;

		public IndirectDisplacementOperand(int i, String string) {
			this.register=i;
			this.label=string;
		}

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.ADDRESS_REGISTER_INDIRECT_DISPLACEMENT;
		}

		@Override
		public int generate() {
			return (5<<3)+register;
		}

		@Override
		public int getExtension(Map<String,Integer> map,int pc) {
			assert map!=null;
			assert label!=null;
			return map.get(label);
		}
		
	}

	
	public static class AddressRegisterIndirectOperand implements Operand{

		private int register;

		public AddressRegisterIndirectOperand(int i) {
			this.register=i;
		}

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.ADDRESS_REGISTER_INDIRECT;
		}

		@Override
		public int generate() {
			return (2<<3)+register;
		}
		
	}
	
	public static class AddressRegisterIndirectPreDecrementOperand implements Operand{

		private int register;

		public AddressRegisterIndirectPreDecrementOperand(int i) {
			this.register=i;
		}

		@Override
		public AddressMode getAddressMode() {
			return AddressMode.ADDRESS_REGISTER_INDIRECT_PREDECREMENT;
		}

		@Override
		public int generate() {
			return (4<<3)+register;
		}
		
	}

	
	public static Instruction assembleLine(String line) {
		Instruction result = new Instruction();
		String parts[] = line.split("\\s+");
		result.inst = parts[0];
		if (parts[1].contains(",")) {
			String operands[] = parts[1].split(",");
			result.a = parseOp(operands[0].trim());
			result.b = parseOp(operands[1].trim());
		} else {
			result.a = parseOp(parts[1].trim());
			result.b = NONE;
		}
		return result;
	}

	private static Operand parseOp(String string) {
		if (string.startsWith("#")) {
			return new ImmediateOperand(Integer.parseInt(string
					.substring(1)));
		} else if (string.matches("D[0-7]")) {
			return new RegisterDirectOperand(Integer.parseInt(string.substring(1)));
		} else {
			return new RegisterDirectOperand(
					Integer.parseInt(string.substring(1)));
		}
	}

	public static List<Integer> generate(Instruction inst) {
		return generate(inst,Collections.<String,Integer>emptyMap());
	}
	
	public static List<Integer> generate(Instruction inst,Map<String,Integer> map) {
		assert inst.a!=null;
		
		List<Integer> result = new ArrayList<Integer>();
		
		if (inst.isLink()) {
			result.add( (8 << 9) + ((inst.b.generate()-8) << 6) + inst.a.generate());
		}else  if(inst.isHalt()){
			result.add(0);
		}
		else if (!inst.twoOperands()) {
			assert opcodes.containsKey(inst.inst) : "Instruction " + inst.inst
					+ " not found.";
			int opcode = opcodes.get(inst.inst);

			result.add((opcode << 8) + inst.a.generate());
		} else {
			assert opcodes.containsKey(inst.inst) : "Instruction " + inst.inst
					+ " not found.";
			int opcode = opcodes.get(inst.inst);
			int amode = inst.a.generate();
			int bmode = inst.b.generate();
			result.add((opcode << 12) + (amode<<6)
					+ bmode);

		}
		if(inst.b instanceof ExtensionOperand){
			result.add(((ExtensionOperand)inst.b).getExtension(map,inst.getAddress()));
		}
		if(inst.a instanceof ExtensionOperand){
			result.add(((ExtensionOperand)inst.a).getExtension(map,inst.getAddress()));
		}
		return result;
	}

	public static String toBinary(int x) {

		String result = Integer.toBinaryString(x);
		while (result.length() < 16) {
			result = "0" + result;
		}
		if (x < 0) {
			result=result.substring(16);
		}
		return result;
	}
	
}

package net.kingsbery.wc34000;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kingsbery.wc34000.Wc34Asm.AbsoluteOperand;
import net.kingsbery.wc34000.Wc34Asm.AddressRegisterIndirectOperand;
import net.kingsbery.wc34000.Wc34Asm.AddressRegisterIndirectPreDecrementOperand;
import net.kingsbery.wc34000.Wc34Asm.ExtensionOperand;
import net.kingsbery.wc34000.Wc34Asm.ImmediateOperand;
import net.kingsbery.wc34000.Wc34Asm.IndirectDisplacementOperand;
import net.kingsbery.wc34000.Wc34Asm.Instruction;
import net.kingsbery.wc34000.Wc34Asm.LabelOperand;
import net.kingsbery.wc34000.Wc34Asm.Operand;
import net.kingsbery.wc34000.Wc34Asm.RegisterDirectOperand;

public class AssemblyTree {

	private int current = 0;

	public AssemblyTree(String str) {
		program = str;
	}

	public static class UnexpectedTokenException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnexpectedTokenException(String string) {
			super(string);
		}

	}

	public Operand matchOperand(String str) {
		String token = str.toUpperCase();
		if (token.matches("#[-]{0,1}[0-9]+")) {
			return new ImmediateOperand(Integer.parseInt(token.substring(1)));
		}else if(token.matches("[-]{0,1}[0-9]+")){
			return new AbsoluteOperand(Integer.parseInt(token));
		}
		else if (token.matches("D[0-7]")) {
			return new RegisterDirectOperand(Integer.parseInt(token
					.substring(1)));
		} else if (token.matches("A[0-7]")) {
			return new RegisterDirectOperand(Integer.parseInt(token
					.substring(1)) + 8);
		} else if (token.matches("\\(A[0-7]\\)")) {
			return new AddressRegisterIndirectOperand(Integer.parseInt(token
					.substring(2, token.length() - 1)));
		} else if (token.matches("([A-Za-z]+)\\(A[0-7]\\)")) {
			String[] parts = token.split("\\(");
			return new IndirectDisplacementOperand(Integer.parseInt(parts[1]
					.substring(1, parts[1].length() - 1)), parts[0]);
		} else if (token.matches("-\\(A[0-7]\\)") || token.matches("-\\(SP\\)")) {
			if (token.matches("-\\(A[0-7]\\)")) {
				return new AddressRegisterIndirectPreDecrementOperand(
						Integer.parseInt(token.substring(3, token.length() - 1)));
			} else {
				return new AddressRegisterIndirectPreDecrementOperand(7);
			}
		} else if (token.matches("[A-Za-z][A-Za-z0-9]+")) {
			return new LabelOperand(token);
		} else {
			throw new UnexpectedTokenException("Expected operand but found: "
					+ token);
		}
	}

	Instruction currentInstruction = new Instruction();

	public static class NotYetImplementedException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NotYetImplementedException(){}
		
		public NotYetImplementedException(String string) {
			super(string);
		}
	}

	public interface State {
		public State processToken(String str);
	}

	// public static enum State {
	public class LINE_START implements State {

		@Override
		public State processToken(String str) {
			String token = str.toUpperCase();
			if (isLabel(token)) {
				currentInstruction.label = token.replace(":", "");
				return new COMMAND();
			} else if (token.equals("HALT")) {
				currentInstruction.inst=token;
				return new NEW_LINE();
			} else if (token.equals("LINK")) {
				currentInstruction.inst = token;
				return new TWO_OPERANDS();
			} else if (Wc34Asm.opcodes.keySet().contains(token)) {
				currentInstruction.inst = token;
				if (Wc34Asm.twoOperands.contains(token)) {
					return new TWO_OPERANDS();
				} else {
					currentInstruction.b = Wc34Asm.NONE;
					return new ONE_OPERAND();
				}
			} else if (token.trim().isEmpty()) {
				return this;
			} else {
				throw new UnexpectedTokenException(
						"Label or Operation but found: " + token);
			}
		}
	}

	public class COMMENT implements State {
		@Override
		public State processToken(String token) {
			if (endsLine(token)) {
				return new LINE_START();
			} else {
				return this;
			}

		}
	}

	public class COMMAND implements State {
		@Override
		public State processToken(String str) {
			String token = str.toUpperCase();
			if (token.trim().isEmpty()) {
				// Sometimes, there is a new line between the label and the
				// opcode
				return this;
			} else if (token.equals("LINK")) {
				currentInstruction.inst = token;
				return new TWO_OPERANDS();
			} else if (Wc34Asm.opcodes.keySet().contains(token)) {
				currentInstruction.inst = token;
				if (Wc34Asm.twoOperands.contains(token)) {
					return new TWO_OPERANDS();
				} else {
					return new ONE_OPERAND();
				}
			} else if ("EQU".equals(token)) {
				currentInstruction.inst = token;
				return new ONE_OPERAND();
			} else {
				throw new UnexpectedTokenException(
						"Expected opcode or directive but found " + token);
			}
		}
	}

	public class TWO_OPERANDS implements State {
		@Override
		public State processToken(String str) {
			String token = str.toUpperCase();
			currentInstruction.b = matchOperand(token);
			return new COMMA();
		}

	}

	public class ONE_OPERAND implements State {
		@Override
		public State processToken(String token) {
			currentInstruction.a = matchOperand(token);
			return new NEW_LINE();
		}
	}

	public class NEW_LINE implements State {
		@Override
		public State processToken(String token) {
			if (token.equals("\n")) {
				instructions.add(currentInstruction);
				currentInstruction = new Instruction();
				return new LINE_START();
			} else {
				throw new UnexpectedTokenException(
						"Expected new line, but found " + token);
			}
		}
	}

	public class COMMA implements State {
		@Override
		public State processToken(String token) {
			if (token.equals(",")) {
				return new ONE_OPERAND();
			} else {
				throw new UnexpectedTokenException("Expected \",\" but found "
						+ token);
			}
		}
	}

	public static void main(String args[]) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				AssemblyTree.class.getClassLoader().getResourceAsStream(
						"addToTail.asm")));
		StringBuffer buffer = new StringBuffer();
		String str;
		while ((str = reader.readLine()) != null) {
			buffer.append(str + "\n");
		}
		AssemblyTree tree = new AssemblyTree(buffer.toString());
		tree.parse();
	}

	State state;
	private String program;

	List<Instruction> instructions = new ArrayList<Instruction>();
	private List<Integer> codes;

	public void parse() {
		state = new LINE_START();
		while (!this.end()) {
			String token = this.nextToken();
			state = state.processToken(token);
		}
	}

	private static boolean endsLine(String token) {
		return token.contains("\n");
	}

	private static boolean startsComment(String token) {
		return token.startsWith(";");
	}

	private static boolean isLabel(String token) {
		return token.matches("[A-Za-z]+:");
	}

	private boolean end() {
		return current == program.length();
	}

	
	//TODO: Handle quotes properly. (Easiest is probably to handle entire quote as a single token.)
	private String nextToken() {
		// Continue to read from buffer until you hit character that denotes end
		// of token
		int start = current;
		// First, read off any non-new-line white space
		while (program.charAt(start) == ' ' || program.charAt(start) == '\t') {
			start++;
		}
		List<Character> endChars = new ArrayList<Character>() {
			{
				add(',');
				add(' ');
				add('\n');
				add('\t');
			}
		};
		// First, if it's a comment, keep reading until the next line
		if (program.charAt(start) == ';') {
			while (program.charAt(start) != '\n') {
				start++;
			}
			// start++;
		}
		int end = start;
		if (start == program.length()) {
			current = program.length();
			return "\n";
		}
		// If it's punctuation or new line
		if (endChars.contains(program.charAt(start))) {
			end++;
		} else
			while (!endChars.contains(program.charAt(end))) {
				end++;
			}
		current = end;
		return program.substring(start, end);
	}

	Map<String,Integer> integerConstants = new HashMap<String,Integer>();
	private Map<String,Integer> labelLocations=new HashMap<String,Integer>();
	
	public void generate() {
		this.codes = new ArrayList<Integer>();
		codes.add(2);
		codes.add(60000);//TODO implement Global variable area
		for (Instruction inst : instructions){
			if (inst.inst.equals("EQU")) {
				integerConstants.put(inst.label,((ImmediateOperand)inst.a).getExtension(integerConstants,inst.getAddress()));
			} else {
				codes.addAll(Wc34Asm.generate(inst,integerConstants));
			}
		}

	}

	public List<Integer> getCodes() {
		return this.codes;
	}

	public void getLinesNumbers() {
		this.codes = new ArrayList<Integer>();
		int address=2;
		for (Instruction inst : instructions) {
			if (inst.inst.equals("EQU")) {
				//skip
			} else {
				inst.setAddress(address);
				if(inst.label!=null){
					this.labelLocations.put(inst.label,inst.getAddress());
				}
				address++;
				if(inst.b instanceof ExtensionOperand){
					address++;
				}
				if(inst.a instanceof ExtensionOperand){
					address++;
				}
			}
		}
		for (Instruction inst : instructions) {
			if(inst.a instanceof LabelOperand){
				((LabelOperand)inst.a).update(integerConstants, labelLocations);
			}
			if(inst.b instanceof LabelOperand){
				((LabelOperand)inst.b).update(integerConstants, labelLocations);
			}
		}
	}
	
	public static StringBuffer readFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				AssemblyTree.class.getClassLoader().getResourceAsStream(
						fileName)));
		StringBuffer buffer = new StringBuffer();
		String str;
		while ((str = reader.readLine()) != null) {
			buffer.append(str + "\n");
		}
		return buffer;
	}
}

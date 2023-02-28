import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CPU {
	private Variable[] ram;
	private LinkedList<Integer> ReadyQueue;
	private int program_space;
	private int pc;
	private int lower_bound;
	private int upper_bound;

	public CPU(LinkedList<String> programs) throws Exception {
		ram = new Variable[3000];
		ReadyQueue = new LinkedList<Integer>();
		program_space = ram.length / programs.size(); // 3000//3 = 1000
		pc = 0;
		lower_bound = 0;
		upper_bound = 0;
		initMemory(programs); 
	}

	public void initMemory(LinkedList<String> programs) throws Exception {
		for (int i = 0; i < programs.size(); i++) { //programs.size =3
			String path = "src/" + programs.get(i) + ".txt";
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			int ramword = i * program_space; // first round = 0*1000 //1*1000 //2*1000
			int lower_bound = ramword; //0 //1000 //2000
			int upper_bound = ramword + program_space - 1; //999  //1999  //2999
			System.out.println(
					"-------------------------------------\nThe following is the PCB created for process with ID " + i);
			for (int j = ramword; j < ramword + 5; j++) {
				int mod = j % 5;
				ram[j] = mod == 0 ? new Variable("ID", i + "")
						: mod == 1 ? new Variable("State", "Ready")
								: mod == 2 ? new Variable("PC", ramword + 5 + "")
										: mod == 3 ? new Variable("Lower boundary", lower_bound + "")
												: new Variable("Upper boundary", upper_bound + "");
				System.out.println("Memory word #" + j + " currently has (" + ram[j].key + ": " + ram[j].value + ")"); //printing entire pcb 
			}
			ramword += 5;
			System.out.println(
					"-------------------------------------\nThe following are the lines of codes created for the process with ID "
							+ i);
			while ((line = br.readLine()) != null) {
				if (ramword > upper_bound) {
					throw new Exception("The memory boundary for process" + " " + i + " has been surpassed ");
				} else {
					System.out.println("Memory word #" + ramword + " currently has (Inst:" + " " + line + ")");
					ram[ramword++] = new Variable("Inst", line);
				}

			}
			ReadyQueue.add(i);
		}
	}

	public void Schedule() throws Exception {
		if (ReadyQueue.isEmpty()) {
			return;
		}
		int process_id = ReadyQueue.remove();
		int process_lower = process_id * program_space;
		lower_bound = process_lower;
		upper_bound = lower_bound + program_space - 1;
		if (!ram[process_lower + 1].value.equals("Ready")) {
			throw new Exception("Error, Expected ready state");
		}
		System.out.println(
				"-------------------------------------\nThe Currently running process in the CPU is process with ID "
						+ process_id);
		ram[process_lower + 1].value = "Running";
		System.out.println("Memory word #" + (process_lower + 1) + " has been written and currently has ("
				+ ram[process_lower + 1].key + ": " + ram[process_lower + 1].value + ")");
		pc = Integer.parseInt(ram[process_lower + 2].value);
		System.out.println("Memory word #" + (process_lower + 2) + " has been read and currenly has ("
				+ ram[process_lower + 2].key + ": " + ram[process_lower + 2].value + ")");

		for (int i = 0; i < 2; i++) {
			System.out.println("Memory word #" + pc + " has been read and currently has (" + ram[pc].key + ": "
					+ ram[pc].value + ")");
			Execute(ram[pc].value);
			if (ram[pc + 1] == null || !ram[pc + 1].key.equals("Inst")) {
				System.out.println("This process took " + (i + 1) + " Quanta in the CPU");
				ram[process_lower + 1].value = "Finshed";
				System.out.println("Memory word #" + (process_lower + 1) + " has been written and currently has ("
						+ ram[process_lower + 1].key + ": " + ram[process_lower + 1].value + ")");
				Schedule();
				return;
			}

			pc++;
		}
		System.out.println("This process took 2 Quanta in the CPU");

		ReadyQueue.add(process_lower / program_space);
		ram[process_lower + 1].value = "Ready";
		ram[process_lower + 2].value = pc + "";
		System.out.println("Memory word #" + (process_lower + 1) + " has been written and currently has ("
				+ ram[process_lower + 1].key + ": " + ram[process_lower + 1].value + ")");
		System.out.println("Memory word #" + (process_lower + 2) + " has been written and currently has ("
				+ ram[process_lower + 2].key + ": " + ram[process_lower + 2].value + ")");
		Schedule();

	}

	public void Execute(String line) throws Exception {
		Stack<String> stack = new Stack<String>();
		StringTokenizer st = new StringTokenizer(line);
		while (st.hasMoreTokens())
			stack.push(st.nextToken());
		while (!stack.isEmpty()) {
			String parameter2 = stack.pop();
			if (stack.isEmpty()) {
				// System.out.println(parameter2);
				break;
			}
			String parameter1 = stack.pop();
			if (parameter1.equals("print")) {
				print(parameter2);
				continue;
			}
			if (parameter1.equals("readFile")) {
				stack.push(readFile(parameter2));
				continue;
			}
			String inst = stack.pop();
			if (inst.equals("assign")) {
				assign(parameter1, parameter2);
				continue;
			}
			if (inst.equals("add")) {
				stack.push(add(parameter1, parameter2));
				continue;
			}
			if (inst.equals("writeFile")) {
				writeFile(parameter1, parameter2);
				continue;
			}
			throw new IOException("undefined instruction");
		}

	}

	private String SearchinMemory(String variable) {
		String res = null;
		int i = 0;
		for (i = lower_bound+5; i <= upper_bound; i++) {
			if (ram[i] != null && ram[i].key.equals(variable)) {
				res = ram[i].value;
				break;
			}
		}
		if (res != null) {
			System.out.println("Memory word #" + i + " has been read and currently has (" + ram[i].key + " : "
					+ ram[i].value + ")");
		}
		return res;
	}

	private void AddinMemory(String variable, String value) throws Exception {
		boolean flag = false;
		int i = 0;
		for (i = lower_bound+5; i <= upper_bound; i++) {
			if (ram[i] != null && ram[i].key.equals(variable)) {
				ram[i].value = value;
				flag = true;
				break;
			} else if (ram[i] == null) {
				ram[i] = new Variable(variable, value);
				flag = true;
				break;
			}
		}
		System.out.println("Memory word #" + i + " has been written and currently has (" + ram[i].key + " : "
				+ ram[i].value + ")");

		if (!flag)
			throw new Exception("There is no space in ram to store this variable");
	}

	private String readFile(String path) throws IOException {
		String search_path = SearchinMemory(path);
		if (search_path != null) {
			path = search_path;
		}
		path = "src/" + path + ".txt";
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = br.readLine()) != null) {
			sb.append(s + "\n");
		}
		return sb.toString();

	}

	private String add(String variable, String added) throws Exception {
		int value = 0;
		String tmp = variable;
		String search_variable = SearchinMemory(variable);
		if (search_variable != null) {
			tmp = search_variable;
		}
		int oldValue = Integer.parseInt(tmp);
		tmp = added;
		String search_added = SearchinMemory(added);
		if (search_added != null) {
			tmp = search_added;
		}
		value = Integer.parseInt(tmp);

		int res = value + oldValue;
		AddinMemory(variable, res + "");
		return res + "";
	}

	private void writeFile(String filename, String data) throws FileNotFoundException {
		String path = filename;
		String search_filename = SearchinMemory(filename);
		String search_data = SearchinMemory(data);
		if (search_filename != null) {
			path = search_filename;
		}
		if (search_data != null) {
			data = search_data;
		}
		path = "src/" + path + ".txt";
		PrintWriter pw = new PrintWriter(new File(path));
		pw.print(data);
		pw.flush();
		pw.close();

	}

	private void assign(String variable, String data) throws Exception {
		String s = data;
		if (data.equals("input")) {
			s = getInput();
		} else {
			String search_data = SearchinMemory(data);
			if (search_data != null) {
				s = search_data;
			}
		}

		AddinMemory(variable, s);

	}

	private String getInput() {
		Scanner sc = new Scanner(System.in);
		String s = sc.nextLine();
		return s;
	}

	private void print(String printed) {
		String search_printed = SearchinMemory(printed);
		if (search_printed != null) {
			printed = search_printed;
		}
		System.out.println(printed);

	}

	public static void main(String[] args) throws Exception {
		LinkedList<String> programs = new LinkedList<String>();
		programs.add("Program 1");
		programs.add("Program 2");
		programs.add("Program 3");
		CPU l = new CPU(programs);
		l.Schedule();
	}

	static class Variable {
		public String key;
		public String value;

		public Variable(String variable_name, String variable_value) {
			this.key = variable_name;
			this.value = variable_value;
		}
	}

}

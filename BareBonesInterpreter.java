/** Space Cadets Challenge 3
 *  Extended interpreter for "BareBones Language".
 *  Fix the issue with memory handling.
 *  I'll simplify the indentation handling if I feel like it.
 */
// import necessary libraries
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class BareBonesInterpreter {

    Integer indentation = 0;
    Dictionary<String, Integer> variables = new Hashtable<>();
    File bareBonesFile;
    List<String> code = new ArrayList<>();
    Integer lineNo = 0;
    Stack<Integer> whileStack = new Stack<>();
    Stack<Subroutine> callStack = new Stack<>();
    String operations = "(incr|decr|clear)";
    String operationRegex = operations + "\\s([a-z]|[A-Z])+;";
    String iterations = "while";
    String iterationRegex = iterations + "\\s(([a-z]|[A-Z])+\\snot\\s\\d\\sdo);";
    /* I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX
    I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX
    I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX
    I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX
    I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX I HATE REGEX*/
    String arithmetics = "[+\\-/*]";
    String arithmeticsRegex = "([a-z]|[A-Z])+(\\s)*=(\\s)*(([a-z]|[A-Z])+|(\\d)+)(\\s)*"+ arithmetics +"(\\s)*(([a-z]|[A-Z])+|(\\d)+);";
    String assignmentRegex = "let\\s([a-z]|[A-Z])+\\s=\\s((\\d)+|(([a-z]|[A-Z])+[(](\\d+,)*\\d?[)]));";
    String subDefRegex = "def([a-z]|[A-Z])+[(](\\d+,)*\\d?[)]";
    String returnRegex = "return\\s([a-z]|[A-Z])+;";
    boolean skip = false;
    Dictionary<String, String[]> subroutineDict = new Hashtable<>(); //maps identifiers to subroutine data


     //Open file method that checks for the correct file type and if the file exists.
     //If conditions are satisfied open the file.
    public String openFile(String filePath) {
        try {
            // Check for the correct file type.
            if (!filePath.endsWith(".txt")){
                return "Invalid file type";
            }
            bareBonesFile = new File(filePath);
            Scanner codeReader = new Scanner(bareBonesFile);
            while (codeReader.hasNextLine()) {
                code.add(codeReader.nextLine());
            }
            return "pass";
        } catch (FileNotFoundException e) {
            // If file does not exist.
            return "File not found";
        }
    }

    public String interpret() {
        // While program has lines to interpret.
        while (lineNo < code.size()) {
            // Get line.
            String line = code.get(lineNo);
            String s = line.trim();
            String errorCheck = "pass";
            // Count indentation
            int currentIndentation = line.indexOf(s.substring(0,1));
            errorCheck = execute(s, currentIndentation);
            // Return if an error was encountered.
            if (!errorCheck.equals("pass")) {
                return errorCheck;
            }
            // Increment line number.
            lineNo++;
        }
        return "pass";
    }

    public String execute(String s, int currentIndentation) {
        String errorCheck = "pass";
        // Run error check.
        if (s.equals("end;") && currentIndentation == indentation - 3) {
            System.out.println(s);
            indentation -= 3;
            end();
        } else if (s.equals("endSub;") && currentIndentation == indentation - 3) {
            System.out.println(s);
            indentation -= 3;
            endSubroutine();
        }
        else if (!skip) {
            System.out.println(s);
            String syntaxCheck = syntaxErrorCheck(s, currentIndentation); //this method is a general syntax check now
            // Return if an error was encountered.
            if (syntaxCheck.equals("Syntax Error")) {
                return syntaxCheck;
            }
            // Call methods.
            switch (syntaxCheck) {
                case "operations":
                    errorCheck = executeOperation(s);
                    break;
                case "arithmetics":
                    errorCheck = executeArithmetic(s);
                    break;
                case "iterations":
                    executeIteration(s);
                    errorCheck = "pass";
                    break;
                case "assignment":
                    errorCheck = assign(s);
                    break;
                case "defSub":
                    defineSub(s);
                    errorCheck = "pass";
                    break;
                case "return":
                    returnSubroutine(s);
            }
        }
        return errorCheck;
    }

    public String syntaxErrorCheck(String line, int currentIndentation) {
        // Check if line matches regex.
        if (line.matches(operationRegex) && currentIndentation == indentation) {
            return "operations";
        } else if (line.matches(arithmeticsRegex) && currentIndentation == indentation) {
            return "arithmetics";
        } else if (line.matches(iterationRegex) && currentIndentation == indentation) {
            return "iterations";
        } else if (line.matches(assignmentRegex) && currentIndentation == indentation) {
            return "assignment";
        } else if (line.matches(subDefRegex) && currentIndentation == indentation) {
            return "defSub";
        } else if (line.matches(returnRegex) && currentIndentation == indentation) {
            return "return";
        }
        // Unknown keyword.
        return "Syntax Error";
    }

    public String assign(String s) {
        String[] stuff = s.split("[\\s=]+|;");
        String varName = stuff[1];
        Integer value = Integer.valueOf(stuff[2]);
        if (stuff[2].matches("([a-z]|[A-Z])+[(].+")) {
            callSubroutine(s.substring(s.indexOf(stuff[2])));
        }
        if (variables.put(varName, value) == null) {
            return "Var not found";
        }
        System.out.println(variables);
        return "pass";
    }

    public void defineSub(String s) { //def subName(parameters);
        // Divide the line into parts.
        String[] stuff = s.split("[\\s;()]");
        String identifier = stuff[0];
        String[] subData = new String[stuff.length - 3];
        subData[0] = String.valueOf(lineNo);
        System.arraycopy(stuff, 2, subData, 1, subData.length - 1);
        subroutineDict.put(identifier, subData);
    }

    public void callSubroutine(String s) {
        // Divide the line into parts.
        String[] stuff = s.split("[\\s;()]");
        String identifier = stuff[0];
        Integer[] arguments = new Integer[stuff.length - 2];
        for (int i = 0 ; i < arguments.length ; i++) {
            arguments[i] = Integer.valueOf(stuff[i + 1]);
        }
        Subroutine newSub = new Subroutine(lineNo, subroutineDict.get(identifier), arguments);
        this.callStack.push(newSub);
        lineNo = Integer.valueOf(subroutineDict.get(identifier)[0]);
        indentation += 3;
    }

    public void endSubroutine() {
        lineNo = callStack.pop().getReturnLineNo();
        indentation -= 3;
    }

    public void returnSubroutine(String varName) {
        Subroutine sub = callStack.pop();
        lineNo = sub.getReturnLineNo();
        Integer value = sub.getLocalVariables(varName);
        String line = code.get(lineNo);
        String s = line.trim();
        String[] stuff = s.split("[\\s=]+;");
        variables.put(stuff[0], value);
    }

    public String executeOperation(String s) {
        // Divide the line into parts.
        String[] stuff = s.split("\\s|;");
        String keyword = stuff[0];
        String errorCheck = "pass";
        switch(keyword) {
            case "clear":
                clear(stuff[1]);
                break;
            case "incr":
                errorCheck = incr(stuff[1]);
                break;
            case "decr":
                errorCheck = decr(stuff[1]);
                break;
        }
        if (!errorCheck.equals("pass")) {
            return errorCheck;
        }
        System.out.println(variables);
        return errorCheck;
    }

    public String executeArithmetic(String s) {
        // Divide the line into parts.
        String[] stuff = s.split("\\s|;");
        String keyword = stuff[3]; //lazy solution
        String var1 = stuff[0];
        Integer var2;
        Integer var3;
        int result;
        if (stuff[2].matches("\\d+")) {
            var2 = Integer.valueOf(stuff[2]);
        } else {
            if (variables.get(stuff[2]) != null){
                var2 = variables.get(stuff[2]);
            } else {
                return "Var not found";
            }
        }
        if (stuff[4].matches("\\d+")) {
            var3 = Integer.valueOf(stuff[4]);
        } else {
            if (variables.get(stuff[4]) != null){
                var3 = variables.get(stuff[4]);
            } else {
                return "Var not found";
            }
        }
        switch(keyword) {
            case "+":
                result = var2 + var3;
                variables.put(var1, result);
                break;
            case "-":
                result = var2 - var3;
                if (result < 0) {
                    return "Values assigned to variables cannot be negative";
                }
                variables.put(var1, result);
                break;
            case "/":
                if (var3 == 0) {
                    return "Division by zero";
                }
                result = var2 / var3; //rounds down
                variables.put(var1, result);
                break;
            case "*":
                result = var2 * var3;
                variables.put(var1, result);
                break;
        }
        System.out.println(variables);
        return "pass";
    }

    public void executeIteration(String s) {
        // Divide the line into parts.
        String[] stuff = s.split("\\s|;");
        String keyword = stuff[0];
        switch(keyword) { //may append iterations later thus switch statement is used here
            case "while":
                bbwhile(stuff[1], Integer.valueOf(stuff[3]));
                break;
        }
        System.out.println(variables);
    }

    public void clear(String name) {
        // Assign var 0.
        variables.put(name, 0);
    }

    public String incr(String name) {
        if (variables.get(name) != null) {
            // Increase var.
            variables.put(name, (variables.get(name) + 1));
            return "pass";
        } else {
            // Var does not exist.
            return "Var not found";
        }
    }

    public String decr(String name) {
        if (variables.get(name) != null) {
            // Check if var is 0.
            if (variables.get(name) == 0) {
                return "Values assigned to variables cannot be negative";
            }
            // Decrease var.
            variables.put(name, (variables.get(name) - 1));
            return "pass";
        } else {
            // Var does not exist.
            return "Var not found";
        }
    }

    public void bbwhile(String name, Integer num) {
        // Increase indentation level.
        indentation += 3;
        // Push line no into the while stack.
        whileStack.push(lineNo);
        if (variables.get(name) != null){
            // If condition is false, activate skip mode.
            if (variables.get(name).equals(num)) {
                skip = true;
            }
        }
    }

    public void end() {
        if (skip) {
            // Set skip to false and remove the last while from while stack.
            skip = false;
            whileStack.pop();
        } else {
            // Set line number into the last while from the while stack - 1.
            lineNo = whileStack.pop() - 1;
        }
    }

    public static void main(String[] args) {
        String errorCheck;
        System.out.println("Please enter code path: ");
        Scanner inputListener = new Scanner(System.in);
        String codePath = inputListener.nextLine();
        // Initiate object
        BareBonesInterpreter brb = new BareBonesInterpreter();

        errorCheck = brb.openFile(codePath);
        if (errorCheck.equals("pass")) {
            errorCheck = brb.interpret();
        }
        if (errorCheck.equals("pass")) {
            System.out.println("The program was executed successfully.");
        } else {
            System.out.println("Error at line " + (brb.lineNo + 1) + ": " + errorCheck);
        }
    }
}

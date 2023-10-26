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
    boolean skip = false;
    boolean endFound = false;

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
            String s = code.get(lineNo);
            String errorCheck = "pass";
            // Check for end statements.
            if (indentation >= 3) {
                if (s.substring(indentation - 3).startsWith("end;")) {
                    // Decrease the indentation level.
                    indentation -= 3;
                    endFound = true;
                }
            }
            // Remove whitespaces.
            s = s.substring(indentation);
            // If skip mode is inactivated or an end statement is encountered, execute the code.
            if (!skip || endFound) {
                errorCheck = execute(s, endFound);
            }
            // Return if an error was encountered.
            if (!errorCheck.equals("pass")) {
                return errorCheck;
            }
            // Increment line number.
            lineNo++;
        }
        return "pass";
    }

    public String execute(String s, boolean endFound) {
        System.out.println(s);
        String errorCheck = "pass";
        // Run error check.
        String syntaxCheck = syntaxErrorCheck(s); //this method is a general syntax check now tho
        // Return if an error was encountered.
        if (syntaxCheck.equals("Syntax Error")) {
            return syntaxCheck;
        }
        // Call methods.
        switch(syntaxCheck) {
            case "operations":
                errorCheck = executeOperation(s);
                break;
            case "arithmetics":
                errorCheck = executeArithmetic(s);
                break;
            case "iterations":
                executeIteration(s);
                break;
            case "end":
                if (!endFound){
                    return "Unexpected end";
                }
                end();
                break;
        }
        return errorCheck;
    }

    public String syntaxErrorCheck(String line) {
        // Check if line matches regex.
        if (line.matches(operationRegex)) {
            return "operations";
        } else if (line.matches(arithmeticsRegex)) {
            return "arithmetics";
        } else if (line.matches(iterationRegex)) {
            return "iterations";
        } else if (line.matches("end;")) {
            return "end";
        }
        // Unknown keyword.
        return "Syntax Error";
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
        Integer result;
        if (stuff[2].matches("\\d+")) {
            var2 = Integer.valueOf(stuff[2]);
        } else {
            var2 = variables.get(stuff[2]);
        }
        if (stuff[4].matches("\\d+")) {
            var3 = Integer.valueOf(stuff[4]);
        } else {
            var3 = variables.get(stuff[4]);
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
        endFound = false;
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

import java.util.Dictionary;
import java.util.Hashtable;

public class Subroutine {

    private Integer returnLineNo;
    private Dictionary<String, Integer> localVariables = new Hashtable<>();
    public Subroutine(Integer returnLineNo, String[] parameters, Integer[] arguments) {
        this.returnLineNo = returnLineNo;
        for (int i = 1 ; i < parameters.length ; i++) { //i starts at 1 because 0 is start line. I'm too lazy to make a new array...
            localVariables.put(parameters[i],arguments[i - 1]);
        }
    }

    public Integer getReturnLineNo() {
        return returnLineNo;
    }

    public void setLocalVariables(String varName, Integer value) {
        localVariables.put(varName, value);
    }

    public Integer getLocalVariables(String varName) {
        return localVariables.get(varName);
    }
}

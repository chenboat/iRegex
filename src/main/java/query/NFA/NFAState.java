package query.NFA;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/27/13
 * Time: 3:53 PM
 * A class representing a state in a NFA
 */
public class NFAState {
    private final String string; // the string this node
    public static final String epilsonStr = ""; // an empty string denoting epsilon state

    public List<Transition> getTransitions() {
        return transitions;
    }

    private List<Transition> transitions; // a set of transition from the current state
    public NFAState(String str) {
        string = str;
        transitions = new ArrayList<Transition>();
    }

    public void addTransition(Transition t)
    {
        transitions.add(t);
    }


    public String getString() {
        return string;
    }


}

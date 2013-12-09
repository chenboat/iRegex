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
    public List<Transition> getTransitions() {
        return transitions;
    }

    private List<Transition> transitions; // a set of transition from the current state
    public NFAState() {
        transitions = new ArrayList<Transition>();
    }

    public void addTransition(Transition t)
    {
        transitions.add(t);
    }


    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[State]").append(hashCode()).append("[numTransitions]").append(transitions.size()).append("\n");
        for(Transition t: transitions)
        {
            sb.append("[Transition]").append(t).append("\n");
        }

        return sb.toString();
    }


}

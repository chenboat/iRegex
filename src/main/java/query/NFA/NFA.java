package query.NFA;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/27/13
 * Time: 3:50 PM
 *
 * A NFA representing non-deterministic finite automaton
 */
public class NFA {

    public NFA(NFAState initState) {
        this.initState = initState;
    }

    public NFAState getInitState() {
        return initState;
    }

    private final NFAState initState;
    private List<NFAState> finalStates = new ArrayList<NFAState>();


    public List<NFAState> getFinalStates() {
        return finalStates;
    }

    public void setFinalStates(List<NFAState> states) {
        finalStates = states;
    }

    public void addFinalState(NFAState s)
    {
        finalStates.add(s);
    }


}

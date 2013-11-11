package query.NFA;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/27/13
 * Time: 3:56 PM
 * Representing a transition in a NFA; each transition is defined by an edge text and the destination string
 */
public class Transition {
    private final NFAState from;
    private final NFAState to;


    public Transition(NFAState from, NFAState to) {
        this.from = from;
        this.to = to;
    }
}

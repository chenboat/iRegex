package query.NFA;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/27/13
 * Time: 3:56 PM
 * Representing a transition in a NFA; each transition is defined by an edge text and the destination string
 */
public class Transition {
    private final String str;
    public final static String EPSILON_STR = "";
    private final NFAState to;
    private final NFAState from;

    public NFAState getFrom() {
        return from;
    }

    public String getStr() {
        return str;
    }

    public NFAState getTo() {
        return to;
    }


    public Transition(NFAState from, NFAState to, String str) {
        this.from = from;
        this.to = to;
        this.str = str;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(from.hashCode()).append("]").append("->").append("[").append(to.hashCode()).append("]");
        return sb.toString();
    }
}

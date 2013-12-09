package query;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/24/13
 * Time: 4:45 PM
 */
public class Pair<String, NFAState> {
    private final String str;
    private final NFAState state;

    public Pair(String str, NFAState s){
        this.str = str;
        this.state = s;
    }
    @Override
    public boolean equals(Object p)
    {
        try
        {
            Pair<String,NFAState> cast = (Pair<String,NFAState>)p;
            return str.equals(cast.str) && state.equals(cast.state);
        }catch (ClassCastException e)
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return str.hashCode() + state.hashCode();
    }
}
package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:14 PM
 */
public class Repetition extends Regex {
    private final Regex internal ;
    private final int min; // the min time that the internal regex can repeat
    private final int max; // the max time that the internal regex can repeat

    public Repetition(Regex in, int l, int h){
        internal = in;
        min = l;
        max = h;
    }


    public Regex condense(){
        return new Repetition(internal.condense(),min,max);
    }

    public String toString(){
        if(min == 0 && max == Integer.MAX_VALUE)
            return "(" + internal + ")" + "*";
        else if(min == 1 && max == Integer.MAX_VALUE)
            return "(" + internal + ")" + "+";
        else if(min == 0 && max == 1)
            return "(" + internal + ")" + "?";
        else
            return "(" + internal + ")" + "{" + min + "," + max + "}";
    }

}

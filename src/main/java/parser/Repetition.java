package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:14 PM
 */
public class Repetition extends Regex {
    private Regex internal ;

    public Repetition(Regex internal) {
        this.internal = internal ;
    }
    
    public Regex condense(){
        internal = internal.condense();
        return this;
    }
    public String toString(){
        return "(" + internal + ")" + "*";
    }

}

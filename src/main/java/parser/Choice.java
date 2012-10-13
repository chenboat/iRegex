package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:04 PM
 */
public class Choice extends Regex {
    private Regex thisOne ;
    private Regex thatOne ;

    public Choice(Regex term, Regex regex) {
        this.thisOne = term;
        this.thatOne = regex;
    }
    
    public Regex condense(){
        thisOne = thisOne.condense();
        thatOne = thatOne.condense();
        return this;
    }
    
    public Regex getLeftRegex()
    {
        return thisOne;
    }
    
    public Regex getRightRegex()
    {
        return thatOne;
    }
    
    public String toString(){
        return "(" + thisOne + "|" + thatOne + ")";
    }
}

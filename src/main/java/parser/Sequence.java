package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:13 PM
 */
public class Sequence extends Regex {
    private Regex first ;
    private Regex second ;

    public Sequence (Regex first, Regex second) {
        this.first = first ;
        this.second = second ;
    }

    public Regex condense(){
        first = first.condense();
        second = second.condense();
        if(first instanceof Primitive && second instanceof Primitive){
            return new Primitive(first.toString() + second.toString());
        }
        else
            return this;
    }

    public Regex getLeftRegex()
    {
        return first;
    }

    public Regex getRightRegex()
    {
        return second;
    }

    public String toString(){
        return first.toString() + second.toString();
    }

}

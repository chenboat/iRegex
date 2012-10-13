package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:15 PM
 */
public class Primitive extends Regex {
    private String c ;

    public Primitive(char c) {
        this.c = Character.toString(c) ;
    }
    
    public Primitive(String str) {
        this.c = str;
    }
    
    public Regex condense(){
        return this;    
    }
    public String toString()
    {
        return c;
    }
}

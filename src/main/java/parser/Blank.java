package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 10:16 PM
 */
public class Blank extends Regex{
    public Regex condense(){
        return this;
    }
    public String toString(){
        return "";
    }
}

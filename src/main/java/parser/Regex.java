package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 9:58 PM
 */
public abstract class Regex {
    public static Blank blank = new Blank();
    public abstract Regex condense();
}

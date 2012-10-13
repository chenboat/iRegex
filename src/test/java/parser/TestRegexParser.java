package parser;

import junit.framework.TestCase;

/**
 * Created by User: ting
 * Date: 10/6/12
 * Time: 10:07 AM
 */
public class TestRegexParser extends TestCase{
    public void testNormalSequence(){
        RegexParser parser = new RegexParser("d");
        Regex regex = parser.parse();
        assertEquals("Single char regex: ", "d",regex.toString());

        parser = new RegexParser("cd");
        regex = parser.parse();
        assertEquals("two char regex: ", "cd",regex.toString());

        parser = new RegexParser("acd");
        regex = parser.parse();
        assertEquals("three char regex: ", "acd",regex.toString());
    }

    public void testNormalChoice(){
        RegexParser parser = new RegexParser("a|d");
        Regex regex = parser.parse();
        assertEquals("Two branch choice: ", "(a|d)" ,regex.toString());

        parser = new RegexParser("(a|d)");
        regex = parser.parse();
        assertEquals("Two branch choice with parentheses: ", "(a|d)",regex.toString());

        parser = new RegexParser("(ac|df)");
        regex = parser.parse();
        assertEquals("Another two branch choice regex: ", "(ac|df)",regex.toString());

        parser = new RegexParser("(ac|df|ddd)");
        regex = parser.parse();
        assertEquals("Triple branch choice regex: ", "(ac|(df|ddd))",regex.toString());
    }


    public void testNormalRepetition(){
        RegexParser parser = new RegexParser("a*");
        Regex regex = parser.parse();
        assertEquals("One character rep: ", "(a)*" ,regex.toString());

        parser = new RegexParser("(ab)*");
        regex = parser.parse();
        assertEquals("Two character rep: ", "(ab)*",regex.toString());

        parser = new RegexParser("ab*");
        regex = parser.parse();
        assertEquals("A char followed by repetition: ", "a(b)*",regex.toString());

    }

    public void testNormalChoicePlusRepetition(){
        RegexParser parser = new RegexParser("a*|b");
        Regex regex = parser.parse();
        assertEquals("Star plus char: ", "((a)*|b)" ,regex.toString());

        parser = new RegexParser("a*|bc");
        regex = parser.parse();
        assertEquals("Star plus two character sequence: ", "((a)*|bc)",regex.toString());

        parser = new RegexParser("(a*|b*)");
        regex = parser.parse();
        assertEquals("Star with star ", "((a)*|(b)*)",regex.toString());

        parser = new RegexParser("(ab*|b*)");
        regex = parser.parse();
        assertEquals("Star with nested sequence with star ", "(a(b)*|(b)*)",regex.toString());

        parser = new RegexParser("(acb*|b*)");
        regex = parser.parse();
        assertEquals("Star with nested sequence with star ", "(ac(b)*|(b)*)",regex.toString());

        parser = new RegexParser("(acb*|b*|ab*)");
        regex = parser.parse();
        assertEquals("Star with nested sequence with star ", "(ac(b)*|((b)*|a(b)*))",regex.toString());

    }


}

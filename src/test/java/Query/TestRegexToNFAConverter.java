package Query;

import junit.framework.TestCase;
import parser.Regex;
import parser.RegexParser;
import query.NFA.NFA;
import query.NFA.RegexToNFAConverter;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/24/13
 * Time: 11:43 AM
 */
public class TestRegexToNFAConverter extends TestCase {
    public void testSequenceRegex(){
        Regex r = new RegexParser("abc").parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa abc \n" + nfa);
        assertEquals("Number of transition should be 1: ",nfa.getInitState().getTransitions().size(),1);
    }

    public void testUnionRegex(){
        Regex r = new RegexParser("a|b").parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for a|b is \n" + nfa);
        assertTrue(nfa.getFinalStates().size() == 2);

        r = new RegexParser("(a|b)").parse().condense();
        nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for (a|b) is \n" + nfa);
        assertTrue(nfa.getFinalStates().size() == 2);

    }



    public void testRepeatRegex(){
        Regex r = new RegexParser("a*").parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for a* is \n " + nfa);
        assertTrue(nfa.getFinalStates().size() == 2);
    }

    public void testUnionAndSequenceRegex(){
        Regex r1 = new RegexParser("ab(cd|e)").parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r1);
        System.out.println("==== The nfa for ab(cd|e) is \n "+ nfa);

        Regex r2 = new RegexParser("(cd|e)ab").parse().condense();
        nfa = RegexToNFAConverter.convert(r2);
        System.out.println("==== The nfa for (cd|e)ab is \n "+ nfa);
    }

    public void testUnionAndSequenceAndStarRegex(){
        String regex = "ab*c";
        Regex r = new RegexParser(regex).parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for " + regex + " is \n "+ nfa);

        regex = "a(b|d)*c";
        r = new RegexParser(regex).parse().condense();
        nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for " + regex + " is \n "+ nfa);
    }

    public void testUnionAndSequenceAndStarWithNestRegex(){
        String regex = "a(b*|e)*c";
        Regex r = new RegexParser(regex).parse().condense();
        NFA nfa = RegexToNFAConverter.convert(r);
        System.out.println("==== The nfa for " + regex + " is \n "+ nfa);
    }


}

package Query;

import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;
import parser.RegexParser;
import query.NFA.NFA;
import query.NFA.NFAState;
import query.NFA.RegexToNFAConverter;
import query.NFABasedIndexQuery;
import query.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/24/13
 * Time: 1:05 PM
 */
public class TestNFABasedIndexQuery extends TestCase {
    Set<String> dictionary = ImmutableSet.of("ab","bc","de","ae","db");
    public void testSequence() {
        try{
            List<String> pLst = queryIndex("abc");
            assertTrue(pLst.contains("abc"));
            assertTrue(pLst.size() == 1);
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(true);
        }
    }

    public void testUnion() {
        try{
            queryIndex("ab|c");
            assertTrue(false);
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(true);
        }
    }

    public void testStar() {
        try{
            queryIndex("ab*");
            assertTrue(false);
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(true);
        }
    }

    public void testSequenceAndUnion() {
        try{
            List<String> pLst = queryIndex("a(b|d)e");
            assertTrue(pLst.size() == 2);
            assertTrue(pLst.contains("ab"));
            assertTrue(pLst.contains("ade"));
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(false);
        }

        try{
            List<String> pLst = queryIndex("a(b|d)e");
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(false);
        }
    }

    public void testSequenceAndUnionAndStar() {
        try{
            List<String> pLst = queryIndex("a(b|d)*e");
            assertEquals("The number of path should be 6:",6,pLst.size());
            assertTrue(pLst.contains("ab"));
            assertTrue(pLst.contains("ae"));
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(false);
        }

        try{
            List<String> pLst = queryIndex("a(b|c)*e");
         }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(true);
        }

        try{
            List<String> pLst = queryIndex("a(b|c)*");
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(true);
        }

        try{
            List<String> pLst = queryIndex("ad*e");
            assertTrue(pLst.size() == 3);
            assertTrue(pLst.contains("ae"));
            assertTrue(pLst.contains("adde"));
            assertTrue(pLst.contains("ade"));
        }catch (NFABasedIndexQuery.NoIndexQueryException e)
        {
            assertTrue(false);
        }
    }

    public void testPair()
    {
        NFAState state = new NFAState();
        Pair<String,NFAState> p1 = new Pair<String,NFAState>("s", state);

        Set<Pair<String, NFAState>> visited = new HashSet<Pair<String, NFAState>>();
        visited.add(p1);
        assertTrue(visited.contains(new Pair<String,NFAState>("s", state)));
    }

    private List<String> queryIndex(String regex)throws NFABasedIndexQuery.NoIndexQueryException{
        NFABasedIndexQuery queryPlanner = new NFABasedIndexQuery(dictionary,2);

        NFA nfa = RegexToNFAConverter.convert(new RegexParser(regex).parse().condense());

        System.out.println("======= Query for " + regex + " is:");
        List<String> strs = queryPlanner.buildIndexQuery(nfa.getInitState());
        for (String s : strs) {
            System.out.println(s);
        }
        return strs;
    }
}

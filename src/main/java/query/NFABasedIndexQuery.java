package query;

import query.NFA.NFAState;
import query.NFA.Transition;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/27/13
 * Time: 3:44 PM
 * A nfa based n-gram index query engine
 */
public class NFABasedIndexQuery {

    private List<String> pathSet = new ArrayList<String>(); // a set of strings containing indexed grams

    private Set<Pair<String,NFAState>> visitedStates = new HashSet<Pair<String, NFAState>>();

    private final Set<String> ngramDictionary;
    private final int Max_Ngram_Length;
    public NFABasedIndexQuery(Set<String> dictionary,int maxNgramLength)
    {
        this.ngramDictionary = dictionary;
        this.Max_Ngram_Length = maxNgramLength;
    }

    public List<String> buildIndexQuery(NFAState init) throws NoIndexQueryException
    {
        // Recursively perform DFS
        DFSExplore("",init);
        return pathSet;
    }

    private void DFSExplore(String leadingStr, NFAState s) throws NoIndexQueryException
    {
        if(!visitedStates.contains(new Pair(getSuffix(leadingStr),s)))
        {
            visitedStates.add(new Pair(getSuffix(leadingStr),s));
            if(hasNGram(leadingStr))
            {
                pathSet.add(leadingStr);
            }
            else
            {
                List<Transition> transitions = s.getTransitions();
                if(transitions == null || transitions.size() == 0) // there is no next possible state, throws exception
                {
                    throw new NoIndexQueryException();
                }
                for(Transition t: transitions)
                {
                    DFSExplore(leadingStr + t.getStr(),t.getTo());
                }
            }
        }
        else
        {
            if(hasNGram(leadingStr))
            {
                pathSet.add(leadingStr);
            }
        }
    }

    /**
     *
     *
     * @param str   input
     * @return the length max_kgram_length suffix of str; if str is shorter than that, return the entire string
     */
    private String getSuffix(String str) {
        if(str.length() < Max_Ngram_Length)
        {
            return str;
        }
        else
        {
            return str.substring(str.length() - Max_Ngram_Length);
        }
    }

    /**
     * @param s an input string
     * @return true if a substring of s is in the ngramDictionary
     */
    private boolean hasNGram(String s)
    {
        for(int i = 0; i < s.length(); i++)
        {
            for(int j = Max_Ngram_Length; j > 0 ; j--) // longer substring has more prob to be in the dictionary
            {
                if((i+j) <= s.length() && ngramDictionary.contains(s.substring(i,i+j)))
                {
                    return true;
                }
            }
        }
        return false;
    }




    public class NoIndexQueryException extends Exception {
    }
}

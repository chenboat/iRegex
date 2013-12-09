package query;

import Utility.PostingListOperation;
import com.google.common.collect.ImmutableSet;
import indexer.NGramIndexer;
import it.unimi.dsi.fastutil.booleans.BooleanSets;
import org.apache.lucene.index.IndexReader;
import parser.*;
import query.NFA.NFA;
import query.NFA.QueryConversionSideEffect;
import query.NFA.RegexToNFAConverter;
import query.NFA.Transition;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/28/13
 * Time: 10:07 PM
 * A class implementing the parse tree with leaf string NFA traversal query formulation algorithm
 */
public class NFATreeIndexQuery extends IndexQuery{
    public NFATreeIndexQuery(IndexReader r) throws IOException {
        super(r);
    }

    private Set<Integer> prune(Regex r) throws IOException{
        // First get the leaf string to transition mapping
        QueryConversionSideEffect sideEffect = new QueryConversionSideEffect();
        NFA nfa = RegexToNFAConverter.convert(r, sideEffect);
        // Now recursively prune
        return recursivePrune(r,sideEffect.getMap());
    }

    private Set<Integer> recursivePrune(Regex r, HashMap<Primitive, Transition> map) throws IOException
    {
        if(r instanceof Choice)
        {
            Choice c = (Choice)r;
            Set<Integer> leftSet = prune(c.getLeftRegex());
            Set<Integer> rightSet = prune(c.getRightRegex());
            return PostingListOperation.union(leftSet, rightSet);
        }else if(r instanceof Sequence)
        {
            Sequence s = (Sequence)r;
            Set<Integer> leftSet = prune(s.getLeftRegex());
            Set<Integer> rightSet = prune(s.getRightRegex());
            return PostingListOperation.intersect(leftSet, rightSet);
        }else if(r instanceof Repetition)
        {
            return null;
        }else if(r instanceof Primitive)
        {
            String str = r.toString();
            if(str == null || str.length() == 0)
                return null;
            Transition transition = map.get(r);
            assert transition != null; // there should be an associated transition
            NFABasedIndexQuery leafQuery = new NFABasedIndexQuery(this.gram2TermDocs.keySet(),MAX_LEN);
            try {
                List<String> strList = leafQuery.buildIndexQuery(transition.getFrom());
                Set<Integer> result = Collections.emptySet();
                for(String s: strList)
                {
                    result = PostingListOperation.union(result,getPrunedSetFromAStr(s));
                }
                return result;
            } catch (NFABasedIndexQuery.NoIndexQueryException e) {
                return null;
            }
        }
        return null;
    }


    private Set<Integer> getPrunedSetFromAStr(String str) throws IOException {
        Set<Integer> result = null;
        for(int i = 0; i < str.length(); i++)
        {
            for(int j = MAX_LEN ; j >= 1; j--)
            {
                if((i+j) > str.length()) continue;

                String subStr = str.substring(i,i+j);
                String fieldName = NGramIndexer.DOC_FIELD_NAME+":"+subStr;

                if(!gram2TermDocs.containsKey(fieldName))
                    return ImmutableSet.of();
                result = PostingListOperation.intersect
                        (result, getPostingList(fieldName));
                break; // we have searched the super-string and there is no need to search its substrings
            }
        }
        return result;
    }
}

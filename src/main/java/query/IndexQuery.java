package query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import indexer.NGramIndexer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import parser.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by User: ting
 * Date: 10/7/12
 * Time: 3:49 PM
 */
public class IndexQuery {
    private final IndexReader reader;
    private static final int MAX_LEN = 2;
    private final HashMap<String,TermDocs> gram2PostingLst = new HashMap<String, TermDocs>();

    public IndexQuery(IndexReader r) throws IOException
    {
        this.reader = r;
        TermEnum terms = reader.terms();
        while(terms.next())
        {
            Term t = terms.term();
            TermDocs docs = reader.termDocs(t);
            gram2PostingLst.put(t.toString(),docs);
        }
    }
    
    /**
     * 
     * @param regex: a given regular expression
     * @return a set of document ids surviving the index pruning
     *         if the returned set is null: it means all the documents in the corpus
     *         
     */
    public Set<Integer> getPrunedSet(String regex) throws IOException
    {
        //1. Parse the regex
        RegexParser parser = new RegexParser(regex);
        Regex r = parser.parse();
        //2. Read the index and prune
        return prune(r);
    }   
    
    private Set<Integer> prune(Regex r) throws IOException{
       if(r instanceof Choice)
       {
           Choice c = (Choice)r;
           Set<Integer> leftSet = prune(c.getLeftRegex());
           Set<Integer> rightSet = prune(c.getRightRegex());
           return union(leftSet, rightSet);
       }else if(r instanceof Sequence)
       {
           Sequence s = (Sequence)r;
           Set<Integer> leftSet = prune(s.getLeftRegex());
           Set<Integer> rightSet = prune(s.getRightRegex());
           return intersect(leftSet, rightSet);
       }else if(r instanceof Repetition)
       {
           return null;
       }else if(r instanceof Primitive)
       {
           String str = r.toString();
           if(str == null || str.length() == 0)
               return null;
           Set<Integer> result = null;
           for(int i = 0; i < str.length(); i++)
           {
               for(int j = MAX_LEN ; j >= 1; j--)
               {
                   if((i+j) > str.length()) continue;

                   String subStr = str.substring(i,i+j);
                   if(!gram2PostingLst.containsKey(NGramIndexer.DOC_FIELD_NAME+":"+subStr))
                       return ImmutableSet.of();
                   result = intersect
                           (result, getPostingLists(gram2PostingLst.get(NGramIndexer.DOC_FIELD_NAME+":"+subStr)));
               }
           }
           return result;
       }
       return null; 
    }

    /**
     * 
     * @param termDocs
     * @return
     */
    private Set<Integer> getPostingLists(TermDocs termDocs) throws IOException{
       Set<Integer> set = Sets.newHashSet();
       while(termDocs.next()){
        set.add(termDocs.doc());
       }
       return set;
    }

    /**
     * 
     * @param leftSet: null represents a universal set
     * @param rightSet
     * @return
     */
    private Set<Integer> union(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null || rightSet == null)
            return null;
        return Sets.union(leftSet,rightSet);
    }

    /**
     *
     * @param leftSet: null represents a universal set
     * @param rightSet
     * @return
     */
    private Set<Integer> intersect(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null)
            return rightSet;
        else if(rightSet == null)
            return leftSet;
        return Sets.intersection(leftSet, rightSet);
    }
}

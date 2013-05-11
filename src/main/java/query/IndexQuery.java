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
    private static final int MAX_LEN = 2;
    private final HashMap<String,TermDocs> gram2TermDocs = new HashMap<String, TermDocs>();
    private final HashMap<String,Set<Integer>> gram2PostingLst = new HashMap<String, Set<Integer>>();
    
    public IndexQuery(IndexReader r) throws IOException
    {
        TermEnum terms = r.terms();
        while(terms.next())
        {
            Term t = terms.term();
            TermDocs docs = r.termDocs(t);
            gram2TermDocs.put(t.toString(),docs);
        }
    }
    
    /**
     * 
     * @param regex: a given regular expression
     * @return a set of document ids surviving the index pruning
     *         if the returned set is null: it means all the documents in the corpus
     *
     * @throws java.io.IOException if there is io exception
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
                   String fieldName = NGramIndexer.DOC_FIELD_NAME+":"+subStr;

                   if(!gram2TermDocs.containsKey(fieldName))
                       return ImmutableSet.of();
                   result = intersect
                           (result, getPostingList(fieldName));
                   break; // we have searched the super-string and there is no need to search its substrings
               }
           }
           return result;
       }
       return null; 
    }

    /**
     * 
     * @param str a string
     * @return the posting list of the str
     * @throws IOException error
     */
    private Set<Integer> getPostingList(String str) throws IOException{
       if(gram2PostingLst.containsKey(str))
       {
           return gram2PostingLst.get(str); //return the list if it is already found 
       }
        
       Set<Integer> set = Sets.newHashSet();
       TermDocs termDocs = gram2TermDocs.get(str);
       while(termDocs.next()){
        set.add(termDocs.doc());
       }
       gram2PostingLst.put(str,set);
       return set;
    }

    /**
     * 
     * @param leftSet: null represents a universal set
     * @param rightSet: null is all
     * @return the result of union; null means all
     */
    private Set<Integer> union(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null || rightSet == null)
            return null;
        return Sets.union(leftSet,rightSet);
    }

    /**
     *
     * @param leftSet: null represents a universal set
     * @param rightSet: null is all
     * @return result of intersection
     */
    private Set<Integer> intersect(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null)
            return rightSet;
        else if(rightSet == null)
            return leftSet;
        return Sets.intersection(leftSet, rightSet);
    }
}

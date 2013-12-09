package query;

import Utility.PostingListOperation;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import indexer.NGramIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import parser.*;
import query.filters.LuceneResultFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by : Ting Chen
 * Date: 10/7/12
 * Time: 3:49 PM
 */
public class IndexQuery {
    protected static final int MAX_LEN = 2;
    protected final HashMap<String,TermDocs> gram2TermDocs = new HashMap<String, TermDocs>();
    private final HashMap<String,Set<Integer>> gram2PostingLst = new HashMap<String, Set<Integer>>();
    private final IndexReader reader;

    public IndexQuery(IndexReader r) throws IOException
    {
        reader = r;
        TermEnum terms = reader.terms();
        while(terms.next())
        {
            Term t = terms.term();
            TermDocs docs = reader.termDocs(t);
            gram2TermDocs.put(t.toString(),docs);
        }
    }

    /**
     *
     *
     * @param regex : a given regular expression
     * @return a set of document ids surviving the index pruning
     *         if the returned set is null: it means all the documents in the corpus
     *
     * @throws java.io.IOException if there is io exception
     */
    public Set<Document> getPrunedSet(String regex)
            throws IOException
    {
        return getPrunedSet(regex,null);
    }

    /**
     * 
     *
     * @param regex : a given regular expression
     * @param filter: a filter which may be applied to the results; it can be null
     * @return a set of document ids surviving the index pruning
     *         if the returned set is null: it means all the documents in the corpus
     *
     * @throws java.io.IOException if there is io exception
     */
    public Set<Document> getPrunedSet(String regex, LuceneResultFilter filter)
            throws IOException
    {
        //1. Parse the regex
        RegexParser parser = new RegexParser(regex);
        Regex r = parser.parse();
        //2. Read the index and prune
        Set<Document> results = new HashSet<Document>();
        for(Integer i:prune(r))
        {
            Document d = reader.document(i);
            if(filter == null || filter.filter(d))
            {
                results.add(d);
            }
        }
        return results;
    }   
    
    private Set<Integer> prune(Regex r) throws IOException{
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
       return null; 
    }

    /**
     * 
     * @param str a string
     * @return the posting list of the str
     * @throws IOException error
     */
    protected Set<Integer> getPostingList(String str) throws IOException{
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


}

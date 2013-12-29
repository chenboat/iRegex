package Query;

import indexer.NGramAnalyzer;
import indexer.NGramIndexer;
import indexer.NGramTokenizer;
import indexer.channel.SingleFileIndexer;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import query.IndexQuery;
import query.filters.RegexLuceneResultFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Created by User: ting
 * Date: 10/7/12
 * Time: 5:20 PM
 */
public class TestIndexQuery extends TestCase {
    public final NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
    
    @Before
    public void setUp(){
        
    } 
    
    @After
    public void tearDown()
    {
        
    }
    
    public void testIndexQueryOnRAMDir() throws IOException{
        // Add documents to the index
        // Add documents to the index
        String sourceFile = "src/test/resources/corpora/names/1.txt";
        Directory index = new RAMDirectory();

        NGramIndexer indexer = new NGramIndexer(analyzer,index,true);
        SingleFileIndexer fileIndexer = new SingleFileIndexer(sourceFile,indexer);

        fileIndexer.buildIndex();

        // Query the index
        IndexReader reader = IndexReader.open(index);
        TermEnum terms = reader.terms();
        while(terms.next())
        {
            Term t = terms.term();
            TermDocs docs = reader.termDocs(t);
            System.out.print(t + "\t|" );
            while(docs.next()){
                System.out.print(docs.doc()+" ");
            }
            System.out.println();
        }
        
        // Prune for a dummy regex
        IndexQuery indexQuery = new IndexQuery(reader);
        String pattern = "(J|M)aa*y";
        RegexLuceneResultFilter filter = new RegexLuceneResultFilter(pattern,"content");
        Set<Document> set = indexQuery.getPrunedSet(pattern,filter);

        System.out.print("Pruned set: ");
        if(set != null){
            for (Document aSet : set) {
                System.out.print(aSet.get("content") + " ");
            }
            System.out.println("| " + set.size() + " docs in total");
        }else{
            System.out.println( " All docs are possible matches");
        }

        reader.close();    
    }



}

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
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import query.IndexQuery;
import query.NFATreeIndexQuery;
import query.filters.RegexLuceneResultFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 12/8/13
 * Time: 7:12 PM
 */
public class TestNFATreeIndexQuery extends TestCase{
    public final NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));

    @Before
    public void setUp(){

    }

    @After
    public void tearDown()
    {

    }

    public void testIndexQueryOnRAMDir() throws IOException {
        // Add documents to the index
        // Add documents to the index
        String sourceFile = "src/test/resources/corpora/names/1.txt";
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);

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
        NFATreeIndexQuery indexQuery = new NFATreeIndexQuery(reader);
        String pattern = "(J|M)aa*y";
        RegexLuceneResultFilter filter = new RegexLuceneResultFilter(pattern,"content");
        Set<Document> set = indexQuery.getPrunedSet(pattern,filter);

        System.out.print("Pruned set: ");
        if(set != null){
            Iterator<Document> itr = set.iterator();
            while(itr.hasNext()){
                System.out.print(itr.next().get("content") + " ");
            }
            System.out.println("| " + set.size() + " docs in total");
        }else{
            System.out.println( " All docs are possible matches");
        }

        reader.close();
    }

}

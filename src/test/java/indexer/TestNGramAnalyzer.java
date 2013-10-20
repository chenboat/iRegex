package indexer;

import indexer.channel.SingleFileIndexer;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 * Created by User: ting
 * Date: 10/7/12
 * Time: 3:31 PM
 */
public class TestNGramAnalyzer extends TestCase {
    public final NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));

    public void testNGramAnalyzer() throws IOException {
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
        reader.close();
    }

}

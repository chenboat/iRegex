package indexer;

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
    public final NGramAnalyzer analyzer = new NGramAnalyzer();



    public void testNGramAnalyzer() throws IOException {
        // Add documents to the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);

        IndexWriter w = new IndexWriter(index,config);
        addDoc(w, "hello");
        addDoc(w, "ting");
        addDoc(w, "test it");
        addDoc(w, "tim");
        w.close();

        // Query the index
        String query = "ti";
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

    private static void addDoc(IndexWriter w, String value) throws IOException {
        Document doc = new Document();
        doc.add(new Field("contents", value, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
    }
}

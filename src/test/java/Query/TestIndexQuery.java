package Query;

import indexer.NGramAnalyzer;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import query.IndexQuery;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by User: ting
 * Date: 10/7/12
 * Time: 5:20 PM
 */
public class TestIndexQuery extends TestCase {
    
    
    @Before
    public void setUp(){
        
    } 
    
    @After
    public void tearDown()
    {
        
    }
    
    public void testIndexQueryOnRAMDir() throws IOException{
        // Add documents to the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,new NGramAnalyzer());

        IndexWriter w = new IndexWriter(index,config);
        addDoc(w, "hello");
        addDoc(w, "ting");
        addDoc(w, "test it");
        addDoc(w, "tim");
        w.close();

        // Examine the index
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
        
        // Prune for a dummy regex
        IndexQuery indexQuery = new IndexQuery(reader);
        Set<Integer> set = indexQuery.getPrunedSet("hel*");


        System.out.print("Pruned set: ");
        if(set != null){
            Iterator<Integer> itr = set.iterator();
            while(itr.hasNext()){
               System.out.print(itr.next() + " ");  
            }
            System.out.println("| " + set.size() + " docs in total");
        }else{
            System.out.println( " All docs are possible matches");
        }
            
        
       
        
        reader.close();    
    }

    private void addDoc(IndexWriter w, String value) throws IOException {
        Document doc = new Document();
        doc.add(new Field("", value, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
    }
}
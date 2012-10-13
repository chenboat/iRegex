package indexer;

import Utility.Utility;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.*;

/**
 * Created by User: ting
 * Date: 8/29/12
 * Time: 5:27 PM
 *
 * The class builds a n-gram Lucene index for all the documents
 */
public class NGramIndexer {
    private String dataSrc;
    private String outputDir;
    private static final int MAX_NGRAM_LENGTH = 1;

    /**
     * @param dataSrc the directory root of all files to index
     * @param outputDir the directory where the indices file will be stored
     */
    public NGramIndexer(String dataSrc,String outputDir){
        this.dataSrc = dataSrc;
        this.outputDir = outputDir;
    }

    /**
     * This method will scan each document and extract all n-gram up to length
     * K from the document. The mapping from docID to a set of k-grams will be fed
     * into a Lucene indexer.
     */
    public void constructIndex(){
        
    }

    public static void main(String[] args) throws IOException {
        // Add documents to the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,new NGramAnalyzer());
        
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

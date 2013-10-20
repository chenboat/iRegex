package indexer;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.*;

/**
 * Created by User: ting chen
 * Date: 8/29/12
 * Time: 5:27 PM
 *
 * The class builds a n-gram Lucene index for all the documents
 */
public class NGramIndexer implements Indexer{
    private String dataSrc;
    private NGramAnalyzer analyzer;
    private IndexWriter writer;
    private boolean storeDocValue = false; // control whether to store file content in the index
    public static final String DOC_FIELD_NAME = "gram";
    public static final String FILE_NAME = "fn";
    public static final String FILE_CONTENT = "content";


    /**
     * @param analyzer  the n-gram analyzer used by the indexer
     * @param index the output index directory
     * @param storeDocValue control whether to store file content in the index
     */
    public NGramIndexer( NGramAnalyzer analyzer, Directory index, boolean storeDocValue) throws IOException{
        this.analyzer = analyzer;
        this.storeDocValue = storeDocValue;

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,this.analyzer);
        writer = new IndexWriter(index,config);

    }

    /**
     *
     * @param content document content
     * @param id doc id
     * @throws IOException
     */
    public void addDoc(String content, String id) throws IOException {
        Document doc = new Document();
        doc.add(new Field(DOC_FIELD_NAME, content, Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field(FILE_NAME,id,Field.Store.YES,Field.Index.NO));
        if(storeDocValue)
        {
            doc.add(new Field(FILE_CONTENT,content,Field.Store.YES, Field.Index.NO));
        }
        writer.addDocument(doc);
    }

    /**
     * This will close the indexer writer
     * @throws IOException
     */
    public void close() throws IOException {
        writer.close();
    }
}

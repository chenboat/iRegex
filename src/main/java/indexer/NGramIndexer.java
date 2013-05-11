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
public class NGramIndexer {
    private String dataSrc;
    private NGramAnalyzer analyzer;
    public static final String DOC_FIELD_NAME = "gram";
    public static final String FILE_NAME = "fn";

    /**
     * @param dataSrc the directory root of all files to index
     * @param analyzer the n-gram analyzer used by the indexer
     */
    @Inject
    public NGramIndexer(@Named("source directory")  String dataSrc, NGramAnalyzer analyzer){
        this.dataSrc = dataSrc;
        this.analyzer = analyzer;
    }

    /**
     * This method will scan each document and extract all n-gram up to length
     * K from the document. The mapping from docID to a set of k-grams will be fed
     * into a Lucene indexer.
     * @param index the output index directory
     * @throws java.io.IOException when reading from a file or writing to lucene index
     */
    public void constructIndex(Directory index) throws IOException{
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);

        IndexWriter w = new IndexWriter(index,config);
        
        File f = new File(dataSrc);
        // iterate through all the files root at the data src directory
        Queue<File> q = new LinkedList<File>();
        q.add(f);
        
        File fi;
        
        while((fi=q.poll())!= null)
        {
            if(fi.isFile()){
                String s1 = readFileContent(fi);
                addDoc(w,s1,fi.getName());
            }
            else if(fi.isDirectory()){ // a directory itself
                Collections.addAll(q, fi.listFiles());
            }
        }
        w.close();  
        
    }

    private String readFileContent(File f) {
        StringBuilder file_contents = new StringBuilder(3*IndexerConfig.FS_BLOCK_SIZE);
        try{
            FileInputStream fs = new FileInputStream(f);
            byte[] bs = new byte[IndexerConfig.FS_BLOCK_SIZE];
            int len;
            while((len=fs.read(bs))!= -1){
                file_contents.append(new String(bs,0,len));
            }
            fs.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return file_contents.toString();
    }

    private static void addDoc(IndexWriter w, String value, String fileName) throws IOException {
        Document doc = new Document();
        doc.add(new Field(DOC_FIELD_NAME, value, Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field(FILE_NAME,fileName,Field.Store.YES,Field.Index.NO));
        w.addDocument(doc);
    }
}

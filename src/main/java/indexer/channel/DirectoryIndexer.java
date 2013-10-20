package indexer.channel;

import indexer.Indexer;
import indexer.IndexerConfig;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: ting
 * Date: 10/19/13
 * Time: 3:13 PM
 * This is a class which will index all the files under a directory
 */
public class DirectoryIndexer {
    private final String srcDataDirectory;
    private final Indexer indexer;
    public DirectoryIndexer(String dataSrc, Indexer indexer)
    {
        srcDataDirectory = dataSrc;
        this.indexer = indexer;
    }

    /**
     * This method will scan each document and extract all n-gram up to length
     * K from the document. The mapping from docID to a set of k-grams will be fed
     * into a Lucene indexer.
     * @throws java.io.IOException when reading from a file or writing to lucene index
     */
    public void buildIndex() throws IOException {
        File f = new File(srcDataDirectory);
        // iterate through all the files root at the data src directory
        Queue<File> q = new LinkedList<File>();
        q.add(f);

        File fi;

        while((fi=q.poll())!= null)
        {
            if(fi.isFile()){
                String s1 = readFileContent(fi);
                indexer.addDoc(s1, fi.getName());
            }
            else if(fi.isDirectory()){ // a directory itself
                Collections.addAll(q, fi.listFiles());
            }
        }
        indexer.close();
    }

    private String readFileContent(File f) {
        StringBuilder file_contents = new StringBuilder(3* IndexerConfig.FS_BLOCK_SIZE);
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

}

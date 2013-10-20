package indexer;

import com.google.common.io.Files;
import indexer.channel.SingleFileIndexer;
import junit.framework.TestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Created by User: ting
 * Date: 10/13/12
 * Time: 12:09 PM
 */
public class TestNGramIndexer extends TestCase{

    public void testNGramIndexer() throws IOException {
        String sourceFile = "src/test/resources/corpora/names/1.txt";
        String outputDir = "src/test/resources/indexes/nameIndex/";



        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        // Add documents to the index
        long start = System.currentTimeMillis();
        Directory indexDir = new NIOFSDirectory(new File(outputDir));
        NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
        NGramIndexer indexer = new NGramIndexer(analyzer,indexDir,true);
        SingleFileIndexer fileIndexer = new SingleFileIndexer(sourceFile,indexer);

        fileIndexer.buildIndex();

        long end = System.currentTimeMillis();
        System.out.println("Time spend: " + (end - start) + " millisec");




    }
    
    
    
}

package indexer;

import indexer.channel.SingleFileIndexer;
import indexer.channel.SingleFileNGramToSelectivityMapper;
import junit.framework.TestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 1/18/14
 * Time: 10:03 PM
 */
public class TestAlphaBetaSelectivityNGramIndexer extends TestCase {
    public void testAlphaBetaSelectivityNGramIndexer() throws IOException {
        String sourceFile = "src/test/resources/corpora/names/1.txt";
        String outputDir = "src/test/resources/indexes/nameIndex/";
        int maxNGramLength = 2;

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        // Add documents to the index
        long start = System.currentTimeMillis();
        Directory indexDir = new NIOFSDirectory(new File(outputDir));

        SingleFileNGramToSelectivityMapper mapper = new SingleFileNGramToSelectivityMapper(sourceFile,
                                                                                            maxNGramLength);


        TokenFilter abFilter = new AlphaBetaFilter(0.9,0.1,mapper.getNGramToSelectivityMap());
        NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(maxNGramLength,abFilter));
        NGramIndexer indexer = new NGramIndexer(analyzer,indexDir,true);
        SingleFileIndexer fileIndexer = new SingleFileIndexer(sourceFile,indexer);

        fileIndexer.buildIndex();

        long end = System.currentTimeMillis();
        System.out.println("Time spend: " + (end - start) + " millisec");

    }

    public void testSingleFileNGramToSelectivityMapper() throws IOException {
        int maxNGramLength = 2;
        String sourceFile = "src/test/resources/corpora/names/1.txt";

        SingleFileNGramToSelectivityMapper mapper = new SingleFileNGramToSelectivityMapper(sourceFile,
                maxNGramLength);

        Map<String,Double> ngramSelMap = mapper.getNGramToSelectivityMap();
        assertEquals(0.2,ngramSelMap.get("am"));
        assertEquals(0.2,ngramSelMap.get("Ja"));
        assertEquals(0.1,ngramSelMap.get("oa"));
    }

    public void testAlphaBetaFilter() throws IOException {
        Map<String,Double> gramToSelMap = new HashMap<String, Double>();
        gramToSelMap.put("aa",0.649);
        gramToSelMap.put("a",0.65);
        gramToSelMap.put("ac",0.4);
        gramToSelMap.put("c",0.48);
        AlphaBetaFilter filter = new AlphaBetaFilter(0.8,0.01,gramToSelMap);

        assertTrue(filter.shouldFilter("aa"));
        assertTrue(!filter.shouldFilter("a"));
        assertTrue(!filter.shouldFilter("ac"));
        assertTrue(!filter.shouldFilter("c"));
    }
}

package Query;

import indexer.NGramAnalyzer;
import indexer.NGramIndexer;
import indexer.NGramTokenizer;
import indexer.channel.SingleFileIndexer;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import query.IndexQuery;
import query.NFATreeIndexQuery;
import query.filters.RegexLuceneResultFilter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 12/27/13
 * Time: 11:07 PM
 */
public class TestTreeVsNFAQueryFormulation extends TestCase{
    public final NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
    private final Directory index = new RAMDirectory();
    private final String sourceFile = "src/test/resources/firstNames/firstname.txt";
    @Before
    public void setUp(){


        try{
            NGramIndexer indexer = new NGramIndexer(analyzer,index,true);
            SingleFileIndexer fileIndexer = new SingleFileIndexer(sourceFile,indexer);
            fileIndexer.buildIndex();
        }catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }


    public void testTreeVsNFAQueryPerformance() throws IOException
    {
        IndexReader reader = IndexReader.open(index);

        String[] patterns = {"(J|M)aa*y","adi*l","a(d|i)*l","tin*g","r(o|u)ger","aa*ron","s(a|e)ba*stian","z(e|o)*n",
                                "timothy"};

        System.out.println("\t\tTree Based\tNFA Based\tScan");
        for(String pattern:patterns)
        {
            // Prune for a dummy regex
            long time = System.nanoTime();
            IndexQuery treeQuery = new IndexQuery(reader);
            RegexLuceneResultFilter filter = new RegexLuceneResultFilter(pattern,"content");
            Set<Document> s1 = treeQuery.getPrunedSet(pattern,filter);
            long time1 = System.nanoTime();

            NFATreeIndexQuery nfaQuery = new NFATreeIndexQuery(reader);
            Set<Document> s2 = nfaQuery.getPrunedSet(pattern,filter);
            long time2 = System.nanoTime();

            List<String> baseline = grep(sourceFile,pattern);
            long time3 = System.nanoTime();

            assert s1.size() == s2.size();
            assert s2.size() == baseline.size();

            System.out.println(pattern + "\t\t" + s1.size() + "(" + (time1 - time)/1000000 +")" +
                                "\t" + s2.size() + "(" + (time2 - time1)/1000000 +")" +
                                "\t" + baseline.size() + "(" + (time3 - time2)/1000000 + ")" );
        }
        reader.close();
    }

    /**
     *
     * @param file the number of file
     * @param regex the regular expression
     * @return the lines in the file which match the regex
     */
    public List<String> grep(String file, String regex) throws FileNotFoundException {
        Scanner f = new Scanner(new FileInputStream(file));
        List<String> result = new LinkedList<String>();
        Pattern pattern = Pattern.compile(regex);
        while(f.hasNextLine()) {
            String line = f.nextLine();
            Matcher matcher = pattern.matcher(line);
            if(matcher.find(0))
            {
                result.add(line);
            }
        }
        return result;
    }



    @After
    public void tearDown()
    {

    }
}

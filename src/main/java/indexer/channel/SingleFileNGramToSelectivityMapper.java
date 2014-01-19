package indexer.channel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import Utility.Utility;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 1/18/14
 * Time: 9:13 PM
 */
public class SingleFileNGramToSelectivityMapper {
    private String fileName;
    private Map<String,Double> ngramToSelectivityMap;
    private int MAX_NGRAM_LENGTH;

    public SingleFileNGramToSelectivityMapper(String fileName,int maxNGramLength)
    {
        this.fileName = fileName;
        this.MAX_NGRAM_LENGTH = maxNGramLength;
    }

    /**
     * This method will scan each file line and extract all n-gram up to length
     * K from the line. The mapping from docID to a set of k-grams will be fed
     * into a Lucene indexer.
     * @throws java.io.IOException when reading from a file or writing to lucene index
     */
    public Map<String,Double> getNGramToSelectivityMap() throws IOException {
        ngramToSelectivityMap = new HashMap<String,Double>();
        Scanner f = new Scanner(new FileInputStream(fileName));

        int count = 0;
        // first count the term frequency of each gram
        while(f.hasNextLine()) {
            count ++;
            Set<String> uniqueNGrams = Utility.getUniqueNGrams(f.nextLine(),
                                                                       MAX_NGRAM_LENGTH);
            for(String ngram:uniqueNGrams)
            {
                Double v = ngramToSelectivityMap.get(ngram);
                if(v == null)
                {   ngramToSelectivityMap.put(ngram, (double) 1);

                }else
                {
                    ngramToSelectivityMap.put(ngram, (double)(v+1));
                }
            }
        }
        // next compute the selectivity
        for(String ngram: ngramToSelectivityMap.keySet())
        {
            double frequency = ngramToSelectivityMap.get(ngram);
            ngramToSelectivityMap.put(ngram,frequency/count);
        }

        return ngramToSelectivityMap;
    }

}

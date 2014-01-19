package indexer;

import indexer.trex.IndexerConfig;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import Utility.Utility;


/**
 * Created by User: ting
 * Date: 8/31/12
 * Time: 8:23 PM
 * The first implementation which just parses the entire document and loads
 * its gram set into a hashset iterator during object init;
 * TODO: replace it with a more efficient streaming version
 */

public final class NGramTokenizer extends Tokenizer{

    private Iterator<String> iterator;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private int MAX_NGRAM_LENGTH = 2;
    private final TokenFilter filter;

    /**
     * @param kgramLength the length of max index grams
     */
    public NGramTokenizer(int kgramLength) {
      this(kgramLength,null);
    }

    public NGramTokenizer(int kgramLength,TokenFilter filter) {
        MAX_NGRAM_LENGTH = kgramLength;
        this.filter = filter;
    }

    public NGramTokenizer setReader(Reader reader)
    {
        StringBuilder fileContent = new StringBuilder(3* IndexerConfig.FS_BLOCK_SIZE);
        try{
            char[] bs = new char[IndexerConfig.FS_BLOCK_SIZE];
            int len;
            while((len=reader.read(bs))!= -1){
                fileContent.append(new String(bs,0,len));
            }
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        String fileString = fileContent.toString();
        Set<String> grams = Utility.getUniqueNGrams(fileString, MAX_NGRAM_LENGTH);

        if(filter != null)
        {
            Set<String> tmpGramSet = new HashSet<String>();
            for(String gram: grams)
            {
                if(!filter.shouldFilter(gram))
                {
                    tmpGramSet.add(gram);
                }
            }
            grams = tmpGramSet;
        }

        iterator = grams.iterator();
        return this;
    }


    @Override
    public boolean incrementToken() throws IOException {
        if(iterator == null) return false;
        if(iterator.hasNext())
        {
            termAtt.setEmpty();
            termAtt.append(iterator.next());
            return true;
        }
        return false;
    }
}

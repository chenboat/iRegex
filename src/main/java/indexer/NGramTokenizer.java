package indexer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by User: ting
 * Date: 8/31/12
 * Time: 8:23 PM
 */
public final class NGramTokenizer extends Tokenizer{
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private static final int MAX_NGRAM_LENGTH = 2;
    private Iterator<String> iterator;

    /**
     * The first implementation which just parses the entire document and loads
     * its gram set into a hashset iterator during object init;
     * TODO: replace it with a more efficient streaming version
     * @param reader
     */
    public NGramTokenizer(Reader reader) {
        StringBuilder fileContent = new StringBuilder(3*IndexerConfig.FS_BLOCK_SIZE);
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
        HashSet<String> grams = new HashSet<String>();
        for(int i = 0; i < fileString.length(); i++){
            for (int j = 1; j <= MAX_NGRAM_LENGTH && (i+j) <= fileString.length(); j++)
            {
                String gram = fileString.substring(i, i+j);
                if(!grams.contains(gram))
                    grams.add(gram);
            }
        }

        iterator = grams.iterator();
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(iterator.hasNext())
        {
            termAtt.setEmpty();
            termAtt.append(iterator.next());
            return true;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

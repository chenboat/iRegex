package indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

/**
 * Created by User: ting
 * Date: 8/30/12
 * Time: 9:17 PM
 * A customized analyzer which extracts all distinct n-grams for the file reader stream
 */
public final class NGramAnalyzer extends Analyzer{
    NGramTokenizer tokenizer;

    public NGramAnalyzer(NGramTokenizer nGramTokenizer){
        tokenizer = nGramTokenizer;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return tokenizer.setReader(reader);
    }
    
    public static void main(String[] args) throws Exception
    {
        String text = "Hello";
        System.out.println("");
        Analyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
        System.out.println("\t" + analyzer.getClass().getName() + ":");
        System.out.print("\t\t");
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        while (stream.incrementToken()) {
            if(stream.hasAttribute(CharTermAttribute.class))
            {
                CharTermAttribute attr = stream.getAttribute(CharTermAttribute.class);
                System.out.print("[" + attr.toString() + "] ");
            }
        }
        stream.close();
    }
}

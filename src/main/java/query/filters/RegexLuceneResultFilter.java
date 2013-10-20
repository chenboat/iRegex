package query.filters;

import org.apache.lucene.document.Document;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/20/13
 * Time: 4:08 PM
 */
public class RegexLuceneResultFilter implements LuceneResultFilter {
    private final Pattern regex;
    private final String fieldName;
    /**
     *
     * @param regex a reg
     * @param fieldName the fieldname in the lucene document which stores the document content
     */
    public RegexLuceneResultFilter(String regex, String fieldName)
    {
        this.regex = Pattern.compile(regex);  // compile the regex for repeated use
        this.fieldName = fieldName;
    }

    @Override
    public boolean filter(Document document) {
        Matcher m = regex.matcher(document.get(fieldName));
        return m.find(0);
    }
}

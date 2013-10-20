package query.filters;

import org.apache.lucene.document.Document;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 10/20/13
 * Time: 4:03 PM
 * Given a lucene document, the interface will decide if it will remain in the final result
 */
public interface LuceneResultFilter {
    public boolean filter(Document document);
}

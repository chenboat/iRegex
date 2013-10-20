package indexer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ting
 * Date: 10/19/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Indexer {
    public void addDoc(String content, String id) throws IOException;
    public void close() throws IOException;
}

package indexer.channel;

import indexer.Indexer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: ting
 * Date: 10/19/13
 * Time: 3:42 PM
 * This indexer will index each line in a single file
 */
public class SingleFileIndexer {
    private final String filename;
    private final Indexer indexer;
    public SingleFileIndexer(String dataSrc, Indexer indexer)
    {
        filename = dataSrc;
        this.indexer = indexer;
    }

    /**
     * This method will scan each file line and extract all n-gram up to length
     * K from the line. The mapping from docID to a set of k-grams will be fed
     * into a Lucene indexer.
     * @throws java.io.IOException when reading from a file or writing to lucene index
     */
    public void buildIndex() throws IOException {
        Scanner f = new Scanner(new FileInputStream(filename));

        int count = 0;
        while(f.hasNextLine())
        {
            indexer.addDoc(f.next(),new Integer(count ++).toString());
        }
        indexer.close();
    }
}

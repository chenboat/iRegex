package indexer;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 1/18/14
 * Time: 9:49 PM
 *
 * An interface deciding if a given gram should be filtered (i.e., left out without indexing) during indexing
 * phase
 */
public interface TokenFilter {
    /**
     *
     * @param gram input string
     * @return  true if the input should filtered (i.e., left out without indexing) during indexing
     */
    public boolean shouldFilter(String gram);
}

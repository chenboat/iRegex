package indexer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 1/18/14
 * Time: 5:49 PM
 *  Every ngram n the filter declares as selective must satisfy the following condition
 *    1. the selectivity of n is <= alpha
 *    2. there does not exist any substring s of n, such that
 *              selectivity(s) - selectivity(n) <= beta
 */
public final class AlphaBetaFilter implements TokenFilter {
    protected Set<String> selectiveNGrams;
    protected final double alpha,beta;

    public AlphaBetaFilter(double a, double b, Map<String,Double> ngramToSelMap)
    {
        this.alpha = a;
        this.beta = b;
        initSelectiveNGramSet(ngramToSelMap);
    }

    private void initSelectiveNGramSet(Map<String, Double> ngramToSelMap) {
        selectiveNGrams = new HashSet<String>();
        if (ngramToSelMap != null) {
            for(String ngram: ngramToSelMap.keySet())
            {
                if(ngramToSelMap.get(ngram) > alpha)      // not an alpha selective gram
                    continue;

                if(ngram.length() == 1)
                {
                    selectiveNGrams.add(ngram);
                    continue;
                }

                String pfx = ngram.substring(0,ngram.length() - 1);
                String sfx = ngram.substring(1);
                assert ngramToSelMap.containsKey(pfx);
                assert ngramToSelMap.containsKey(sfx);

                if((ngramToSelMap.get(pfx) - ngramToSelMap.get(ngram) <= beta) ||
                    ngramToSelMap.get(sfx) - ngramToSelMap.get(ngram) <= beta)  // not a beta selective gram
                {
                    continue;
                }
                selectiveNGrams.add(ngram);
            }
        }
    }

    public boolean shouldFilter(String ngram)
    {
        return selectiveNGrams == null || !selectiveNGrams.contains(ngram);
    }
}

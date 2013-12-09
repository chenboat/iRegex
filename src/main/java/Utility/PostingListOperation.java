package Utility;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/28/13
 * Time: 10:16 PM
 */
public class PostingListOperation {
    /**
     *
     * @param leftSet: null represents a universal set
     * @param rightSet: null is all
     * @return the result of union; null means all
     */
    public static Set<Integer> union(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null || rightSet == null)
            return null;
        return Sets.union(leftSet, rightSet);
    }

    /**
     *
     * @param leftSet: null represents a universal set
     * @param rightSet: null is all
     * @return result of intersection
     */
    public static Set<Integer> intersect(Set<Integer> leftSet, Set<Integer> rightSet) {
        if(leftSet == null)
            return rightSet;
        else if(rightSet == null)
            return leftSet;
        return Sets.intersection(leftSet, rightSet);
    }
}

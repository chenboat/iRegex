package Utility;

import java.util.Vector;
import indexer.StringValuePair;

public class VectorSort {
	
	public static void main(String[] args){
		Vector<StringValuePair> v = new Vector<StringValuePair>();
		v.add(new StringValuePair("abd","a"));
		//v.add(new StringValuePair("baa","a"));
		//v.add(new StringValuePair("aab","a"));
		//v.add(new StringValuePair("aba","a"));
		
		System.out.println("============ Sort ============");
		sortStringValuePairs(v);
		for(int i = 0; i < v.size(); i++){
			System.out.println(v.elementAt(i));
		}
		System.out.println("============ Sort on Sfx ============");
		sortStringValuePairOnSfx(v);
		for(int i = 0; i < v.size(); i++){
			System.out.println(v.elementAt(i));
		}
	}
	
	
	
	/**
	 * @param v a vector of stringvalue pairs
	 * quicksort a vector of stringvalue pairs based on the key strings' suffix 
	 */
	public static void sortStringValuePairs(Vector<StringValuePair> v) {
		sortStringValuePairs_aux(v,0, v.size() - 1);
	}

	private static void sortStringValuePairs_aux(Vector<StringValuePair> v,
			int head, int tail) {
		if (head < tail) {
			int pivotIndex = (head + tail)/2;
			StringValuePair pivot = v.elementAt(pivotIndex);
			int i = head - 1;
			int j = tail + 1;
			do {
				do i++; while (! leq(pivot, v.elementAt(i)));
				do j--; while (! leq(v.elementAt(j),pivot));
				if (i < j) swap(v,i, j);
			} while (i < j);
			if (i == j) {
				sortStringValuePairs_aux(v,head, j - 1);
				sortStringValuePairs_aux(v,i + 1, tail);
			} else {
				sortStringValuePairs_aux(v,head, j);
				sortStringValuePairs_aux(v,i, tail);
			}
		}
	}


	private static boolean leq(StringValuePair p1,
			StringValuePair p2){
		return (p1.key.compareTo(p2.key) <= 0);
	}
	
	  /**
	 * @param v a vector of stringvalue pairs
	 * quicksort a vector of stringvalue pairs based on the key strings' suffix 
	 */
	public static void sortStringValuePairOnSfx(Vector<StringValuePair> v) {
		  sortStringValuePairOnSfx_aux(v,0, v.size() - 1);
	}

	private static void sortStringValuePairOnSfx_aux(Vector<StringValuePair> v,
			  				int head, int tail) {
	    if (head < tail) {
	      int pivotIndex = (head + tail)/2;
	      StringValuePair pivot = v.elementAt(pivotIndex);
	      int i = head - 1;
	      int j = tail + 1;
	      do {
	        do i++; while (! leqOnSfx(pivot, v.elementAt(i)));
	        do j--; while (! leqOnSfx(v.elementAt(j),pivot));
	        if (i < j) swap(v,i, j);
	      } while (i < j);
	      if (i == j) {
	    	  sortStringValuePairOnSfx_aux(v,head, j - 1);
	    	  sortStringValuePairOnSfx_aux(v,i + 1, tail);
	      } else {
	    	  sortStringValuePairOnSfx_aux(v,head, j);
	    	  sortStringValuePairOnSfx_aux(v,i, tail);
	      }
	    }
	}


	private static boolean leqOnSfx(StringValuePair p1,
			  						StringValuePair p2){
		  return (p1.key.substring(1).compareTo(p2.key.substring(1)) 
				  <= 0);
	}

	private static void swap (Vector v,int i, int j) {
	    Object obj = v.elementAt(i);
	    v.setElementAt(v.elementAt(j), i);
	    v.setElementAt(obj, j);
	}


}

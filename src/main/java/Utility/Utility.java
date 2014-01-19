package Utility;

import java.io.File;
import java.util.*;

public class Utility {
	
	public static void main(String[] args){
		int[] a1 = {0,2,4,10,12};
		int[] a2 = {1,2,10,14};
		int[] a3 = {};
		int[] i12 = Utility.intersectSortedArray(a1, a2);
		System.out.println("Intersect a1 and a2");
		for(int i = 0; i < i12.length; i++){
			System.out.print(i12[i]+" ");
		}
		System.out.println();
		
		System.out.println("Union a1 and a2");
		int[] u12 = Utility.unionSortedArray(a1, a2);
		for(int i = 0; i < u12.length; i++){
			System.out.print(u12[i]+" ");
		}
		System.out.println();
		
		System.out.println("Intersect a1 and a3");
		int[] i13 = Utility.intersectSortedArray(a1, a3);
		for(int i = 0; i < i13.length; i++){
			System.out.print(i13[i]+" ");
		}
		System.out.println();
		
		System.out.println("Union a1 and a3");
		int[] u13 = Utility.unionSortedArray(a1, a3);
		for(int i = 0; i < u13.length; i++){
			System.out.print(u13[i]+" ");
		}
		System.out.println();
		
	}
	
	public static String transformText(String seq){
		String str = "";
		for(int i = 0; i < seq.length(); i++){
			String ch = seq.substring(i, i+1);
			char c = seq.charAt(i);
			if(Character.isLetterOrDigit(c)){
				str = str + "("+ch +")";
			}else if(Character.isWhitespace(c)){
				if(ch.compareTo("\t")==0){
					str = str + "(\\t)";
				}else if(ch.compareTo("\r")==0){
					str = str + "(\\r)";
				}else if(ch.compareTo("\n")==0){
					str = str + "(\\n)";
				}else if(ch.compareTo(" ")==0){
					str = str + "( )";
				}
			}else if(Character.isISOControl(c)){
				str = str + "(\\c)";
			}else{
				str = str + "("+ch+")";
			}
		}
		return str;
	}
	
	public static String byte2bits(byte b){
		String result = "";
		for(int bit = 7; bit >= 0; bit--) {
            if ((b & (2 << bit)) > 0)
            	result = result + "1";
            else
            	result = result + "0";
		}
		return result;
	}
		
	public static boolean[] intersectBitmap(boolean[] b1,boolean[] b2){
		if(b1.length != b2.length){
			System.out.println("Fatal Error in Utility Function: two boolean array sizes not equal");
			System.exit(1);
		}
		boolean[] r = new boolean[b1.length];
		for(int i = 0; i < b1.length; i++){
			r[i] = b1[i] && b2[i];
		}
		return r;
	}
	
	
	/**
	 * @param arr1: an ascending sorted array of distinct integers; null means the set of all integers
	 * @param arr2: an ascending sorted array of distinct integers;
	 * @return the intersection of arr1 and arr2.
	 */
	public static int[] intersectSortedArray(int[] arr1,int[] arr2){
		if(arr1 == null) return arr2;
		if(arr2 == null) return arr1;
		int[] lst = new int[arr1.length + arr2.length];
		int p1 = 0; int p2 = 0;
		int ctr = 0;
		while(p1 < arr1.length && p2 < arr2.length){
			if(arr1[p1] == arr2[p2]){
				lst[ctr++] = arr1[p1];
				p1++;
				p2++;
			}else if(arr1[p1] > arr2[p2]){
				p2++;
			}else{
				p1++;
			}
		}
		int[] returnLst = new int[ctr];
		System.arraycopy(lst, 0, returnLst, 0, ctr);
		return returnLst;
	}
	
	/**
	 * @param arr1: an ascending sorted array of distinct integers; null means the set of all integers
	 * @param arr2: an ascending sorted array of distinct integers;
	 * @return the intersection of arr1 and arr2.
	 */
	public static int[] unionSortedArray(int[] arr1,int[] arr2){
		if(arr1 == null || arr2 == null) return null;
		int[] lst = new int[arr1.length + arr2.length];
		int p1 = 0; int p2 = 0;
		int ctr = 0;
		while(p1 < arr1.length && p2 < arr2.length){
			if(arr1[p1] == arr2[p2]){
				lst[ctr++] = arr1[p1];
				p1++;
				p2++;
			}else if(arr1[p1] > arr2[p2]){
				lst[ctr++] = arr2[p2];
				p2++;
			}else{
				lst[ctr++] = arr1[p1];
				p1++;
			}
		}
		while(p1 < arr1.length) { lst[ctr++] = arr1[p1++]; }
		while(p2 < arr2.length) { lst[ctr++] = arr2[p2++]; }
		
		int[] returnLst = new int[ctr];
		System.arraycopy(lst, 0, returnLst, 0, ctr);
		return returnLst;
	}
	
	
	/**
	 * @param v1 a list of int
	 * @param v2 a list of int
	 * @return the list of integers which is in v2 but not in v1
	 */
	public static Vector<String> setDiff(Vector<String> v1,Vector<String> v2){
		Hashtable<String,String> hash1 = new Hashtable<String,String>();
		Vector<String> diff = new Vector<String>();
		for(int i = 0; i < v1.size(); i++){
			hash1.put(v1.elementAt(i), v1.elementAt(i));
		}
		for(int i = 0; i < v2.size(); i++){
			if(!hash1.containsKey(v2.elementAt(i)))
				diff.add(v2.elementAt(i));
		}
		return diff;
	}

	public static Hashtable<String,String> intersectResultLst(
			Vector<String> l1,
			Vector<String> l2){
		Hashtable<String,String> h = new Hashtable<String,String>();
		if(l1 == null && l2 == null)
			return null;
		else if(l1 == null){
			for(int i = 0; i < l2.size(); i++){
				h.put(l2.elementAt(i),l2.elementAt(i));
			}
		}else if(l2 == null){
			for(int i = 0; i < l1.size(); i++){
				h.put(l1.elementAt(i),l1.elementAt(i));
			}
		}else{
			Hashtable<String,String> h1 = new Hashtable<String,String>();
			for(int i = 0; i < l1.size(); i++){
				h1.put(l1.elementAt(i),l1.elementAt(i));
			}
			Hashtable<String,String> h2 = new Hashtable<String,String>();
			for(int i = 0; i < l2.size(); i++){
				h2.put(l2.elementAt(i),l2.elementAt(i));
			}
			Enumeration<String> h1_keys = h1.keys();
			while(h1_keys.hasMoreElements()){
				String k = h1_keys.nextElement();
				if(h2.get(k) != null)
					h.put(k, k);
			}
		}
		return h;
	}
	
	public static Hashtable<String,String> intersectBitmapHash(
			Hashtable<String,String> h1,
			Hashtable<String,String> h2){
		Hashtable<String,String> h = new Hashtable<String,String>();
		if(h1 == null && h2 == null)
			return null;
		else if(h1 == null){
			return h2;
		}else if(h2 == null){
			return h1;
		}else{
			Enumeration<String> h1_keys = h1.keys();
			while(h1_keys.hasMoreElements()){
				String k = h1_keys.nextElement();
				if(h2.get(k) != null)
					h.put(k, k);
			}
		}
		return h;
	}
	
    // quicksort a[left] to a[right]
    public static void quicksort(int[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(int[] a, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[++i], a[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[right], a[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, i, j);                      // swap two elements into place
        }
        exch(a, i, right);                      // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(int x, int y) {
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(int[] a, int i, int j) {
        int swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

	
	
	/* Test if one multiset of numbers exists in another multiset of multiset of numbers.*/
	public static boolean exist(Vector<Vector<Integer>> orConstraintSet,
					Vector<Integer> branch){
		for(int i = 0; i < orConstraintSet.size(); i++){
			if(equal(orConstraintSet.elementAt(i),branch))
				return true;
		}
		return false;
	}
	/*
	 * Test if two multisets of numbers are equal
	 */
	public static boolean equal(Vector<Integer> v1,Vector<Integer> v2){
		Hashtable<Integer,Integer> h1 = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> h2 = new Hashtable<Integer,Integer>();
		for(int i = 0; i < v1.size(); i++){
			h1.put(v1.elementAt(i), v1.elementAt(i));
		}
		for(int i = 0; i < v2.size(); i++){
			h2.put(v2.elementAt(i), v2.elementAt(i));
		}
		if(h1.size() != h2.size()) return false;
		Enumeration<Integer> h1Keys = h1.keys();
		while(h1Keys.hasMoreElements()){
			Integer k = h1Keys.nextElement();
			if(!h2.containsKey(k))
				return false;
		}
		return true;
	}
	

	
	
	// Deletes all files and subdirectories under dir together with dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns false.
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}
	
	// Deletes all files and subdirectories under dir if dir exists
	// dir is not deleted
	// dir is created if not exist
	public static void ClearOutputDir(String outputDir){
		if(new File(outputDir).exists()){
			//Remove all existing files in the directory
			File[] children = new File(outputDir).listFiles();
			if(children.length > 0){
				for(int i = 0; i < children.length; i++){
					Utility.deleteDir(children[i]);
				}
			}
		}else{
			System.err.println("Output Index Directory doesnt exist. Creating...");
			boolean success = new File(outputDir).mkdirs();
			if(success)
				System.out.println("Output Index Directory created");
			else{
				System.err.println("Creating output index directory failed...");
				System.exit(1);
			}
		}
	}

    /**
     *
     * @param fileString the string to extract the ngrams
     * @param MAX_NGRAM_LENGTH max ngram length
     * @return all the unique ngrams of length up to MAX_NGRAM_LENGTH
     */

    public static Set<String> getUniqueNGrams(String fileString, int MAX_NGRAM_LENGTH) {
        Set<String> grams = new HashSet<String>();
        for(int i = 0; i < fileString.length(); i++){
            for (int j = 1; j <= MAX_NGRAM_LENGTH && (i+j) <= fileString.length(); j++)
            {
                String gram = fileString.substring(i, i+j);
                if(!grams.contains(gram))
                    grams.add(gram);
            }
        }
        return grams;
    }


}

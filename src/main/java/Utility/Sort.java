package Utility;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Vector;

public class Sort {
	
	public static void main(String[] args){
		Vector<Integer> lst = new Vector<Integer>();
		try{
			FileInputStream fs = new FileInputStream("/scratch/SIGMOD09/index/W3Detag/test/array.txt");
			BufferedInputStream bufInput = new BufferedInputStream(fs,4*1024);
			
			byte[] lengthByte = new byte[4];
			// Read enough bytes to make a leaf page
			int i = 0;
			while(bufInput.read(lengthByte)!= -1){
				int length = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
				(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);
				lst.add(new Integer(length));
			}
			fs.close();
			
		}catch(Exception e) {e.printStackTrace();}
		
		int[] a = new int[lst.size()];
		int col = 1;
		for(int i = 0; i < a.length; i++){
			a[i] = lst.elementAt(i).intValue();
			System.out.print(a[i]+" ");
			if(col% 100 == 0){
				col = 1;
				System.out.println();
			}else
				col++;
		}
		System.out.println();
		System.out.println("After sort");
		
		
		Sort.quicksortInt(a,0,a.length-1);
		col = 1;
		for(int i = 0;i < a.length; i++){
			System.out.print(a[i]+" ");
			if(col% 100 == 0){
				col = 1;
				System.out.println();
			}else
				col++;
		}
		System.out.println();
	}
	
	public static void quicksort(Comparable[] a, int left, int right){
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(Comparable[] a, int left, int right) {
    	int mid = (left+right)/2;
    	
    	
    	
    	Comparable swap = a[mid];
        a[mid] = a[right];
        a[right] = swap;
    	
    	int i = left - 1;
        int j = right;
        while (true) {
            while (a[++i].compareTo(a[right]) < 0)      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (a[right].compareTo(a[--j]) < 0)      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            
            // swap two elements into place
           swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
        // swap with partition element
        swap = a[i];
        a[i] = a[right];
        a[right] = swap;
        
        return i;
    }
    
    
    /**
     * @param a: the int array
     * @param left: the index of the first element
     * @param right: the index of the last element (not the array size)
     */
    public static void quicksortInt(int[] a, int left, int right){
        if (right <= left) return;
        int i = partitionInt(a, left, right);
        quicksortInt(a, left, i-1);
        quicksortInt(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partitionInt(int[] a, int left, int right) {
    	int mid = (left+right)/2;
    	
    	double rand = Math.random();
    	mid = left + (int)Math.round(rand * (right-left));
    	
    	int swap = a[mid];
        a[mid] = a[right];
        a[right] = swap;
    	
    	int i = left - 1;
        int j = right;
        while (true) {
            while (a[++i] < a[right])      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (a[right] < a[--j])      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            
            // swap two elements into place
           swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
        // swap with partition element
        swap = a[i];
        a[i] = a[right];
        a[right] = swap;
        
        return i;
    }
    
    
    /**
     * @param a: an array of integer pairs, the first int of a pair is the key
     * @param left: the left inclusive key boundary
     * @param right: the right inclusive key bounary which is one less than the right inclusive boundary
     * @param ascending: sort the array in ascending or descending order
     */
    public static void sortPairsOfInts(int[] a, int left,int right,boolean ascending){
    	if (right <= left) return;
    	int i = partitionIntPairs(a, left, right,ascending);
    	sortPairsOfInts(a, left, i-1,ascending);
    	sortPairsOfInts(a, i+1,right,ascending);
    }
    
    // partition a[left] to a[right], assumes left < right
    private static int partitionIntPairs(int[] a, int left, int right,boolean ascending) {
    	int mid = (left+right)/2;
    	int swapKey = a[2*mid];
    	int swapValue = a[2*mid+1];
        a[2*mid] = a[2*right];
        a[2*mid+1] = a[2*right+1];
        a[2*right] = swapKey;
    	a[2*right+1] = swapValue;
    	
    	
    	int i = left - 1;
        int j = right;
        while (true) {
        	if(ascending){
        		while (a[2*(++i)] < a[2*right])      // find item on left to swap
                ;						     // a[right] acts as sentinel
        		while (a[2*right] < a[2*(--j)])      // find item on right to swap
        			if (j == left) break;           // don't go out-of-bounds
        		if (i >= j) break;                  // check if pointers cross
        	}else{
        		while (a[2*(++i)] > a[2*right])      // find item on left to swap
                    ;						     // a[right] acts as sentinel
            	while (a[2*right] > a[2*(--j)])      // find item on right to swap
            			if (j == left) break;           // don't go out-of-bounds
            	if (i >= j) break;                  // check if pointers cross
        	}
            // swap two elements into place
           swapKey = a[2*i];
           swapValue = a[2*i + 1];
           a[2*i] = a[2*j];
           a[2*i+1] = a[2*j+1]; 
           a[2*j] = swapKey;
           a[2*j+1] = swapValue;
        }
        // swap with partition element
        swapKey = a[2*i];
        swapValue = a[2*i+1];
        a[2*i] = a[2*right];
        a[2*i+1] = a[2*right+1];
        
        a[2*right] = swapKey;
        a[2*right+1] = swapValue;
        
        return i;
    }
    
   
}

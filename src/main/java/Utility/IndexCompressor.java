package Utility;

import java.util.ArrayList;
import java.util.Vector;

public class IndexCompressor {
	public static final int BYTES_INT = 4;
	public static void main(String[] args){
		int[] nums = {5,824,214577};
		for(int i = 0; i< nums.length; i++){
			System.out.print(nums[i]+":\t");
			byte[] encoded = VBEncodeNumber(nums[i]); 
			for(int j = 0; j < encoded.length; j++){
				Byte B = new Byte(encoded[j]);
				System.out.print(B+ " ");
				
				
			}
			System.out.println();
		}
	}
	
	public static byte[] VBEncodeNumber(int n){
		int cur = n;
		int max_length = 4;
		byte[] bytes = new byte[max_length];
		int cnt = 0;
		while(true){
			bytes[max_length-cnt-1] = (byte) (cur%128);
			cnt ++;
			if(cur < 128)
				break;
			cur = cur / 128;
		}
		bytes[max_length - 1] += 128;
		byte[] return_bytes = new byte[cnt];
		System.arraycopy(bytes, max_length - cnt, return_bytes, 0, cnt);
		return return_bytes;
	}
	
	/**
	 * @param bs: an encoded array of bytes
	 * @param start: the start byte index inclusive
	 * @param end: the end byte index inclusive
	 * @return a vector of integers decoded
	 */
	public static Vector<Integer> VBDECODE(byte[] bs,int start,int end){
		Vector<Integer> numbers = new Vector<Integer>();
		int n = 0;
		for(int i = start; i <= end; i++){
			int in = (int)bs[i] & 0xff;
			//System.out.println("Convert " + bs[i] + " to " + in);
			if(in < 128)
				n = 128 * n + in;
			else{
				n = 128 * n + (in - 128);
				numbers.add(new Integer(n));
				n = 0;
			}
		}
		return numbers;
		
	}
	
	/**
	 * @param bs: an encoded array of bytes
	 * @param start: the start byte index inclusive
	 * @param end: the end byte index inclusive
	 * @return a byte array of integers decoded
	 */
	public static byte[] VBDECODE2ByteArray(byte[] bs,int start,int end){
		Vector<Integer> numbers = new Vector<Integer>();
		int n = 0;
		for(int i = start; i <= end; i++){
			int in = (int)bs[i] & 0xff;
			//System.out.println("Convert " + bs[i] + " to " + in);
			if(in < 128)
				n = 128 * n + in;
			else{
				n = 128 * n + (in - 128);
				numbers.add(new Integer(n));
				n = 0;
			}
		}
		byte[] bArray = new byte[numbers.size() * 4];
		for(int i = 0; i < numbers.size(); i++){
			int t = numbers.elementAt(i).intValue();
			bArray[4*i] = (byte)(t>>24);
			bArray[4*i+1] = (byte)(t>>16);
			bArray[4*i+2] = (byte)(t>>8);
			bArray[4*i+3] = (byte)t;
		}
		return bArray;
	}
	
	
	/**
	 * @param bs: an gap encoded array of bytes
	 * @param start: the start byte index inclusive
	 * @param end: the end byte index inclusive
	 * @return a byte array of integers decoded without using gap
	 */
	public static byte[] GapVBDECODE2ByteArray(byte[] bs,int start,int end){
		Vector<Integer> numbers = new Vector<Integer>();
		int n = 0;
		int prev = 0;
		for(int i = start; i <= end; i++){
			int in = (int)bs[i] & 0xff;
			//System.out.println("Convert " + bs[i] + " to " + in);
			if(in < 128)
				n = 128 * n + in;
			else{
				n = 128 * n + (in - 128);
				numbers.add(new Integer(prev+n));
				prev += n;
				n = 0;
			}
		}
		byte[] bArray = new byte[numbers.size() * 4];
		for(int i = 0; i < numbers.size(); i++){
			int t = numbers.elementAt(i).intValue();
			bArray[4*i] = (byte)(t>>24);
			bArray[4*i+1] = (byte)(t>>16);
			bArray[4*i+2] = (byte)(t>>8);
			bArray[4*i+3] = (byte)t;
		}
		return bArray;
	}
	
	public static byte[] convertIntToBytes(int i){
		byte[] bytes = {(byte)(i >> 24), (byte)(i >> 16) , 
				(byte)(i >> 8), (byte)i};
		return bytes;
	}
	
	public static byte[] convertIntArrayToBytes(ArrayList<Integer> lst){
		int ctr = 0;
		int noIds = lst.size();
		byte[] tmp = new byte[noIds * BYTES_INT]; 
		int prevDocId = 0;
		for(int i = 0 ; i < noIds; i++){
			int tmpId = lst.get(i).intValue();
			int gap = tmpId - prevDocId;
			byte[] firstIdEncoded = IndexCompressor.VBEncodeNumber(gap);
			System.arraycopy(firstIdEncoded, 0, tmp, ctr, firstIdEncoded.length);
			ctr += firstIdEncoded.length;
			prevDocId = tmpId;
		}
		
		byte[] encodedPostingLst = new byte[ctr];
		System.arraycopy(tmp, 0, encodedPostingLst, 0, ctr);
		return encodedPostingLst;
	}
	
	
	/**
	 * @param lst: a list of uncompressed bytes
	 * @param s: the start position;must be a multiple of 4
	 * @param e: the end posting+1: must be a multiple of 4 too
	 * @return a compressed byte array from s to e-1
	 */
	public static byte[] compressByteArray(byte[] lst, int s,int e){
		int ctr = 0;
		int noIds = (e - s) / 4;
		byte[] tmp = new byte[noIds * BYTES_INT]; 
		int prevDocId = 0;
		for(int i = s ; i < e; i += 4){
			int tmpId = lst[i]<<24 | (lst[i+1]&0xff)<<16 | 
						(lst[i+2]&0xff)<<8 | (lst[i+3]&0xff);
			int gap = tmpId - prevDocId;
			byte[] firstIdEncoded = IndexCompressor.VBEncodeNumber(gap);
			System.arraycopy(firstIdEncoded, 0, tmp, ctr, firstIdEncoded.length);
			ctr += firstIdEncoded.length;
			prevDocId = tmpId;
		}
		
		byte[] encodedPostingLst = new byte[ctr];
		System.arraycopy(tmp, 0, encodedPostingLst, 0, ctr);
		return encodedPostingLst;
	}
	
	/**
	 * @param lst: a list of uncompressed bytes
	 * @param s: the start position;must be a multiple of 4
	 * @param e: the end posting+1: must be a multiple of 4 too
	 * @return a compressed byte array from s to e-1
	 */
	public static byte[] compressByteArray(byte[] lst, int s,int e, int startID){
		int ctr = 0;
		int noIds = (e - s) / 4;
		byte[] tmp = new byte[noIds * BYTES_INT]; 
		int prevDocId = startID;
		for(int i = s ; i < e; i += 4){
			int tmpId = lst[i]<<24 | (lst[i+1]&0xff)<<16 | 
						(lst[i+2]&0xff)<<8 | (lst[i+3]&0xff);
			int gap = tmpId - prevDocId;
			byte[] firstIdEncoded = IndexCompressor.VBEncodeNumber(gap);
			System.arraycopy(firstIdEncoded, 0, tmp, ctr, firstIdEncoded.length);
			ctr += firstIdEncoded.length;
			prevDocId = tmpId;
		}
		
		byte[] encodedPostingLst = new byte[ctr];
		System.arraycopy(tmp, 0, encodedPostingLst, 0, ctr);
		return encodedPostingLst;
	}

}

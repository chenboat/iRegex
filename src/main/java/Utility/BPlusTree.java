package Utility;


import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

public class BPlusTree {
	BTreeInternalNode root;
	Stack<BTreeNode> rightPath;   
	public int keyLength;
	public static int size = 8*1024;
	public int IDCtr = 0;
	public final int OUTPUT_BUFFER_SIZE = 20*1024*1024;
	public String outputDir;
	public static final int RECID_LENGTH = 4;
	public static final int PAGEID_LENGTH = 4;
	
	public byte[] leafLevelPageBuffer;
	public byte[] first2LvlInternalPageBuffer;
	public byte[] otherInternalPageBuffer;
	public int leafLevelPageBufferCtr = 0;
	public int first2LvlInternalPageBufferCtr = 0;
	public int otherInternalPageBufferCtr = 0;
	public int total_leaf_keys = 0;
	public int total_internal_keys = 0;
	
	public BPlusTree(int keyLength,String outputDir){
		rightPath = new Stack<BTreeNode>();
		this.outputDir = outputDir;
		this.keyLength = keyLength;
		this.leafLevelPageBuffer = new byte[OUTPUT_BUFFER_SIZE];
		this.first2LvlInternalPageBuffer = new byte[OUTPUT_BUFFER_SIZE];
		this.otherInternalPageBuffer = new byte[OUTPUT_BUFFER_SIZE];
		
	}
	
	/**
	 * @param filename: the name of file which contains the set of keys
	 * 					File format: key1|pointer1|key2|pointer2|...|
	 * 					Each key has keyLength bytes, each pointer is 4 bytes integer
	 * @param keyLength: the length (in bytes) of a key
	 * return: the total number of keys
	 */
	public int bulkLoad(String filename,int keyLength){
		

		try{
			FileInputStream fs = new FileInputStream(filename);
			int leaf_size_in_bytes = BPlusTree.size - 
									BPlusTree.size % (keyLength+BPlusTree.RECID_LENGTH);
			byte[] bs = new byte[leaf_size_in_bytes];
			int len = 0;
			// Read enough bytes to make a leaf page
			while((len=fs.read(bs))!= -1){
				BTreeNode childNode = new BTreeLeafNode(bs,keyLength,IDCtr++,len);
				String highKey = childNode.getHighKey();

				
				// Flush out the leave level pages to the buffer;
				writeOutLeafPage((BTreeLeafNode)childNode);
				//Insert the leaf node to its parent
				Stack<BTreeNode> tmpStack = new Stack<BTreeNode>();
				while(true){
					if(rightPath.size() > 0){
						BTreeInternalNode n = (BTreeInternalNode)rightPath.peek();
						if(!n.isFull()){
							String childHighKey = childNode.getHighKey();
							n.insert(childHighKey,childNode.getID());
							//Push every thing in tmpStack back to make a full right path
							while(tmpStack.size() > 0){
								rightPath.push(tmpStack.pop());
							}
							// insertion done; go out of the while loop
							break;
						}else{
							n = (BTreeInternalNode)rightPath.pop();
							writeOutInternalPage(n);
							// Make a new internal node at the same level of n
							childNode = new BTreeInternalNode(IDCtr++,childNode.getHighKey(),
															  childNode.getID(),
															  keyLength,n.getLevel());
							tmpStack.push(childNode);
						}
					}else{ // Need to make a root
						root = new BTreeInternalNode(IDCtr++,childNode.getHighKey(),
								  childNode.getID(), keyLength,0);
						rightPath.push(root);
						while(tmpStack.size() > 0){
							rightPath.push(tmpStack.pop());
						}
						// insertion done
						break;
					}
				}
				
			}
			fs.close();
			// Flush out the internal page in the right path 
			while(rightPath.size() > 0){
				writeOutInternalPage((BTreeInternalNode)rightPath.pop());
			}
			// Flush out the buffer contents into files
			this.flushLeafBuffer();
			this.flushOutInternalPageBuffer();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("There are "+ this.total_internal_keys + " internal keys");
		System.out.println("There are "+ this.total_leaf_keys + " leaf keys");
		return this.total_leaf_keys;
	}
	public void flushOutInternalPageBuffer(){
		FileOutputStream fout = null;
		
		try{
			fout = new FileOutputStream(
					outputDir+"k"+this.keyLength+"first2Lvl.txt",true);
			BufferedOutputStream bufOut = new BufferedOutputStream
					(fout,OUTPUT_BUFFER_SIZE);
			bufOut.write(this.first2LvlInternalPageBuffer,0,
					this.first2LvlInternalPageBufferCtr);
			bufOut.close();
			fout.close();
			this.first2LvlInternalPageBufferCtr = 0;
			
			fout = new FileOutputStream(
					outputDir+"k"+this.keyLength+"otherLvls.txt",true);
			bufOut = new BufferedOutputStream
					(fout,OUTPUT_BUFFER_SIZE);
			bufOut.write(this.otherInternalPageBuffer,0,
					this.otherInternalPageBufferCtr);
			bufOut.close();
			fout.close();
			this.otherInternalPageBufferCtr = 0;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void writeOutInternalPage(BTreeInternalNode page){
		
		FileOutputStream fout = null;
		//The first two levels of tree will be written into 
		//a seperate file which could be loaded into memory
		byte[] bs = page.getBytes();
		try{
			if(page.getLevel() < 2){
				if(bs.length + this.first2LvlInternalPageBufferCtr <= 
					this.first2LvlInternalPageBuffer.length){
					System.arraycopy(bs, 0, this.first2LvlInternalPageBuffer, 
							this.first2LvlInternalPageBufferCtr, bs.length);
					this.first2LvlInternalPageBufferCtr += bs.length;
				}else{
					fout = new FileOutputStream(
						outputDir+"k"+this.keyLength+"first2Lvl.txt",true);
					BufferedOutputStream bufOut = new BufferedOutputStream
					(fout,OUTPUT_BUFFER_SIZE);
					bufOut.write(this.first2LvlInternalPageBuffer,0,
								 this.first2LvlInternalPageBufferCtr);
					bufOut.close();
					fout.close();
					//Now put into the buffer
					System.arraycopy(bs, 0, this.first2LvlInternalPageBuffer, 
							0, bs.length);
					this.first2LvlInternalPageBufferCtr = bs.length;
				}
			}else{
				if(bs.length + this.otherInternalPageBufferCtr <=
					this.otherInternalPageBuffer.length){
					System.arraycopy(bs, 0, this.otherInternalPageBuffer, 
							this.otherInternalPageBufferCtr, bs.length);
					this.otherInternalPageBufferCtr += bs.length;
				}else{
					fout = new FileOutputStream(
						outputDir+"k"+this.keyLength+"otherLvls.txt",true);
					BufferedOutputStream bufOut = new BufferedOutputStream
						(fout,OUTPUT_BUFFER_SIZE);
					bufOut.write(this.otherInternalPageBuffer,0,
							this.otherInternalPageBufferCtr);
					bufOut.close();
					fout.close();
					//Now put into the buffer
					System.arraycopy(bs, 0, this.otherInternalPageBuffer, 
								0, bs.length);
					this.otherInternalPageBufferCtr = bs.length;
				}
			} 	
			
		}catch(IOException e){
			e.printStackTrace();
		}
		this.total_internal_keys += page.no_keys;

	}
	
	public void flushLeafBuffer(){
		try{
			FileOutputStream fout = new FileOutputStream(
					outputDir+"k"+this.keyLength+"leaf.txt",true);
			BufferedOutputStream bufOut = new BufferedOutputStream
			(fout,OUTPUT_BUFFER_SIZE);
			bufOut.write(this.leafLevelPageBuffer,0,
					this.leafLevelPageBufferCtr);
			bufOut.close();
			fout.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void writeOutLeafPage(BTreeLeafNode page){
		try{
			byte[] bs = page.getBytes();
			if(bs.length + this.leafLevelPageBufferCtr <=
				this.leafLevelPageBuffer.length){
				System.arraycopy(bs, 0, this.leafLevelPageBuffer, 
						this.leafLevelPageBufferCtr, bs.length);
				this.leafLevelPageBufferCtr += bs.length;
			}else{
				FileOutputStream fout = new FileOutputStream(
						outputDir+"k"+this.keyLength+"leaf.txt",true);

				BufferedOutputStream bufOut = new BufferedOutputStream
				(fout,OUTPUT_BUFFER_SIZE);
				bufOut.write(this.leafLevelPageBuffer,0,
							this.leafLevelPageBufferCtr);
				bufOut.close();
				fout.close();
				// Now put into the buffer
				System.arraycopy(bs, 0, this.leafLevelPageBuffer, 0, bs.length);
				this.leafLevelPageBufferCtr = bs.length;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		this.total_leaf_keys += page.no_keys;
	}
	
	public void search(String key){
		
	}
	
	/**
	 * @param filename of the file which contains the first two internal levels of B-Tree;
	 */
	public void loadBPlusTree(String filename){
		//Step 1: Read in all the pages 
		Hashtable<Integer,BTreeNode> id2NodeHash = new Hashtable<Integer,BTreeNode>();
		
		try{
			FileInputStream fs = new FileInputStream(filename);
			byte[] bs = new byte[size];
			int len = 0;
			// Read enough bytes to make a leaf page
			while((len=fs.read(bs))!= -1){
				//BTreeNode node = makeBTreeNode(bs);
				//id2NodeHash.put(new Integer(node.getID()), node);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		//Step 2: Build an in-memory tree of the first two level of the B-Tree
		Iterator<BTreeNode> nodeLstItr = id2NodeHash.values().iterator();
		while(nodeLstItr.hasNext()){
			BTreeNode n = nodeLstItr.next();
			if(n instanceof BTreeInternalNode){
				BTreeInternalNode tmp = (BTreeInternalNode)n;
				//tmp.g
			}
		}
	}
}

class BTreeNode{
	int nodeID;
	BTreeNode parent;
	String[] keys;
	int no_keys = 0;
	public String getHighKey(){
		return keys[no_keys - 1];
	}
	public int getID(){
		return nodeID;
	}
}

class BTreeInternalNode extends BTreeNode{
	/*
	 * Internal node format: Each interal page has a fixed size: 
	 * |PageID(4 byte)|page_length(4byte)|p_1(4byte)|key_1(keyLength byte)|p_2(4 byte)|...|key_n(keyLength byte)|
	 */
	int[] pointers;
	int noSlots;
	int level;
	byte[] bytes;
	int keyLength;
	public BTreeInternalNode(int id, String key, int childPageID, int keyLength,int level){
		
		noSlots = BPlusTree.size / (keyLength + BPlusTree.RECID_LENGTH);
		bytes = new byte[BPlusTree.size + BPlusTree.PAGEID_LENGTH];
		keys = new String[noSlots];
		pointers = new int[noSlots];
		keys[no_keys] = key;
		pointers[no_keys++] = childPageID;
		this.level = level;
		this.nodeID = id;
		this.keyLength = keyLength;
		//Now fill in the bytes content
		//1. Page ID
		byte[] pageID_bytes = {(byte)(id >> 24), (byte)(id >> 16) , 
				(byte)(id >> 8), (byte)id};
		System.arraycopy(pageID_bytes, 0, bytes, 0, BPlusTree.PAGEID_LENGTH);
		//2. p_1 first pageID of cihld
		byte[] childPageID_bytes = {(byte)(childPageID >> 24), (byte)(childPageID >> 16) , 
				(byte)(childPageID >> 8), (byte)childPageID};
		System.arraycopy(childPageID_bytes, 0, bytes, BPlusTree.PAGEID_LENGTH, BPlusTree.RECID_LENGTH);
		//3. Division key
		System.arraycopy(key.getBytes(), 0, bytes, BPlusTree.PAGEID_LENGTH+BPlusTree.RECID_LENGTH, key.getBytes().length);
		
	}
	public int getLevel(){
		return level;
	}
	public boolean isFull(){
		return no_keys >= noSlots;
	}
	public void insert(String childHighKey,int childPageID){
		// Fill in the byte contents
		//1. p_1 first pageID of cihld
		byte[] childPageID_bytes = {(byte)(childPageID >> 24), (byte)(childPageID >> 16) , 
				(byte)(childPageID >> 8), (byte)childPageID};
		System.arraycopy(childPageID_bytes, 0, bytes, BPlusTree.PAGEID_LENGTH+no_keys*(keyLength + BPlusTree.RECID_LENGTH), 4);
		//2. Division key
		System.arraycopy(childHighKey.getBytes(), 0, bytes, 
				BPlusTree.PAGEID_LENGTH+no_keys*(keyLength + BPlusTree.RECID_LENGTH)+4, childHighKey.getBytes().length);
		
		keys[no_keys] = childHighKey;
		pointers[no_keys++] = childPageID;
	}
	
	public byte[] getBytes() { return this.bytes; } 
	public String toString(){
		String s = "|Internal:" + this.getID() + "|";
		for(int i = 0; i < no_keys; i++){
			s += (Utility.transformText(this.keys[i]) + "|" + this.pointers[i] + "|"); 
		}
		return s;
	}
}

class BTreeLeafNode	extends BTreeNode{
	/*
	 * Leaf node format: each page has a fixed size; size
	 * |page_id(4byte)|key1|record_id1|key2|record_id2|...|key_n|record_idn|
	 */
	int[] recIDs;
	byte[] bytz;
	
	public BTreeLeafNode(byte[] bs,int keyLength,int id,int actual_length){ 
		this.bytz = new byte[bs.length + BPlusTree.RECID_LENGTH];
		if(actual_length % (BPlusTree.RECID_LENGTH+keyLength) != 0)
			System.err.println
			("Init Tree Leaf Page err: byte length is not multiple of recID length plus keylength:"+actual_length);
		this.nodeID = id;
		this.no_keys = actual_length/(keyLength+BPlusTree.RECID_LENGTH);
		keys = new String[actual_length/(keyLength+BPlusTree.RECID_LENGTH)];
		recIDs = new int[actual_length/(keyLength+BPlusTree.RECID_LENGTH)];
		int ctr = 0;
		for(int i = 0; i < actual_length; i+= (keyLength+BPlusTree.RECID_LENGTH)){
			keys[ctr] = new String(bs,i, keyLength);
			recIDs[ctr] = bs[i+keyLength]<<24 | (bs[i+keyLength+1]&0xff)<<16 | 
			(bs[i+keyLength+2]&0xff)<<8 | (bs[i+keyLength+3]&0xff);
			ctr++;
		}
		
		// First copy the id byte and then the data portion
		byte[] ID_bytes = {(byte)(id >> 24), (byte)(id >> 16) , 
				(byte)(id >> 8), (byte)id};
		System.arraycopy(ID_bytes, 0, bytz, 0, ID_bytes.length);
		System.arraycopy(bs, 0, bytz, BPlusTree.RECID_LENGTH, bs.length);
	}
	
	public byte[] getBytes(){
		return this.bytz;
	}
	public String toString(){
		String s = "|Leaf:" + this.getID() +"|";
		for(int i = 0; i < no_keys; i++){
			s += (Utility.transformText(this.keys[i]) +"|" + this.recIDs[i]+"|"); 
		}
		return s;
	}
	
}
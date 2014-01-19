package indexer.trex;

import Utility.*;

import java.io.*;
import java.util.*;

/**
 * @author tchen
 * Use spimi algorithm to build the following indices 
 * 1. KGram boolean index 
 * 2. Minspan indices
 * 3. Transformation indices
 */
public class SPIMIRegexIndexer {
	private String dataSrc; // the directory root of all files to index 
	private String outputDir; // the directory where the indices file will be stored
	private final int KGRAM_INDEX_MEM_LIMIT = 2000000000; //the size of the memory in unit of 
												   //hash entry for kgram indices
	private final int AVG_FILE_SIZE = 4000;
	private HashMap<String,byte[]>[] dictionaryLst; //map an kgram to its postings list
	private Hashtable<String,String> nonSelGramHash; 

	private final int MAX_SPAN_INDEX_MEM_LIMIT = 200000000;
	private final int MIN_SPAN_INDEX_MEM_LIMIT = 200000000;
	
	private final int OFFSET_BYTE_SIZE = 4;
	private final int SIZE_BYTE_SIZE = 4;
	private int[] INIT_DICTIONARY_SIZE;
	private static final int SPAN_INDEX_ENTRY_SIZE = 8;  // a span posting entry has a pair of integers: span,docid
	private final String BIG_FILE_NAME = "bigFile";
	private final int BUF_SIZE = 10*1024*1024;
	
	/** Index file naming convention **/
	/** a file in the index directory consists of 3 parts seperated by .
	 *  Part A: file function ["spimiSub","tmpPost","post",
	 *  					   "tmpGramOffset","gram","gramOffset"]
	 *  Part B: sequence length [1,2,...,K]
	 *  Part C: IndexID: [0: ngram, 1: minspan, 2: maxspan, 3: proj]
	 * **/
	
	
	int[][] minSpanArray;
	int no_symbols;
	int[] posArray;
	int[] firstArray;
	int[] lastArray;
	
	//private HashMap<String,Integer> ngram2PrevDocId;  //map an kgram to its latest docid 
	private int current_in_memory_invertedLst_size[] = null;
	private int num_runs[] = null;
	/* The performance statistics */
	long scan_time = 0;
	long filewrite_time = 0;
	long read_time = 0;
	
	/* Configuration parameters */
	private int MERGE_FACTOR = 50;   //The number of sub-inverted lists could be merged
	private int HEADER_SIZE = 8;     //The first 8 bytes of a postings list:4 for limit and 4 for count 
	private int NUM_COUNT_BYTES = 4; //No. of bytes used to store count   
	private int NUM_FILENAME_BYTES = 4; //No. of bytes used to store file id;
	private int INIT_POSTINGS_SIZE = 500;  //The initial size of postings list in the unit of docId-freq pairs
										   //IMPORTANT: INIT_POSTINGS_SIZE > postingEntrySize ;	
	private int  INIT_SPAN_POSTINGS_SIZE = 1000; // initial span posting size
	private int MAX_NGRAM_LENGTH_IN_BYTE = 40; // The maximum number of bytes used to store a ngram in a inverted index
	private byte SEPARATOR_BYTE = '\000'; //The separator byte used to separate ngrams from the rest in an posting list
	private int NUM_FILES_PER_DIR = 1000;  //The number of indices file in a directory
	private int STRING_UNIT_LENGTH = 15;   //The avg number of bytes used to store a string char in Java
	private double MEM_DATA_REP_FACTOR = 1.1; // the blow up factor that memory used to hold a data structure 
	private int OUTPUT_BUFFER_SIZE = 1024*1024;   //The buffer size used to output ngram files and index flies;   
	private int MERGE_INPUT_BUFFER_SIZE = 512*1024;  // The buffer size used to read input inverted list files
	private int noFileScanned;
	private int MIN_STRING_BUF_SIZE = 5096*1024;
	private int MERGE_BUFFER_SIZE = 2*1024*1024;
	private final int AVG_PROJECTIVE_SEQ_PER_FILE = 1000;
	private final int INIT_HASH_SIZE = 10000;
	 
	private static int DOCID_LENGTH = 4;
	private final boolean readFromBigFile = false; //directive to indicate if read from big file or small files
	
	byte[][] mergeBufferArray = null;  // An array of postings list buffers one for each ngram length
	// the format is |sizeofPosting1|doc_11|.....|sizeOfPostingn|doc_n1|....
	// the docID and count is variable byte encoded
	byte[][] ngramsBufferArray = null; // An array of ngrams buffers one for each ngram length
	// the format is |key1|offset1|...|keyn|offsetn|
	Vector<byte[]>[] kgramPostingsLst = null;
	int[] mergeBufferUsedSpaces = null; // An array keeping track the number of bytes used by each mergeBuffer
	int[] ngramBufferUsedSpaces = null;	// An array keeping track of the no. of bytes used by each ngramBuffer
	int[] postingsLstOffsets = null; // An array keeping track of the no. of bytes used for each posting list files
	String[] prevPfxLst = null;
	String[] prefixLst = null;
	boolean[] isUnselective = null;

	
	/* output buffers for kgrams before beta pruning*/
	BufferedOutputStream[] kgramBufOut;
	FileOutputStream[] kgramFile;
	/* output buffers for kgrams before beta pruning*/
	BufferedOutputStream[] postingBufOut;
	FileOutputStream[] postingFile;
	/* the list of a-b optimal max length kgrams used for the kgram summary file output
	 *  the int value is offset of the k-gram posting before beta pruning and is not used for summary output*/
	String dummy = "";
	Hashtable<String,String> optimalKGrams = new Hashtable<String,String>();
	
	public static void main(String[] args)throws IOException{
		long startT = System.currentTimeMillis();
		
		if(args.length == 2 && args[0].compareTo("-i") == 0){
			IndexerConfig.initControlParameters(args[1]);
			IndexerConfig.printParameters();
			SPIMIRegexIndexer regexIndexer = new SPIMIRegexIndexer(IndexerConfig.fileDir2Index,
					IndexerConfig.indexDir);
			
			regexIndexer.constructIndex();
			//regexIndexer.showFinalSpanIndices(regexIndexer.MIN_SPAN_INDEX_ID);
			//regexIndexer.showFinalSpanIndices(regexIndexer.MAX_SPAN_INDEX_ID);
			long endT = System.currentTimeMillis();
			System.out.println("The index construction costs "+(endT-startT)+" millisec in total");
		}else if(args.length >= 6 && args[0].compareTo("-i") == 0 && args[2].compareTo("-n") == 0 && args[4].compareTo("-indexDir") == 0){
			IndexerConfig.initControlParameters(args[1]);
			IndexerConfig.doc_limit = new Integer(args[3]).intValue();
			IndexerConfig.indexDir = args[5];
			for(int i = 6; i < args.length; i++){
				if(args[i].compareTo("-noProj") == 0)
					IndexerConfig.PROJECTIVE_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noSpan") == 0)
					IndexerConfig.SPAN_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noKGram") == 0)
					IndexerConfig.KGRAM_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noTrans") == 0)
					IndexerConfig.TRANSFORM_INDEX_INCLUDED = false;
				if(args[i].compareTo("-a") == 0)
					IndexerConfig.SEL_THRESHOLD = new Double(args[i+1]).doubleValue();
				if(args[i].compareTo("-b") == 0)
					IndexerConfig.SEL_BETA = new Double(args[i+1]).doubleValue();
			}
			IndexerConfig.printParameters();
			SPIMIRegexIndexer regexIndexer = new SPIMIRegexIndexer(IndexerConfig.fileDir2Index,
					IndexerConfig.indexDir);
			
			regexIndexer.constructIndex();
			//regexIndexer.showFinalSpanIndices(regexIndexer.MIN_SPAN_INDEX_ID);
			//regexIndexer.showFinalSpanIndices(regexIndexer.MAX_SPAN_INDEX_ID);
			long endT = System.currentTimeMillis();
			System.out.println("The index construction costs "+(endT-startT)+" millisec in total");
		}else if(args.length >= 4 && args[0].compareTo("-i") == 0){
			IndexerConfig.initControlParameters(args[1]);
			for(int i = 2; i < args.length; i++){
				if(args[i].compareTo("-noProj") == 0)
					IndexerConfig.PROJECTIVE_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noSpan") == 0)
					IndexerConfig.SPAN_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noKGram") == 0)
					IndexerConfig.KGRAM_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noTrans") == 0)
					IndexerConfig.TRANSFORM_INDEX_INCLUDED = false;
				if(args[i].compareTo("-noZero") == 0)
					IndexerConfig.ZERO_SEL_ON = false;
			}
			IndexerConfig.printParameters();
			SPIMIRegexIndexer regexIndexer = new SPIMIRegexIndexer(IndexerConfig.fileDir2Index,
					IndexerConfig.indexDir);
			
			regexIndexer.constructIndex();
			//regexIndexer.showFinalSpanIndices(regexIndexer.MIN_SPAN_INDEX_ID);
			//regexIndexer.showFinalSpanIndices(regexIndexer.MAX_SPAN_INDEX_ID);
			long endT = System.currentTimeMillis();
			System.out.println("The index construction costs "+(endT-startT)+" millisec in total");
		}else if(args.length == 2 && args[0].compareTo("-s") == 0){
			IndexerConfig.initControlParameters(args[1]);
			IndexerConfig.printParameters();
			SPIMIRegexIndexer ngramIndexer = new SPIMIRegexIndexer(IndexerConfig.fileDir2Index,
					IndexerConfig.indexDir);
		}else{
			System.out.println("Usage -i cfg_file_name max_length limit");
			System.exit(0);
		}
		
		
	}
	
	/**
	 * @param dataSrc the directory root of all files to index
	 * @param outputDir the directory where the indices file will be stored
	 */
	public SPIMIRegexIndexer(String dataSrc,String outputDir){
		System.out.println("Clear Indexdir " + outputDir);
		Utility.ClearOutputDir(outputDir);
		System.out.println("Indexdir " + outputDir + "cleared");

		this.dataSrc = dataSrc;
		this.outputDir = outputDir;
		
		this.dictionaryLst = new HashMap[IndexerConfig.NO_INDICES];
		this.INIT_DICTIONARY_SIZE = new int[IndexerConfig.NO_INDICES];
		
		nonSelGramHash = new Hashtable<String,String>(this.INIT_HASH_SIZE);
		dictionaryLst[IndexerConfig.KGRAM_INDEX_ID] = 
			new HashMap<String,byte[]>(); //map an kgram to its postings list
		dictionaryLst[IndexerConfig.PROJECTIVE_INDEX_ID]	= 
			new HashMap<String,byte[]>(); 
		dictionaryLst[IndexerConfig.MIN_SPAN_INDEX_ID] = 
			new HashMap<String,byte[]>(); 
		dictionaryLst[IndexerConfig.TRANSFORMED_INDEX_ID] = 
			new HashMap<String,byte[]>();

		
		current_in_memory_invertedLst_size = new int[IndexerConfig.NO_INDICES];
		for(int i = 0; i < this.current_in_memory_invertedLst_size.length; i++){
			this.current_in_memory_invertedLst_size[i] = 0;
		}
		this.num_runs = new int[IndexerConfig.NO_INDICES];
		for(int i = 0; i < this.num_runs.length; i++){
			this.num_runs[i] = 0;
		}
		
		
		this.kgramFile = new FileOutputStream[IndexerConfig.NGRAM_MAX_ROUND];
		this.kgramBufOut = new BufferedOutputStream[IndexerConfig.NGRAM_MAX_ROUND];
		this.postingFile = new FileOutputStream[IndexerConfig.NGRAM_MAX_ROUND];
		this.postingBufOut = new BufferedOutputStream[IndexerConfig.NGRAM_MAX_ROUND];
		
		// Init the kgram output buffer stream and posting output buffer stream
		for(int i = 0; i < IndexerConfig.NGRAM_MAX_ROUND; i++){
			try{
				kgramBufOut[i]= new BufferedOutputStream(new FileOutputStream(
						this.outputDir+IndexerConfig.tmpGramOffset+IndexerConfig.delimiter+
						(i+1)+IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID,true),
						OUTPUT_BUFFER_SIZE);
				
				postingBufOut[i] = new BufferedOutputStream(new FileOutputStream(
						this.outputDir+IndexerConfig.tmpPostingFile+IndexerConfig.delimiter+(i+1)+
						IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID,true),
						OUTPUT_BUFFER_SIZE);
				
			}catch(Exception e){
				e.printStackTrace();
			}	
		}
		
		
		//Init the various array of minmax span indices construction
		posArray = new int[no_symbols];
		firstArray= new int[no_symbols];
		lastArray = new int[no_symbols];
		posArray = new int[no_symbols];
		minSpanArray = new int[no_symbols][no_symbols];
	} 	
	
	
	public void constructIndex(){
		
		// Step 1: Scan corpus and build inverted lists of subset of corpus
		long scan_start = System.currentTimeMillis();
		//initMinMaxDictionary();
		if(readFromBigFile)
			scanBigFileNBuildSubIndices();
		else //read from file directory
			this.scanCorpusNBuildSubIndices();
		
		long scan_end = System.currentTimeMillis();
		System.out.println("Finish scanning corpus...");
		System.out.println("Scan took "+(scan_end-scan_start) + " millisec");
		// dump the last in-memory dictionary of every
		if(IndexerConfig.KGRAM_INDEX_INCLUDED)
			dumpInMemoryInvertedFile(IndexerConfig.KGRAM_INDEX_ID);
		
		long last_dump_end = System.currentTimeMillis();
		System.out.println("Last dump took "+(last_dump_end-scan_end) + " millisec");
		
		
		// Step 2: Merge inverted lists of corpus subsets and produce all
		long merge_start = System.currentTimeMillis();
		if(IndexerConfig.KGRAM_INDEX_INCLUDED){
			// Output the selective kgrams
			this.prunedMergeAndOutputIndices(0, 
										this.num_runs[IndexerConfig.KGRAM_INDEX_ID] -1,IndexerConfig.NGRAM_MAX_ROUND,
										IndexerConfig.KGRAM_INDEX_ID,IndexerConfig.SEL_THRESHOLD);
			for(int i = 0; i < IndexerConfig.NGRAM_MAX_ROUND; i++){
				try{
					this.kgramBufOut[i].close();
					this.postingBufOut[i].close();
				}catch(IOException e){e.printStackTrace();}
			}
			// Further pruned out those  marginally selective grams
			//if(IndexerConfig.SEL_BETA > 0)
			this.pruneMarginalSelGrams(IndexerConfig.KGRAM_INDEX_ID);
		}	

		

		
		
		long merge_end = System.currentTimeMillis();
		System.out.println("Merge took "+(merge_end-merge_start) + " millisec");
		
		// Step 3: Bulkload the BTree
		
		int total_keys = 0;
		/*
		for(int i = 0; i < IndexerConfig.NGRAM_MAX_ROUND; i++){
			BPlusTree btree = new BPlusTree(i+1,outputDir);
			total_keys += btree.bulkLoad(outputDir+"key"+(i+1)+".0", i+1);
		}
		System.out.println("Total leaf keys "+total_keys); */
	

		//Step 4: Dump the unselective kgram file
		//Step 5: Clear the index directory
		
		if(IndexerConfig.UPDATABLE)
			mergeAndOutputKGramSummary(0,this.num_runs[IndexerConfig.KGRAM_INDEX_ID]-1, 
										IndexerConfig.NGRAM_MAX_ROUND,IndexerConfig.KGRAM_INDEX_ID);
		//clearIntermediateFiles();
		if(IndexerConfig.DEBUG){
			this.showKGramIndices(IndexerConfig.NGRAM_MAX_ROUND,
								IndexerConfig.KGRAM_INDEX_ID);
		}
		
		
	}
	
	
	private void mergeAndOutputKGramSummary(int first, int last, int seqLength, int indexID){
		PriorityQueue<NGramRunPair> pq = new PriorityQueue<NGramRunPair>();
		//open the run files for reading
		BufferedInputStream[] runReaders = new BufferedInputStream[last-first+1];
		//init an output file stream
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(
				outputDir+IndexerConfig.summaryFileName+IndexerConfig.delimiter+seqLength+
				IndexerConfig.delimiter+indexID,true);
		}catch(IOException e){e.printStackTrace();}
		BufferedOutputStream bufOut = new BufferedOutputStream
				(fout,OUTPUT_BUFFER_SIZE);
		
		NGramRunPair[] runGrams = new NGramRunPair[last-first+1];  //storing the current ngram of each run
		String curKgram = null;  //the least gram in alphabetic order
		byte[] curKgramPostings = null;
		
		for(int i = first; i <= last; i++){
			try{
				runReaders[i] = new BufferedInputStream(
					new FileInputStream(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+i+IndexerConfig.delimiter+indexID), 
					MERGE_INPUT_BUFFER_SIZE);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// Read the first posting from each run file
		byte[] ngram = new byte[seqLength];
		for(int i = first; i<= last; i++){
			//Reading the head of run files;
			int ctr = 0;
			int length = 0;
			// First read the next ngram from the run file
			try{
				if((runReaders[i].read(ngram)) != -1){
					//Read the seperator byte
					runReaders[i].read();
					ctr++;
					//Read in the length integer
					byte[] lengthByte = new byte[4];
					runReaders[i].read(lengthByte);
					length = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
						(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			if(ctr > 0){
				String gram = new String(ngram,0,ngram.length);
				NGramRunPair p= new NGramRunPair(gram,i,length);
				runGrams[i] = p;
				pq.add(p);
			}else
				runGrams[i] = null;
		}
	
		StringBuffer sb = new StringBuffer(MIN_STRING_BUF_SIZE);
		while(true){
			if(pq.size() == 0) // all run are exhausted
				break;
			else{
				NGramRunPair min = pq.poll();
				String minGram = min.ngram;
				int run = min.runNum;
				int length = min.length;
				if(IndexerConfig.DEBUG)  System.out.println("PQ min "+min);
				// Reading in the posting list of minGram
				byte[] pLst = new byte[length];
				try{
					runReaders[run].read(pLst);
				}catch(IOException e){ e.printStackTrace();}
				
				if(!optimalKGrams.containsKey(minGram)){ //otherwise the kgram's posting list is in index
					if(curKgram != null && curKgram.compareTo(minGram) == 0){
						// a kgram exists in more than two runs
						// no union required; merge two posting list
						byte[] tmpPLst = new byte[curKgramPostings.length+pLst.length];
						System.arraycopy(curKgramPostings, 0, tmpPLst, 0, curKgramPostings.length);
						System.arraycopy(pLst, 0, tmpPLst, curKgramPostings.length, pLst.length);
						curKgramPostings = tmpPLst;
					}else{
						//a new kgram is found;
						// write out the previous kgram
						if(curKgram != null){
							try{
								bufOut.write(curKgram.getBytes());
								int len = curKgramPostings.length;
								// compress the posting list and write it out
								byte[] compressedPLst = IndexCompressor.
								compressByteArray(curKgramPostings,0,len);

								int compressedLen = compressedPLst.length;
								byte[] lengthBytes = {(byte)(compressedLen>>24), (byte)(compressedLen>>16),
										(byte)(compressedLen>>8), (byte)compressedLen};
								bufOut.write(lengthBytes);
								bufOut.write(compressedPLst);
							}catch(IOException e){e.printStackTrace();}
						}
						// update the current kgram
						curKgram = minGram;
						curKgramPostings = pLst;
					}
				}
				// Reading the next posting of the leading run file
				int ctr = 0;
				int newlength = 0;
				// First read the next ngram from the run file
				try{
					if(runReaders[run].read(ngram) != -1){
						//Read in the seperator byte
						runReaders[run].read();
						ctr++;
						//Read in the length integer
						byte[] lengthByte = new byte[4];
						runReaders[run].read(lengthByte);
						newlength = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
									(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);

					}
				}catch(IOException e){
					e.printStackTrace();
				}
				if(ctr > 0){
					String gram = new String(ngram,0,ngram.length);
					NGramRunPair p= new NGramRunPair(gram,run,newlength);
					runGrams[run] = p;
					pq.add(p);
				}else
					runGrams[run] = null;
			}
		}
		//Flush out the remaining contents in posting list vector AND the buffers
		
		try{
			//Flush out remaining contents in vector
			if(curKgram != null){
				bufOut.write(curKgram.getBytes());
				int len = curKgramPostings.length;
				// compress the posting list and write it out
				byte[] compressedPLst = IndexCompressor.
										compressByteArray(curKgramPostings,0,len);
				
				int compressedLen = compressedPLst.length;
				byte[] lengthBytes = {(byte)(compressedLen>>24), (byte)(compressedLen>>16),
									  (byte)(compressedLen>>8), (byte)compressedLen};
				bufOut.write(lengthBytes);
				bufOut.write(compressedPLst);
				
			}else{
				System.err.println("ERR: the last kgram in the summary outstream is NULL");
			}
			bufOut.close();
			fout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void clearIntermediateFiles(){
		// Rename length 1 tmp gram/posting list files to final ones 
		if(IndexerConfig.KGRAM_INDEX_INCLUDED){
			if(IndexerConfig.SEL_BETA > 0){
				try{
					File f = new File(outputDir+IndexerConfig.tmpPostingFile+
							IndexerConfig.delimiter+
							"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
					f.renameTo(new File(outputDir+IndexerConfig.postingFile+
							IndexerConfig.delimiter+
							"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID));
					f = new File(outputDir+IndexerConfig.tmpGramOffset+
							IndexerConfig.delimiter+
							"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
					// Remove the size bytes in tmpGramOffset file
					removeSizeBytesFromFile(outputDir+IndexerConfig.tmpGramOffset+
							IndexerConfig.delimiter+
							"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID,
							1,IndexerConfig.KGRAM_INDEX_ID);
					f.delete();
				}catch(Exception e){
					e.printStackTrace();
				}

				for(int i = 2; i <= IndexerConfig.NGRAM_MAX_ROUND; i++){
					try{
						File f = new File(outputDir+IndexerConfig.tmpPostingFile+
								IndexerConfig.delimiter+i+IndexerConfig.delimiter+
								IndexerConfig.KGRAM_INDEX_ID);
						f.delete();
						f = new File(outputDir+IndexerConfig.tmpGramOffset+
								IndexerConfig.delimiter+
								i + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
						f.delete();

					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}else{  //Beta = 0
				ByteOutputBuffer[] ngramBuffers = new
				ByteOutputBuffer[IndexerConfig.NGRAM_MAX_ROUND];
				ByteOutputBuffer[] ngramOffsetBuffers = new
				ByteOutputBuffer[IndexerConfig.NGRAM_MAX_ROUND];
				for(int i = 1; i <= IndexerConfig.NGRAM_MAX_ROUND; i++){
					try{
						File f = new File(outputDir+IndexerConfig.tmpPostingFile+
								IndexerConfig.delimiter+i+IndexerConfig.delimiter+
								IndexerConfig.KGRAM_INDEX_ID);
						f.renameTo(new File(outputDir+IndexerConfig.postingFile+
								IndexerConfig.delimiter+i+IndexerConfig.delimiter+
								IndexerConfig.KGRAM_INDEX_ID));
						f = new File(outputDir+IndexerConfig.tmpGramOffset+
								IndexerConfig.delimiter+
								i + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
						//output the grams for beta 0 index, 
						int init_size = 100;
						init_size *= 10;
						ngramBuffers[i-1] = new ByteOutputBuffer(this.outputDir+IndexerConfig.gram
								+ IndexerConfig.delimiter + i 
								+ IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID,
								this.MERGE_BUFFER_SIZE);
						ngramOffsetBuffers[i-1] = new ByteOutputBuffer(this.outputDir+IndexerConfig.gramOffset
								+ IndexerConfig.delimiter + i 
								+ IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID,
								this.MERGE_BUFFER_SIZE);

						FileInputStream fs = new FileInputStream(
								this.outputDir+IndexerConfig.tmpGramOffset
								+ IndexerConfig.delimiter + i  
								+ IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
						byte[] bs = new byte[i + OFFSET_BYTE_SIZE+SIZE_BYTE_SIZE];
						int len = 0;
						while((len=fs.read(bs))!= -1){
							String gram = new String(bs,0,i);
							int offSet = bs[i]<<24 | (bs[i+1]&0xff)<<16 | 
							(bs[i+2]&0xff)<<8 | (bs[i+3]&0xff);
							ngramBuffers[i-1].write(gram);
							ngramOffsetBuffers[i-1].write(gram);
							ngramOffsetBuffers[i-1].write(offSet);
						}
						fs.close();
						ngramBuffers[i-1].close();
						ngramOffsetBuffers[i-1].close();
						f.delete();

					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}


			for(int j = 0; j < this.num_runs[IndexerConfig.KGRAM_INDEX_ID]; j++){
				try{
					File f = new File(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+
							j+IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
					f.delete();

				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(!IndexerConfig.ZERO_SEL_ON){
				for(int i = 1; i <= IndexerConfig.NGRAM_MAX_ROUND; i++){
					try{
						File f = new File(outputDir+IndexerConfig.gram +
								IndexerConfig.delimiter+i+IndexerConfig.delimiter+
								IndexerConfig.KGRAM_INDEX_ID);
						f.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		if(IndexerConfig.SPAN_INDEX_INCLUDED){
			for(int j = 0; j < this.num_runs[IndexerConfig.MIN_SPAN_INDEX_ID]; j++){
				try{
					File f = new File(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+
							j+IndexerConfig.delimiter+IndexerConfig.MIN_SPAN_INDEX_ID);
					f.delete();

				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			/*for(int j = 0; j < this.num_runs[this.MAX_SPAN_INDEX_ID]; j++){
				try{
					File f = new File(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+
							j+IndexerConfig.delimiter+this.MAX_SPAN_INDEX_ID);
					f.delete();

				}catch(Exception e){
					e.printStackTrace();
				}
			}*/
		}
		
		if(IndexerConfig.PROJECTIVE_INDEX_INCLUDED){
			for(int j = 0; j < this.num_runs[IndexerConfig.PROJECTIVE_INDEX_ID]; j++){
				try{
					File f = new File(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+
							j+IndexerConfig.delimiter+IndexerConfig.PROJECTIVE_INDEX_ID);
					f.delete();

				}catch(Exception e){
					e.printStackTrace();
				}
			}
			for(int i = 0; i < IndexerConfig.SEL_MAX_ROUND; i++){
				File f = new File(outputDir+IndexerConfig.tmpPostingFile+
									IndexerConfig.delimiter+
									(i+1) + IndexerConfig.delimiter+IndexerConfig.PROJECTIVE_INDEX_ID);
				f.renameTo(new File(outputDir+IndexerConfig.postingFile+
					IndexerConfig.delimiter+
					(i+1) + IndexerConfig.delimiter+IndexerConfig.PROJECTIVE_INDEX_ID));
				
				f = new File(outputDir+IndexerConfig.tmpGramOffset+
							IndexerConfig.delimiter+
							(i+1) + IndexerConfig.delimiter+IndexerConfig.PROJECTIVE_INDEX_ID);
				f.renameTo(new File(outputDir+IndexerConfig.gramOffset+
							IndexerConfig.delimiter+
							(i+1) + IndexerConfig.delimiter+IndexerConfig.PROJECTIVE_INDEX_ID));
			}
		}
		
		if(IndexerConfig.TRANSFORM_INDEX_INCLUDED){
			for(int j = 0; j < this.num_runs[IndexerConfig.TRANSFORMED_INDEX_ID]; j++){
				try{
					File f = new File(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+
							j+IndexerConfig.delimiter+IndexerConfig.TRANSFORMED_INDEX_ID);
					f.delete();

				}catch(Exception e){
					e.printStackTrace();
				}
			}
			for(int i = 0; i < IndexerConfig.SEL_MAX_ROUND; i++){
				File f = new File(outputDir+IndexerConfig.tmpPostingFile+
									IndexerConfig.delimiter+
									(i+1) + IndexerConfig.delimiter+IndexerConfig.TRANSFORMED_INDEX_ID);
				f.renameTo(new File(outputDir+IndexerConfig.postingFile+
					IndexerConfig.delimiter+
					(i+1) + IndexerConfig.delimiter+IndexerConfig.TRANSFORMED_INDEX_ID));
				
				f = new File(outputDir+IndexerConfig.tmpGramOffset+
							IndexerConfig.delimiter+
							(i+1) + IndexerConfig.delimiter+IndexerConfig.TRANSFORMED_INDEX_ID);
				f.renameTo(new File(outputDir+IndexerConfig.gramOffset+
							IndexerConfig.delimiter+
							(i+1) + IndexerConfig.delimiter+IndexerConfig.TRANSFORMED_INDEX_ID));
			}
		}
	}
	private void removeSizeBytesFromFile(String filename,int gramLen,int indexID){
		ByteOutputBuffer buf = new ByteOutputBuffer(this.outputDir+IndexerConfig.gramOffset
					+ IndexerConfig.delimiter + gramLen + IndexerConfig.delimiter
					+ indexID,this.MERGE_BUFFER_SIZE);
		try{
			FileInputStream fs = new FileInputStream(
						filename);
			byte[] bs = new byte[gramLen + OFFSET_BYTE_SIZE+SIZE_BYTE_SIZE];
			int len = 0;
			// Read enough bytes to make a leaf page
			while((len=fs.read(bs))!= -1){
				String gram = new String(bs,0,gramLen);
				int offSet = bs[gramLen]<<24 | (bs[gramLen+1]&0xff)<<16 | 
					(bs[gramLen+2]&0xff)<<8 | (bs[gramLen+3]&0xff);
				int size = bs[gramLen+4]<<24 | (bs[gramLen+5]&0xff)<<16 | 
					(bs[gramLen+6]&0xff)<<8 | (bs[gramLen+7]&0xff);
				buf.write(gram);
				buf.write(offSet);
			}
			fs.close();
			buf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void loadKGramRecords(Vector<StringValuePair>[] gramLst, int indexID){
		int init_size = 100;
		if(IndexerConfig.DEBUG)
			System.out.println("==========Load Kgrams=========");
		for(int i = 1; i <= IndexerConfig.NGRAM_MAX_ROUND; i++){
			init_size *= 10;
			gramLst[i-1] = new Vector<StringValuePair>(init_size);
			try{
				FileInputStream fs = new FileInputStream(
						this.outputDir+IndexerConfig.tmpGramOffset
						+ IndexerConfig.delimiter + i + IndexerConfig.delimiter
						+ indexID);
				byte[] bs = new byte[i + 4*IndexerConfig.BYTES_INT];
				int len = 0;
				// Read enough bytes to make a leaf page
				while((len=fs.read(bs))!= -1){
					String gram = new String(bs,0,i);
					int size = bs[i]<<24 | (bs[i+1]&0xff)<<16 | 
							(bs[i+2]&0xff)<<8 | (bs[i+3]&0xff);
					int offset = bs[i+4]<<24 | (bs[i+5]&0xff)<<16 | 
							(bs[i+6]&0xff)<<8 | (bs[i+7]&0xff);
					int count = bs[i+8]<<24 | (bs[i+9]&0xff)<<16 | 
							(bs[i+10]&0xff)<<8 | (bs[i+11]&0xff);
					int lastID = bs[i+12]<<24 | (bs[i+13]&0xff)<<16 | 
							(bs[i+14]&0xff)<<8 | (bs[i+15]&0xff);
					KGramRecord rec = new KGramRecord(size,offset,count,lastID);
					gramLst[i-1].add(new StringValuePair(gram,rec));
					if(IndexerConfig.DEBUG)
						System.out.println(Utility.transformText(gram)+":"
										+rec);
					
				}
				fs.close();
			}catch(Exception e) {e.printStackTrace();}
		}
		System.out.println("----Kram file read done");
	}
	
	private void pruneMarginalSelGrams(int indexID){
		long time_start = System.currentTimeMillis();
		System.out.println("----Start marginal pruning");
		// First read in the keys files
		
		Vector<StringValuePair>[] gramLsts = 
						new Vector[IndexerConfig.NGRAM_MAX_ROUND];
		
		loadKGramRecords(gramLsts,indexID);
		pfxSfxPruning(gramLsts);
		prunePostingList(gramLsts,indexID);
		
		long time_end = System.currentTimeMillis();
		System.out.println("----marginal pruning:posting list pruning done");
		System.out.println("Time spent on mar. sel pruning:"+(time_end-time_start));
	}	
	
	
	/**
	 * @param kgramLst: the kgram record list
	 * @param pfxLst: the (k-1)-gram record list
	 * mark all kgrams which doesnt satisfy the prefix beta-optimality test
	 */
	private void pfxPruning(Vector<StringValuePair> kgramLst,
							Vector<StringValuePair> pfxLst){
		int ctr = 0, pfxLstCtr = 0;
		while(ctr < kgramLst.size()){
			String gram = kgramLst.elementAt(ctr).key;
			String gramPfx = gram.substring(0,gram.length()-1);
			KGramRecord rec = (KGramRecord)kgramLst.elementAt(ctr).Value;
			
			if(IndexerConfig.DEBUG)
				System.out.println("prfxPrunning: "+ Utility.transformText(gram)+"["+rec+"]");
			
			
			if(pfxLstCtr < pfxLst.size()){
				String pfx = pfxLst.elementAt(pfxLstCtr).key;
				if(gramPfx.compareTo(pfx) == 0){ //We need to check the sel difference
					int gramCnt = rec.cnt;
					int pfxCnt = ((KGramRecord)pfxLst.elementAt(pfxLstCtr).Value).cnt;
					if(pfxCnt < gramCnt)
						System.err.println("ERR: pfx's cnt is less than that of superstring");
					rec.pfxcnt = pfxCnt;
					if(((double)pfxCnt/IndexerConfig.doc_limit)<
							IndexerConfig.SEL_THRESHOLD &&
						((double)(pfxCnt-gramCnt)/IndexerConfig.doc_limit)<
							IndexerConfig.SEL_BETA){
						if(IndexerConfig.DEBUG)
							System.out.println("Mar. Gram Removed for pfx: "+ Utility.transformText(gram) 
												+"["+gramCnt+"]"
												+" : "+Utility.transformText(pfx) +"["+pfxCnt+"]");
						
						rec.nullifyPostingLst(); //no need to output postinglist
					}else{ }
					ctr++;
				}else if(gramPfx.compareTo(pfx) > 0){ //check the next pfx
					pfxLstCtr++;
				}else{			// did find a prefix of the gram; add the gram and move on
					System.err.println("ERR: A superstring's pfx is not in pfx lst"+
								gramPfx+":"+pfx);
					ctr++;
				}
			}else{				//The prefix list has finished; the superstring list should also finished
				ctr++;
			}
		}
	}
	
	/**
	 * @param kgramLst: the kgram record list
	 * @param sfxLst: the (k-1)-gram record list
	 * mark all kgrams which doesnt satisfy the srefix beta-optimality test
	 */
	private void sfxPruning(Vector<StringValuePair> kgramLst,
						Vector<StringValuePair> sfxLst){
		int ctr = 0; 
		int sfxLstCtr = 0;
		while(ctr < kgramLst.size()){
			String gram = kgramLst.elementAt(ctr).key;
			String gramSfx = gram.substring(1);
			KGramRecord rec = (KGramRecord)kgramLst.elementAt(ctr).Value;
			
			if(sfxLstCtr < sfxLst.size()){
				String sfx = sfxLst.elementAt(sfxLstCtr).key;
				if(gramSfx.compareTo(sfx) == 0){ //We need to check the sel difference
					int gramCnt = rec.cnt;
					int sfxCnt = ((KGramRecord)sfxLst.elementAt(sfxLstCtr).Value).cnt;
					if(sfxCnt < gramCnt)
						System.err.println("ERR: sfx's cnt is less than that of superstring:"+
								Utility.transformText(gram)+"["+
								gramCnt+"]"+":"+
								Utility.transformText(sfx)+"["+sfxCnt+"]");
					rec.sfxcnt = sfxCnt;
					if(((double)sfxCnt/IndexerConfig.doc_limit)<
							IndexerConfig.SEL_THRESHOLD &&
						((double)(sfxCnt-gramCnt)/IndexerConfig.doc_limit)<
							IndexerConfig.SEL_BETA){
						if(IndexerConfig.DEBUG)
							System.out.println("Mar. Gram Removed for sfx: "+ Utility.transformText(gram) 
									+"["+gramCnt+"]"
									+" : "+Utility.transformText(sfx) +"["+sfxCnt+"]");
						rec.nullifyPostingLst(); //no need to output postinglist
					}else{ } 
					ctr++;
				}else if(gramSfx.compareTo(sfx) > 0){
					sfxLstCtr++;
				}else{
					//System.err.println("ERR: A superstring's sfx is not in sfx lst "+
					//		gramSfx+":"+sfx);
					ctr++;
				}
			}else{
				ctr++;
			}
		} 
	}
	
	private void pfxSfxPruning(Vector<StringValuePair>[] gramLst){	
		// Step 1. Use the defn. of marginally selective kgrams to generate the lists
		// of non-marginally selective kgrams; use sort instead of hashtable
		
		for(int i = IndexerConfig.NGRAM_MAX_ROUND; i > 1; i--){
			//Get the list of sorted! selective grams of length i and remove
			// the marginally selective grams based on its prefixes
			//if(IndexerConfig.DEBUG)
			System.out.println("Start Pfx based mar. gram pruning... on length "
									+ i +" init size "+ gramLst[i-1].size());
			pfxPruning(gramLst[i-1],gramLst[i-2]);
			VectorSort.sortStringValuePairOnSfx(gramLst[i-1]);
			if(IndexerConfig.DEBUG){
				System.out.println("********** Before sufix pruning Selective Kgrams of length "+i);
				for(int j = 0; j < gramLst[i-2].size(); j++){
					System.out.println("B4 sfx pruning "+ 
									Utility.transformText(gramLst[i-2].elementAt(j).key)+
									  ":"+gramLst[i-2].elementAt(j).Value);
				}
			}
			
			sfxPruning(gramLst[i-1],gramLst[i-2]);
			//Resort the list of grams by its length (i-1) prefix
			VectorSort.sortStringValuePairs(gramLst[i-1]);
			//System.out.println("----- Final Remaining Selective Kgrams "+finalGramLst[i-1].size());
			
		}
		System.out.println("----marginal pruning:NGram pruning done");
	}


	private void prunePostingList(Vector<StringValuePair>[] gramLst,int indexID){	
		ByteOutputBuffer[] kgramBuffers = new
					ByteOutputBuffer[IndexerConfig.NGRAM_MAX_ROUND];
		for(int i = 0; i < IndexerConfig.NGRAM_MAX_ROUND; i++){
			kgramBuffers[i] = new ByteOutputBuffer
				(outputDir+IndexerConfig.gramOffset+IndexerConfig.delimiter+(i+1)+IndexerConfig.delimiter+indexID,
					OUTPUT_BUFFER_SIZE);
		}
		// Rescan the list of kgram postings to remove the posting lists
		// of marginally selective kgrams
		if(IndexerConfig.DEBUG)
			System.out.println("===========Output new posting lists=============");
		for(int i = IndexerConfig.NGRAM_MAX_ROUND-1; i > 0; i--){
			// Open the corresponding posting lists files to read
			RandomAccessFile rf = null;
			try{
				rf= new RandomAccessFile(this.outputDir+IndexerConfig.tmpPostingFile+IndexerConfig.delimiter+(i+1)
											+ IndexerConfig.delimiter+indexID,"r");
				
				// Open a new posting lists file to write
				ByteOutputBuffer bufOut = new ByteOutputBuffer
						(outputDir+IndexerConfig.postingFile+IndexerConfig.delimiter+(i+1)+IndexerConfig.delimiter+indexID,
								OUTPUT_BUFFER_SIZE);
				
				int prevOffset = 0;
				int prevSize = 0;
				int newPostingFileOffsetCtr = 0;
				if(IndexerConfig.DEBUG)
					System.out.println("==== Length "+(i+1));
				for(int j = 0; j < gramLst[i].size(); j++){
					KGramRecord rec = (KGramRecord)gramLst[i].elementAt(j).Value;
					String gram = gramLst[i].elementAt(j).key;
					kgramBuffers[i].write(gram);
					
					//read the posting list files of the jth key and write to the new posting list file
					int offset = rec.offset;
					if(offset == -1){
						kgramBuffers[i].write(rec.getBytes());
						continue;  //there is no posting list associated with the kgram
					}
					if(i == IndexerConfig.NGRAM_MAX_ROUND - 1)
						this.optimalKGrams.put(gramLst[i].elementAt(j).key, dummy); //the optimal grams list used for summary
					
					rf.skipBytes(offset - prevOffset - prevSize);
					//Read the size 
					int size = rec.size;
					byte[] postingLst = new byte[size];
					rf.read(postingLst);
					
					prevSize = size;
					prevOffset = offset;
					
					//Append the size and postings list bytes to the buffer
					bufOut.write(postingLst);
					//Append the gram and the offset to the ngram buffer
					rec.offset = newPostingFileOffsetCtr;
					//write out the kgram record ONLY after the offset is changed
					kgramBuffers[i].write(rec.getBytes());
					
					newPostingFileOffsetCtr+= (size);
						
					if(IndexerConfig.DEBUG)
						System.out.println(Utility.transformText(gramLst[i].elementAt(j).key)
										+ gramLst[i].elementAt(j).Value+" postSize:"+size);
					
				}
				//Need to flush out the remaining contents in the buffer
				bufOut.close();
				kgramBuffers[i].close();
				rf.close();
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				System.out.println(e.getMessage());
				System.out.println("length "+(i+1)+" buffered usedSize: "+ mergeBufferUsedSpaces[i]
				                     + " buffer ttlSize: " +  mergeBufferArray[i].length+
				                     " sizeByte:" + this.SIZE_BYTE_SIZE);
			}catch(Exception e){
				e.printStackTrace();
			}		
		}
		// finally write the length 1 kgram list and move tmpPosting to posting
		try{
			for(int j = 0; j < gramLst[0].size(); j++){
				KGramRecord rec = (KGramRecord)gramLst[0].elementAt(j).Value;
				kgramBuffers[0].write(gramLst[0].elementAt(j).key);
				kgramBuffers[0].write(rec.getBytes());
				if(IndexerConfig.DEBUG){
					System.out.println(Utility.transformText(gramLst[0].elementAt(j).key)
									+ rec );
				}
			}
			kgramBuffers[0].close();
			
			File f = new File(outputDir+IndexerConfig.tmpPostingFile+
					IndexerConfig.delimiter+
					"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID);
			f.renameTo(new File(outputDir+IndexerConfig.postingFile+
					IndexerConfig.delimiter+
					"1" + IndexerConfig.delimiter+IndexerConfig.KGRAM_INDEX_ID));
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void scanCorpusNBuildSubIndices(){
		noFileScanned = 0;
		Vector<File> stack = new Vector<File>();
		
		stack.add(0, new File(this.dataSrc));
		while(!stack.isEmpty()){
			File f = stack.remove(0);
			if(f.isFile()){
				scanFile(f,noFileScanned); //noFileScanned is the id of 
										   //the file scanned
				noFileScanned ++;
				if(noFileScanned % IndexerConfig.progress_indicator == 0)
					System.out.println(noFileScanned + "...");
				if(noFileScanned > IndexerConfig.doc_limit) break;
			}else{
				File[] children = f.listFiles();
				for(int i = 0; i < children.length; i++){
					if(children[i].isFile()){
						scanFile(children[i],noFileScanned);
						noFileScanned ++;
						if(noFileScanned % IndexerConfig.progress_indicator==0)
							System.out.println(noFileScanned + "...");
						if(noFileScanned > IndexerConfig.doc_limit) break;
					}else{
						stack.add(children[i]);
					}
				}
				if(noFileScanned > IndexerConfig.doc_limit) break;
			}
		}		
	}
	
	private void scanBigFileNBuildSubIndices(){
		noFileScanned = 0;
		try{
			long start = System.currentTimeMillis();
			FileInputStream fs = new FileInputStream(this.dataSrc + BIG_FILE_NAME);
			BufferedInputStream bufInput = new BufferedInputStream(fs,this.BUF_SIZE);
			
			byte[] lengthByte = new byte[4];
			int i = 0;
			while(bufInput.read(lengthByte)!= -1){
				String fileContent = null;
				int length = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
				(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);
				byte[] fileBytes = new byte[length];
				bufInput.read(fileBytes);
				scanFileString(new String(fileBytes),noFileScanned);
				noFileScanned ++;
				if(noFileScanned % IndexerConfig.progress_indicator==0)
					System.out.println(noFileScanned + "...");
				if(noFileScanned > IndexerConfig.doc_limit) break;
			}
			bufInput.close();
			fs.close();
			long end = System.currentTimeMillis();
		}catch(Exception e) {e.printStackTrace();}
	}
	
	
	private void scanFileString(String fileString,int fid){
		
		if(IndexerConfig.DEBUG)
			System.out.println("*******************Now scanning file "+ fid+":"+fileString);
		//docNgrams contains all distinct ngrams with their counts
		if(IndexerConfig.KGRAM_INDEX_INCLUDED)
			updateKGramIndex(fid,fileString);
	}
	
	
	private void scanFile(File f,int fid){
		long read_start = System.currentTimeMillis();
		StringBuffer file_contents = new StringBuffer(3*IndexerConfig.FS_BLOCK_SIZE);
		try{
			FileInputStream fs = new FileInputStream(f);
			byte[] bs = new byte[IndexerConfig.FS_BLOCK_SIZE];
			int len = 0;
			while((len=fs.read(bs))!= -1){
				file_contents.append(new String(bs,0,len));
			}
			fs.close();
		}catch(Exception e){
			System.out.println(e.toString());
		}
		long read_end = System.currentTimeMillis();
		this.read_time += (read_end - read_start);
		
		String fileString = file_contents.toString();
		
		if(IndexerConfig.DEBUG)
			System.out.println("*******************Now scanning file "+ fid+":"+fileString);
		//docNgrams contains all distinct ngrams with their counts
		if(IndexerConfig.KGRAM_INDEX_INCLUDED)
			updateKGramIndex(fid,fileString);
	}
	
	private void updateKGramIndex(int fid, String fileString){
		HashMap<String,Integer> docNgrams = 
			new HashMap<String,Integer>(AVG_FILE_SIZE*IndexerConfig.NGRAM_MAX_ROUND);

		//First find all Max length KGrams
		for(int i = 0; i <= fileString.length() - IndexerConfig.NGRAM_MAX_ROUND; i++){
			String gram = fileString.substring(i, i+IndexerConfig.NGRAM_MAX_ROUND);
			if(!docNgrams.containsKey(gram)){
				docNgrams.put(gram, new Integer(1));
			}
		}

		//TODO: Add in a few more doc ending grams
		for(int i = fileString.length()-1; 
		i > (fileString.length() - IndexerConfig.NGRAM_MAX_ROUND ) && i >=0;
		i--){	
			String g = fileString.substring(i, fileString.length());
			for(int j = fileString.length() - i; j < IndexerConfig.NGRAM_MAX_ROUND; j++){
				g = g + "$";
			}
			if(IndexerConfig.DEBUG && 
					g.length() != IndexerConfig.NGRAM_MAX_ROUND)
				System.err.println("Padding last strings failed " + g + "\t " + g.length());
			if(docNgrams.get(g) == null){
				docNgrams.put(g, new Integer(1));
			}
		}

		// Add ngram in docNgrams to the in-memory hash
		Iterator <String> e = docNgrams.keySet().iterator();
		while(e.hasNext()){
			String ngram = e.next();
			int count = docNgrams.get(ngram).intValue();
			byte[] varByteDocId = {(byte)(fid >> 24), (byte)(fid >> 16) , 
					(byte)(fid >> 8), (byte)fid};

			if(IndexerConfig.DEBUG)
				System.out.print("scan "+ Utility.transformText(ngram)+":"+count);
			appendToPostingsList(varByteDocId,ngram,this.KGRAM_INDEX_MEM_LIMIT, IndexerConfig.KGRAM_INDEX_ID);
		}
		
	}

	
	
	/**
	 * @param postingEntry:	the new posting entry to insert into the in-memory indices
	 * @param term: the term of which postingEntry belongs
	 * @param postingsLstLimit: the maximum size limit of in memory inverted postingslist
	 * @param inMemoryIndiceID: the id which indices the memory usage of currentUsedSize: KGRAM is 0,
	 * 												MINSPAN is 1, MAXSPAN is 2 and Proejctive is 3
	 */
	private void appendToPostingsList(byte[] postingEntry,String term, 
									int postingsLstLimit,
									int inMemoryIndiceID){
		byte[] postingsLst= this.dictionaryLst[inMemoryIndiceID].get(term);
		int currentPostingsLength = 0;
		if(postingsLst != null)
			currentPostingsLength = postingsLst.length;
		else
			currentPostingsLength = this.INIT_POSTINGS_SIZE;
		
		if(stillEnoughMemory(term,inMemoryIndiceID, currentPostingsLength, postingsLstLimit)){
			if(IndexerConfig.DEBUG)
				System.out.print(" Memory sufficient...");
			if(postingsLst != null){	
				if(IndexerConfig.DEBUG)
					System.out.print(" find in dict..."+ term);

				int limit = postingsLst[0]<<24 | (postingsLst[1]&0xff)<<16 | 
				(postingsLst[2]&0xff)<<8 | (postingsLst[3]&0xff);
				int cur_size = postingsLst[4]<<24 | (postingsLst[5]&0xff)<<16 | 
				(postingsLst[6]&0xff)<<8 | (postingsLst[7]&0xff);	

				if((cur_size + postingEntry.length) <= limit){ //No array double is required
					System.arraycopy(postingEntry, 0, postingsLst, 
							HEADER_SIZE+ cur_size, postingEntry.length);
					cur_size += postingEntry.length;
					//update the cur_size in header
					byte[] cur_size_bytes = {(byte)(cur_size >> 24), (byte)(cur_size >> 16) , 
							(byte)(cur_size >> 8), (byte)cur_size};
					System.arraycopy(cur_size_bytes, 0, postingsLst, 
							4 , HEADER_SIZE/2);

				}else{
					if(IndexerConfig.DEBUG)
						System.out.print(" array used up expanding...");
					byte[] expandedArray = new byte[2*limit+HEADER_SIZE];

					//first copy the old content over

					System.arraycopy(postingsLst, HEADER_SIZE, expandedArray, 
							HEADER_SIZE, cur_size);
					System.arraycopy(postingEntry, 0, expandedArray, 
							HEADER_SIZE + cur_size, postingEntry.length);
					//update the limit and cur_size bytes
					limit *= 2;
					cur_size += (postingEntry.length);
					byte[] cur_size_bytes = {(byte)(cur_size >> 24), (byte)(cur_size >> 16) , 
							(byte)(cur_size >> 8), (byte)cur_size};
					System.arraycopy(cur_size_bytes, 0, expandedArray, 
							4 , HEADER_SIZE/2);
					byte[] limit_bytes = {(byte)(limit >> 24), (byte)(limit >> 16) , 
							(byte)(limit >> 8), (byte)limit};
					System.arraycopy(limit_bytes, 0, expandedArray, 
							0 , HEADER_SIZE/2);

					// update the total memory usage: since expandedArray doubles postingLst
					this.current_in_memory_invertedLst_size[inMemoryIndiceID] += (limit/2) ;
					// udpate the pointer to postings
					this.dictionaryLst[inMemoryIndiceID].put(term, expandedArray);
				}

			}else{
				if(IndexerConfig.DEBUG)
					System.out.print(" new to dict..." + term);
				initNewPostings(term,postingEntry,inMemoryIndiceID);
			}
		}else{
			if(IndexerConfig.DEBUG)
				System.out.print(" Memory used up dump dict..." + term);
			dumpInMemoryInvertedFile(inMemoryIndiceID);
			/*We still need to add the current word pair*/
			initNewPostings(term,postingEntry,inMemoryIndiceID);
		}
		if(IndexerConfig.DEBUG)
			System.out.println();
	}
	
	
	/**
	 * @param term: a new term to init its posting list
	 * @param postingEntry: a first posting entries of the list
	 * @param inMemorySubIndicesID: the ID of the in memory subindices
     */
	private void initNewPostings(String term,byte[] postingEntry,int inMemorySubIndicesID){
		byte[] postingsLst = new byte[HEADER_SIZE+ INIT_POSTINGS_SIZE];
		int limit = INIT_POSTINGS_SIZE;

		int cur_size = postingEntry.length ;

		byte[] cur_size_bytes = {(byte)(cur_size >> 24), (byte)(cur_size >> 16) , 
				(byte)(cur_size >> 8), (byte)cur_size};
		System.arraycopy(cur_size_bytes, 0, postingsLst, 
				4 , HEADER_SIZE/2);
		byte[] limit_bytes = {(byte)(limit >> 24), (byte)(limit >> 16) , 
				(byte)(limit >> 8), (byte)limit};
		System.arraycopy(limit_bytes, 0, postingsLst, 
				0 , HEADER_SIZE/2);
		//Add the (docId, freq) pair
		System.arraycopy(postingEntry, 0, postingsLst, 
				HEADER_SIZE, postingEntry.length);
		// update the total memory usage
		this.current_in_memory_invertedLst_size[inMemorySubIndicesID] +=
			term.length() * STRING_UNIT_LENGTH + 
			(HEADER_SIZE+INIT_POSTINGS_SIZE);
		// update the pointer to postings
		this.dictionaryLst[inMemorySubIndicesID].put(term, postingsLst);
		
	}
	
	
	/**
	 * @param term: a term to be put into the in memory index
	 * @param indicesID: the ID of the indices
	 * @param currentPostingLength: the size of the current posting list
	 * @param limit: the maximum memory that the in memory indice can ever use
	 * @return true if there is still enought memory
	 */
	private boolean stillEnoughMemory(String term,int indicesID, int currentPostingLength,int limit){
		return (this.current_in_memory_invertedLst_size[indicesID] + 
				term.length() * this.STRING_UNIT_LENGTH + 
				(currentPostingLength - HEADER_SIZE)) * MEM_DATA_REP_FACTOR <= limit;
	}
	
	/**
	 * @param indicesID: the ID of the in memory index
	 */
	private void dumpInMemoryInvertedFile(int indicesID){
		//First sort the list of ngrams in dictionary
		int ctr = 0;
		String[] gramLst = new String[this.dictionaryLst[indicesID].size()];
		Set<String> enumKey = this.dictionaryLst[indicesID].keySet();
		Iterator<String> is = enumKey.iterator();
		while(is.hasNext()){
			gramLst[ctr++] = is.next();
		}
		RAMSort(gramLst);
		
		BufferedOutputStream fout = null;
		try{
			fout = new BufferedOutputStream(
					new FileOutputStream(this.outputDir+IndexerConfig.spimiSub+
									IndexerConfig.delimiter+this.num_runs[indicesID]
									+IndexerConfig.delimiter+indicesID),OUTPUT_BUFFER_SIZE);
			//Dump the inverted lists
			if(IndexerConfig.DEBUG)
				System.out.println("============ On Disk Inverted Index "+ indicesID + "============");
			for(int i = 0; i < gramLst.length; i++){
				byte[] postingsLst = this.dictionaryLst[indicesID].get(gramLst[i]);
				// First write out the ngram
				fout.write(gramLst[i].getBytes());
				fout.write(this.SEPARATOR_BYTE);
				// Followed by size count of the posting list
				fout.write(postingsLst, 4, 4);
				int size = postingsLst[4]<<24 | (postingsLst[5]&0xff)<<16 | 
							(postingsLst[6]&0xff)<<8 | (postingsLst[7]&0xff);
				fout.write(postingsLst, HEADER_SIZE, size);
				
				if(IndexerConfig.DEBUG){
					System.out.print(Utility.transformText(gramLst[i])+"|\t");
					System.out.print(size + "|\t");
					//Vector<Integer> nums = IndexCompressor.VBDECODE(postingsLst, 
					//												HEADER_SIZE, HEADER_SIZE+size-1);
					//System.out.print("[");
					//for(int j = 0; j < nums.size(); j++){
					//	System.out.print(nums.elementAt(j)+" ");
					//}
					//System.out.print("]");
					//for(int j = HEADER_SIZE; j < postingsLst.length; j++){
					//	System.out.print(" "+ Utility.byte2bits(postingsLst[j]) +"("+postingsLst[j]+")");
					//}
					System.out.println();
				}
			}
			
			fout.close();
		}catch(IOException e){
			e.printStackTrace();
		}		
		//Resetup in memory data structures
		this.dictionaryLst[indicesID] = new HashMap<String,byte[]>(this.INIT_DICTIONARY_SIZE[indicesID]);
		//this.ngram2PrevDocId = new HashMap<String,Integer>(100000);
		this.current_in_memory_invertedLst_size[indicesID] = 0;
		this.num_runs[indicesID] ++;
	}
	


	/**
	 * @param first: the first run file's id
	 * @param last: the last run file's id
	 * @param maxSeqLength: the length of seq stored in the run files generated in phase 1
	 * @param indexID: the id of index: used to read the specific index
	 */
	private void prunedMergeAndOutputIndices(int first, int last,int maxSeqLength,int indexID,double selThreshold){
		// Allocate K buffers each of size > |no. doc in corpus|
		mergeBufferArray = new byte[maxSeqLength][MERGE_BUFFER_SIZE];
		ngramsBufferArray = new byte[maxSeqLength][MERGE_BUFFER_SIZE];
		kgramPostingsLst = new Vector[maxSeqLength];
		mergeBufferUsedSpaces = new int[maxSeqLength];
		ngramBufferUsedSpaces = new int[maxSeqLength];
		this.postingsLstOffsets = new int[maxSeqLength];
		
		// Read postings from all runs produced in Step 1
		prevPfxLst = new String[maxSeqLength];
		prefixLst = new String[maxSeqLength];
		isUnselective = new boolean[maxSeqLength];
		for(int i = 0; i < prefixLst.length; i++){
			prevPfxLst[i] = null;
			prefixLst[i] = null;
			isUnselective[i] = false;
			mergeBufferUsedSpaces[i] = 0;
			ngramBufferUsedSpaces[i] = 0;
			this.postingsLstOffsets[i] = 0;
			kgramPostingsLst[i] = new Vector<byte[]>();
		}
		PriorityQueue<NGramRunPair> pq = new PriorityQueue<NGramRunPair>();
		//open the run files for reading
		BufferedInputStream[] runReaders = new BufferedInputStream[last-first+1];
		NGramRunPair[] runGrams = new NGramRunPair[last-first+1];  //storing the current ngram of each run
		String cur_ngram = null;  //the least gram in alphabetic order
		for(int i = first; i <= last; i++){
			try{
				runReaders[i] = new BufferedInputStream(
					new FileInputStream(this.outputDir+IndexerConfig.spimiSub+IndexerConfig.delimiter+i+IndexerConfig.delimiter+indexID), 
					MERGE_INPUT_BUFFER_SIZE);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// Read the first posting from each run file
		byte[] ngram = new byte[maxSeqLength];
		for(int i = first; i<= last; i++){
			//System.out.println("Reading the head of run files");
			int ctr = 0;
			int length = 0;
			// First read the next ngram from the run file
			try{
				while((runReaders[i].read(ngram)) != -1){
					//Read the seperator byte
					runReaders[i].read();
					ctr++;
					//Read in the length integer
					byte[] lengthByte = new byte[4];
					runReaders[i].read(lengthByte);
					length = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
						(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);
					break;
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			if(ctr > 0){
				String gram = new String(ngram,0,ngram.length);
				NGramRunPair p= new NGramRunPair(gram,i,length);
				runGrams[i] = p;
				pq.add(p);
			}else
				runGrams[i] = null;
		}
	
		StringBuffer sb = new StringBuffer(MIN_STRING_BUF_SIZE);
		while(true){
			if(pq.size() == 0) // all run are exhausted
				break;
			else{
				NGramRunPair min = pq.poll();
				String minGram = min.ngram;
				int run = min.runNum;
				int length = min.length;
				if(IndexerConfig.DEBUG)
					System.out.println("PQ min "+min);
				
				// Extract all prefixes of the minGram: prefixList[i] stores the length (i+1) prefix
				for(int i = 0; i < maxSeqLength; i++){
					String pfx = minGram.substring(0,i+1);
					prevPfxLst[i] = prefixLst[i];
					prefixLst[i] = pfx;
					if(prevPfxLst[i] == null || prefixLst[i].compareTo(prevPfxLst[i]) != 0){
						isUnselective[i] = false;
					}
				}
				// Reading in the posting list of minGram
				byte[] pLst = new byte[length];
				try{
					runReaders[run].read(pLst);
				}catch(IOException e){
					e.printStackTrace();
				}
				
				
				for(int i = maxSeqLength; i >= 1; i--){
					// Read the length of the posting list
					if(!isUnselective[i-1]){
						if(prevPfxLst[i-1] == null || prefixLst[i-1].compareTo(prevPfxLst[i-1]) != 0){ 
						// no union required; just append
							appendToBuffer(i-1,pLst,prefixLst[i-1],maxSeqLength,selThreshold,indexID);
						}else{ // We need to do a Union
							mergeIntoBuffer(i-1,pLst,prefixLst[i-1]);
						}
					}
				}
				// Reading the next posting of the leading run file
				int ctr = 0;
				int newlength = 0;
				// First read the next ngram from the run file
				try{
					while(runReaders[run].read(ngram) != -1){
						//Read in the seperator byte
						runReaders[run].read();
						ctr++;
						//Read in the length integer
						byte[] lengthByte = new byte[4];
						runReaders[run].read(lengthByte);
						newlength = lengthByte[0]<<24 | (lengthByte[1]&0xff)<<16 | 
									(lengthByte[2]&0xff)<<8 | (lengthByte[3]&0xff);
						break;

					}
				}catch(IOException e){
					e.printStackTrace();
				}
				if(ctr > 0){
					String gram = new String(ngram,0,ngram.length);
					NGramRunPair p= new NGramRunPair(gram,run,newlength);
					runGrams[run] = p;
					pq.add(p);
				}else
					runGrams[run] = null;
			}
		}
		//Flush out the remaining contents in posting list vector AND the buffers
		for(int i = 0; i < maxSeqLength; i ++){
			//Flush out remaining contents in vector
			prevPfxLst[i] = prefixLst[i]; // make the last gram the prev one so that appendToBuffer can flush it out
			appendToBuffer(i,null,null,maxSeqLength,selThreshold,indexID);
		}
		
	}
	
	
	
	
	class DocIdCountPair implements Comparable{
		int docId;
		int count;
		public DocIdCountPair(int d, int c){
			this.docId = d;
			this.count = c;
		}
		public int compareTo(Object o){
			DocIdCountPair p = (DocIdCountPair)o;
			if(this.docId > p.docId) return 1;
			else if(this.docId == p.docId) return 0;
			else return -1;
		}
	}
	/**
	 * @param len: the kgram's length minus 1  
	 * @param pLst: a new postings list of a seq of length (len+1)
	 * @param gram: the seq
	 * @param maxSeqLength: the maximum seq indexed
	 * @param selThreshold: the selectivity threshold of the seq
	 * @param indexID: the id of the index
	 * The method will first union the posting lists of the alphabetically predecessor of gram and flush them into a buffer,
	 * next it place the pLst of current seq gram 
	 */
	private void appendToBuffer(int len,byte[] pLst,String gram,int maxSeqLength,double selThreshold,int indexID){
		//First union the previous ngrams postings
		//Step 1: Decode the postings list and make an array
		Vector<Integer> entryLst = new Vector<Integer>(); 
		
		int total_size = 0;
		for(int i = 0; i < this.kgramPostingsLst[len].size(); i++){
			total_size += this.kgramPostingsLst[len].elementAt(i).length / 
							SPIMIRegexIndexer.DOCID_LENGTH;
		}
		int[] union_postingsLst = new int[total_size];
		
		int tmpCtr = 0;
		for(int i = 0; i < this.kgramPostingsLst[len].size(); i++){
			byte[] postings = kgramPostingsLst[len].elementAt(i);
			for(int j = 0; j < postings.length/SPIMIRegexIndexer.DOCID_LENGTH; j++){
				int docId = postings[SPIMIRegexIndexer.DOCID_LENGTH*j]<<24 | 
							(postings[SPIMIRegexIndexer.DOCID_LENGTH*j+1]&0xff)<<16 | 
							(postings[SPIMIRegexIndexer.DOCID_LENGTH*j+2]&0xff)<<8 | 
							(postings[SPIMIRegexIndexer.DOCID_LENGTH*j+3]&0xff);
				union_postingsLst[tmpCtr++] = docId;
			}
		}

		//Step 2: Quicksort and eliminate the duplicates in the array with its payload (i.e. count)
		int[] distinctLst = null; 
		int no_distinctDocId = 0;
		if(len != maxSeqLength -1){
			try{
				Arrays.sort(union_postingsLst);
			}catch(Throwable t){
				t.printStackTrace();
				System.out.println("Length: "+union_postingsLst.length+ "1st items: "+
									union_postingsLst[0]);
				ByteOutputBuffer out = new ByteOutputBuffer(this.outputDir+"array.txt",1024*10);
				for(int i = 0; i < union_postingsLst.length; i++){
					out.write(union_postingsLst[i]);
				}
				out.close();
			}
			distinctLst = new int[union_postingsLst.length];
			if(union_postingsLst.length > 0){
				distinctLst[no_distinctDocId++] = union_postingsLst[0];
				for(int i = 1; i < union_postingsLst.length; i++){
					if(union_postingsLst[i] != union_postingsLst[i-1])
						distinctLst[no_distinctDocId++] = union_postingsLst[i];
				}
			}
		}else{
			distinctLst = union_postingsLst;
			no_distinctDocId = distinctLst.length;
		}
		//Step 3: Test the sizes of the final posting lists
		//Step 4.1: If the size is not above threshold, write it out to the buffer
		if(no_distinctDocId > 0 && ((double)no_distinctDocId/this.noFileScanned ) 
				<  selThreshold){
			
			//Reencode the list and write it to the buffer
			byte[] tmp = new byte[no_distinctDocId *  this.NUM_FILENAME_BYTES];
			int prevDocId =  0; 
			int ctr = 0;
			for(int i = 0 ; i < no_distinctDocId; i++){
				int gap = distinctLst[i] - prevDocId;
				if(gap < 0) System.err.println("Gap is less than 0: "+gap );
				byte[] firstIdEncoded = IndexCompressor.VBEncodeNumber(gap);
				System.arraycopy(firstIdEncoded, 0, tmp, ctr, firstIdEncoded.length);
				ctr += firstIdEncoded.length;
				prevDocId = distinctLst[i];
			}
			int lastDocId = distinctLst[distinctLst.length-1];

			if(IndexerConfig.DEBUG)
					System.out.println("Write selective gram of length "+ (len+1)+":" 
										+ Utility.transformText(this.prevPfxLst[len]));
			try{
				/******* First write to the ngram buffer *******/
				this.kgramBufOut[len].write(this.prevPfxLst[len].getBytes());
				// Write the size bytes
				byte[] cur_size_bytes = {(byte)(ctr >> 24), (byte)(ctr >> 16) , 
						(byte)(ctr >> 8), (byte)ctr};
				this.kgramBufOut[len].write(cur_size_bytes);
				// Write the offset in the postings list
				int tmpOffset = this.postingsLstOffsets[len];
				byte[] tmpOffset_bytes = {(byte)(tmpOffset >> 24), (byte)(tmpOffset >> 16) , 
						(byte)(tmpOffset >> 8), (byte)tmpOffset};
				this.kgramBufOut[len].write(tmpOffset_bytes); 	
				// Write the number of docIDs in the posting list 
				byte[] noDocIDs_bytes = {(byte)(no_distinctDocId>>24), (byte)(no_distinctDocId>>16),
						(byte)(no_distinctDocId>>8), (byte)no_distinctDocId};
				this.kgramBufOut[len].write(noDocIDs_bytes);
				// Write the last docID in the posting list
				byte[] lastDocID_bytes = {(byte)(lastDocId>>24), (byte)(lastDocId>>16),
						(byte)(lastDocId>>8), (byte)lastDocId};
				this.kgramBufOut[len].write(lastDocID_bytes);
				
				/******* End of writing to the ngram buffer *********/

				/******* Next write to the posting list buffer *******/
				this.postingBufOut[len].write(tmp, 0, ctr);
				/******* End of write to the posting list buffer *******/
				
				/******* Update the offset *********/
				this.postingsLstOffsets[len] += ctr;
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			//Step 4.2: otherwise, eliminate the array and inform all prefixes
			/******* First write to the ngram buffer *******/
			try{
				if(this.prevPfxLst[len] != null){
					this.kgramBufOut[len].write(this.prevPfxLst[len].getBytes());
					// Write the size bytes
					byte[] cur_size_bytes = {(byte)(-1 >> 24), (byte)(-1 >> 16) , 
							(byte)(-1 >> 8), (byte)-1};
					this.kgramBufOut[len].write(cur_size_bytes);
					// Write the offset in the postings list
					int tmpOffset = this.postingsLstOffsets[len];
					byte[] tmpOffset_bytes = {(byte)(-1 >> 24), (byte)(-1 >> 16) , 
							(byte)(-1 >> 8), (byte)-1};
					this.kgramBufOut[len].write(tmpOffset_bytes); 	
					// Write the number of docIDs in the posting list 
					byte[] noDocIDs_bytes = {(byte)(no_distinctDocId>>24), (byte)(no_distinctDocId>>16),
							(byte)(no_distinctDocId>>8), (byte)no_distinctDocId};
					this.kgramBufOut[len].write(noDocIDs_bytes);
					// Write the last docID in the posting list
					byte[] lastDocID_bytes = {(byte)(-1>>24), (byte)(-1>>16),
							(byte)(-1>>8), (byte)-1};
					this.kgramBufOut[len].write(lastDocID_bytes);
				}
			}catch(Exception e) {e.printStackTrace();}
			/******* End of writing to the ngram buffer *********/
		}
		//Step 5: Now append the new pLst
		kgramPostingsLst[len] = new Vector<byte[]>();
		kgramPostingsLst[len].add(pLst);
		
	}
	
	private void mergeIntoBuffer(int len,byte[] pLst,String gram){
		//kgramPostingsLst[len] = new Vector<byte[]>();
		kgramPostingsLst[len].add(pLst);	
	}
	



	
	private void showKGramIndices(int maxSeqLength,int indexID){

		/* 1. Output the kgram record and its associated posting lists if exists */
		BufferedInputStream[] kgramPtrReaders = 
			new BufferedInputStream[maxSeqLength];
		RandomAccessFile[] postingFileReaders = 
			new RandomAccessFile[maxSeqLength];
		for(int i = 0; i  < maxSeqLength; i++){
			try{
				kgramPtrReaders[i] = new BufferedInputStream(
						new FileInputStream(this.outputDir+IndexerConfig.gramOffset+
								IndexerConfig.delimiter+(i+1)+IndexerConfig.delimiter+indexID), 
								MERGE_INPUT_BUFFER_SIZE);
				postingFileReaders[i] = new RandomAccessFile(
						this.outputDir+IndexerConfig.postingFile+
						IndexerConfig.delimiter+(i+1)+IndexerConfig.delimiter+indexID,"r");

				System.out.println("=============== Length " + (i+1) + " ================");
				byte[] gramBytes = new byte[i+1];
				int prevOffset = 0;
				while((kgramPtrReaders[i].read(gramBytes)) != -1){
					// Read in the grams
					System.out.print(Utility.transformText(new String(gramBytes))
							+ "[");
					//Read in the offset integer
					byte[] recordBytes = new byte[4];
					int offset = 0, byteCnt=0;
					for(int j = 0; j < KGramRecord.length; j++){
						kgramPtrReaders[i].read(recordBytes);
						int value = recordBytes[0]<<24 | (recordBytes[1]&0xff)<<16 | 
						(recordBytes[2]&0xff)<<8 | (recordBytes[3]&0xff);
						System.out.print(KGramRecord.recordFields[j]+":"+value+",");
						if(j == KGramRecord.OFFSET)
							offset = value;
						if(j == KGramRecord.SIZE)
							byteCnt = value;
					}
					System.out.print("]");
					byte[] pLst = null;
					if(offset != -1){
						// Now read in the corresponding posting list
						postingFileReaders[i].skipBytes(offset - prevOffset);
						
						pLst = new byte[byteCnt];
						postingFileReaders[i].read(pLst);
						Vector<Integer> decodedLst = 
							IndexCompressor.VBDECODE(pLst, 0, pLst.length-1);
						System.out.print("{");	
						int prevId = 0;
						for(int k = 0; k < decodedLst.size(); k++){
							int id = prevId + decodedLst.elementAt(k).intValue();
							System.out.print(id+",");
							prevId = id;
						}
						System.out.print("}");
						prevOffset = offset + byteCnt;
					}
					System.out.println();
				}
				kgramPtrReaders[i].close();
				postingFileReaders[i].close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		/*2. output the summary file */
		System.out.println("===========Summary file content===========");
		try{
			BufferedInputStream summaryReader = new BufferedInputStream(
				new FileInputStream(outputDir+IndexerConfig.summaryFileName+IndexerConfig.delimiter+maxSeqLength+
						IndexerConfig.delimiter+indexID), 
						MERGE_INPUT_BUFFER_SIZE);
			byte[] gramBytes = new byte[maxSeqLength];
			while((summaryReader.read(gramBytes)) != -1){
				System.out.print(Utility.transformText(new String(gramBytes))
						+ "[");
				byte[] lengthBytes = new byte[4];
				summaryReader.read(lengthBytes);
				int length = lengthBytes[0]<<24 | (lengthBytes[1]&0xff)<<16 | 
							(lengthBytes[2]&0xff)<<8 | (lengthBytes[3]&0xff);
				
				System.out.print(length+":");
				byte[] pLst = new byte[length];
				summaryReader.read(pLst);
				pLst = IndexCompressor.VBDECODE2ByteArray(pLst, 0, pLst.length-1);
				
				for(int i = 0; i < pLst.length; i+=4){
					int id = pLst[i+0]<<24 | (pLst[i+1]&0xff)<<16 | 
							(pLst[i+2]&0xff)<<8 | (pLst[i+3]&0xff);
					System.out.print(id+",");
				}
				System.out.print("]");
				System.out.println();
			}
			
			summaryReader.close();
		}catch(Exception e){e.printStackTrace();}
	}
	


	
	byte[] write_buf = new byte[this.OUTPUT_BUFFER_SIZE];
	int cur_output_buf_size = 0;
	/**
     */

	class NGramRunPair implements Comparable{
		String ngram;
		int runNum;
		int length;
		NGramRunPair(String g,int p,int length){
			this.ngram = g;
			this.runNum = p;
			this.length = length;
		}
		public int compareTo(Object o){
			NGramRunPair nrp = (NGramRunPair)o;
			if(this.ngram.compareTo(nrp.ngram) < 0)
				return -1;
			if(this.ngram.compareTo(nrp.ngram) == 0 && this.runNum < nrp.runNum)
				return -1;
			return 1;
		}
		public String toString(){
			return Utility.transformText(ngram)+":"+runNum+"("+length+")";
		}
		
	}
	
	private void RAMSort(String[] gramLst){
		quicksort(gramLst,0,gramLst.length-1);
	}
	
    private void quicksort(String[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private int partition(String[] a, int left, int right) {
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
    private  boolean less(String x, String y) {
        return (x.compareTo(y) < 0);
    }

    // exchange a[i] and a[j]
    private  void exch(String[] a, int i, int j) {
        String swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }
}

class IntPair{
	int cnt;  // the number of doc ids in the postings
	int offset; // the offset in the posting list file
	public IntPair(int c, int offset){
		this.cnt = c;
		this.offset = offset;
	}
	public String toString(){
		return "["+cnt+","+offset+"]";
	}
}


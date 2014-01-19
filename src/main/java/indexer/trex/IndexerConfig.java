package indexer.trex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;

public class IndexerConfig {
	public static int doc_limit = Integer.MAX_VALUE;
	public static int maxWindowLength = 400000000;  // we assume the biggest file we scan is about 400Mbytes
	public static String[] symbols = {"~","`","!","@","#","$",
			"%","^","&","*","(",")",
			"-","_","+","=","{","[","}","]",
			"|","\\",":",";","\"","'",
			"<",",",">",".","?","/","0","s","A"};
	public static int no_sym = 35;
	public static String indexDir	= "C:\\softwares\\EnronData\\EnronSIndex\\";
	public static boolean ordered = true;
	public static String fileDir2Index = 
		"/scratch/SIGMOD09/datasets/test/dir1";
		//"/scratch/ExperimentData/VLDB08/Tiny_Docsets";
	    //"/scratch/ExperimentData/VLDB08/Enron2Lvl";
		//"/scratch/ExperimentData/VLDB08/DBLifeTest";
    public static double SEL_THRESHOLD = 0.2;
    public static double SEL_BETA = 0.05;
    public static int NGRAM_MAX_ROUND = 10;
    public static int SEL_MAX_ROUND = 5;
    public static int FS_BLOCK_SIZE = 8192;
    public static boolean DEBUG = false;
    public static boolean LuceneTermVector = false;
    public static boolean LuceneTermFreq = false;
    public static boolean KGRAM_INDEX_INCLUDED = true;
    public static boolean SPAN_INDEX_INCLUDED = true;
    public static boolean PROJECTIVE_INDEX_INCLUDED = true;
    public static boolean TRANSFORM_INDEX_INCLUDED = false;
    public static boolean ZERO_SEL_ON = true;
    public static boolean UPDATABLE = true;
    
    public static int progress_indicator = 50000;
	
    // A set of string constants to define index filename convention
    // The following set of variables names various index files
    /** Index file naming convention **/
	/** a file in the index directory consists of 3 parts seperated by .
	 *  Part A: file function ["spimiSub","tmpPost","post",
	 *  					   "tmpGramOffset","gram","gramOffset","summary"]
	 *  Part B: sequence length [1,2,...,K]
	 *  Part C: IndexID: [0: ngram, 1: minspan, 2: maxspan, 3: proj]
	 * **/
	public static final String spimiSub = "spimiSub";
	public static final String tmpPostingFile = "tempPost";
	public static final String postingFile = "post";
	public static final String gram = "gram";
	public static final String tmpGramOffset = "tmpGramOffset";
	public static final String gramOffset = "gramOffset"; 
	public static final String delimiter = ".";
	public static final String unselGram = "unsel";
	public static final String summaryFileName = "summary";
	public static final String updatedSummaryFileName = "uSummary";
	public static final String updatedGramOffset = "uGramOffset";
	public static final String updatedPostingFile = "uPost";
	
	public static final int NO_INDICES = 5;
	public static final int KGRAM_INDEX_ID = 0; 
	public static final int MIN_SPAN_INDEX_ID = 1;
	public static final int MAX_SPAN_INDEX_ID = 2;
	public static final int PROJECTIVE_INDEX_ID = 3;
	public static final int TRANSFORMED_INDEX_ID = 4;
	
	public static final int BYTES_INT = 4;
	public static final int SIZE_BYTE_LENGTH = 4;
    
    public static void printParameters(){
    	System.out.println("===================System Parameters==============================");
    	System.out.println(new Date());
    	System.out.println("datafileDir:"+ IndexerConfig.fileDir2Index);
    	System.out.println("NGram MAX_ROUND:"+ IndexerConfig.NGRAM_MAX_ROUND);
    	System.out.println("Selective MAX_ROUND:"+ IndexerConfig.SEL_MAX_ROUND);
    	
    	System.out.println("PROJECTIVE_INDEX_INCLUDED:"+PROJECTIVE_INDEX_INCLUDED);
    	System.out.println("KGRAM_INDEX_INCLUDED:"+KGRAM_INDEX_INCLUDED);
    	System.out.println("SPAN_INDEX_INCLUDED:"+SPAN_INDEX_INCLUDED);
    	System.out.println("TRANSFORM_INDEX_INCLUDED:"+TRANSFORM_INDEX_INCLUDED);
    	System.out.println("ZERO SEL ON:"+ZERO_SEL_ON);
    	
    	System.out.println("Selectivity:"+ IndexerConfig.SEL_THRESHOLD);
    	System.out.println("Beta:"+ IndexerConfig.SEL_BETA);
    	System.out.println("Total doc:" + doc_limit);
    	
    	System.out.println("Debug mode: "+ DEBUG);
    	System.out.println("===================================================================");
    }
    public static void main(String[] args){
    	initControlParameters("/home/ting/tmp/iRegexData/indexer.cfg");
    	printParameters();
    }
    public static void initControlParameters(String filename){
		try{
			FileReader reader = new FileReader(filename);
			BufferedReader bufReader = new BufferedReader(reader);
			String line = null;
			while((line = bufReader.readLine()) != null){
				String[] fields = line.split(" ");
				if(fields.length != 2) continue;
				if(fields[0].compareTo("doc_limit") == 0)
					doc_limit = new Integer(fields[1]).intValue();
				else if(fields[0].compareTo("maxWindowLength")== 0)
					maxWindowLength = new Integer(fields[1]).intValue();
				else if(fields[0].compareTo("no_sym") == 0)	
					no_sym = new Integer(fields[1]).intValue();
				else if(fields[0].compareTo("ordered") == 0)
					ordered = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("LuceneTermVector") == 0)
					LuceneTermVector = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("LuceneTermFreq") == 0)
					LuceneTermFreq = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("DEBUG") == 0)
					DEBUG = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("KGRAM_INDEX_INCLUDED") == 0)
					KGRAM_INDEX_INCLUDED = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("SPAN_INDEX_INCLUDED") == 0)
					SPAN_INDEX_INCLUDED = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("PROJECTIVE_INDEX_INCLUDED") == 0)
					PROJECTIVE_INDEX_INCLUDED = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("TRANSFORM_INDEX_INCLUDED") == 0)
					TRANSFORM_INDEX_INCLUDED = new Boolean(fields[1]).booleanValue();
				else if(fields[0].compareTo("ZERO_SEL_ON") == 0)
					ZERO_SEL_ON = new Boolean(fields[1]).booleanValue();
				
				else if(fields[0].compareTo("fileDir2Index") == 0)
					fileDir2Index = fields[1];
				else if(fields[0].compareTo("indexDir") == 0)
					indexDir = fields[1];
				else if(fields[0].compareTo("SEL_THRESHOLD") == 0)
					SEL_THRESHOLD = new Double(fields[1]).doubleValue();
				else if(fields[0].compareTo("SEL_BETA") == 0)
					SEL_BETA = new Double(fields[1]).doubleValue();
				else if(fields[0].compareTo("NGRAM_MAX_ROUND") == 0)
					NGRAM_MAX_ROUND = new Integer(fields[1]).intValue();
				else if(fields[0].compareTo("SEL_MAX_ROUND") == 0)
					SEL_MAX_ROUND = new Integer(fields[1]).intValue();
				else if(fields[0].compareTo("progress_indicator") == 0)
					progress_indicator = new Integer(fields[1]).intValue();
				
			}
			
			reader.close();
			bufReader.close();
			
		}catch(Exception e){
			System.out.println(e.toString());
		}
    }
}

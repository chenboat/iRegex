package indexer;

/*
 * The record associated with each kgram in an updatable index
 */
public class KGramRecord {
	public int size; // the size of the posting list in bytes
	public int offset; // the offset in the posting list file
	public int cnt;  // the number of doc ids in the posting
	public int lastDocID; // the last doc id in the posting list
	public int pfxcnt;    // the doc cnt of the maxpfx's posting list
	public int sfxcnt;    // the doc cnt of the maxsfx's posting list
	public static final int length = 6;
	public static final String[] recordFields =
			{"bytesize","offset","cnt","lastDocID","pfxcnt","sfxcnt"};
	public static final int SIZE = 0;
	public static final int OFFSET = 1;
	public static final int CNT = 2;
	public static final int LASTID = 3;
	public static final int PFXCNT = 4;
	public static final int SFXCNT = 5;
	
	public KGramRecord(byte[] recordBytes){
		int ptr = 0;
		size = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
				(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		int offset = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
			(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		cnt = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
			(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		
		lastDocID = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
			(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		
		pfxcnt = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
			(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		sfxcnt = recordBytes[ptr]<<24 | (recordBytes[ptr+1]&0xff)<<16 | 
			(recordBytes[ptr+2]&0xff)<<8 | (recordBytes[ptr+3]&0xff);
		ptr += 4;
		
	}
	
	
	public KGramRecord(int size, int offset, int count, int lastID){
		this.size = size;
		this.offset = offset;
		this.cnt = count;
		this.lastDocID = lastID;
		this.pfxcnt = 0;
		this.sfxcnt = 0;
	}
	
	
	public KGramRecord(int size, int offset, int count,
                       int pfxCnt, int sfxCnt, int lastID){
		this.size = size;
		this.offset = offset;
		this.cnt = count;
		this.lastDocID = lastID;
		this.pfxcnt = pfxCnt;
		this.sfxcnt = sfxCnt;
	}
	
	public String toString(){
		return "["+size+","+offset+","+cnt+","+lastDocID+
				","+pfxcnt+","+sfxcnt+"]";
	}
	public void nullifyPostingLst(){
		this.size = -1;
		this.offset = -1;
		this.lastDocID = -1;
	}
	public byte[] getBytes(){
		int ptr = 0;
		byte[] bytes= new byte[24];
		byte[] sizebytes = {(byte)(size>>24), (byte)(size>>16),
				(byte)(size>>8), (byte)size};
		System.arraycopy(sizebytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		byte[] offsetbytes = {(byte)(offset>>24), (byte)(offset>>16),
				(byte)(offset>>8), (byte)offset};
		System.arraycopy(offsetbytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		byte[] cntbytes = {(byte)(cnt>>24), (byte)(cnt>>16),
				(byte)(cnt>>8), (byte)cnt};
		System.arraycopy(cntbytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		
		byte[] lastIDbytes = {(byte)(lastDocID>>24), (byte)(lastDocID>>16),
				(byte)(lastDocID>>8), (byte)lastDocID};
		System.arraycopy(lastIDbytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		
		byte[] pfxcntbytes = {(byte)(pfxcnt>>24), (byte)(pfxcnt>>16),
				(byte)(pfxcnt>>8), (byte)pfxcnt};
		System.arraycopy(pfxcntbytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		
		byte[] sfxcntbytes = {(byte)(sfxcnt>>24), (byte)(sfxcnt>>16),
				(byte)(sfxcnt>>8), (byte)sfxcnt};
		System.arraycopy(sfxcntbytes, 0, bytes, ptr, 4);
		ptr += 4;
		
		return bytes;
	}
	
}


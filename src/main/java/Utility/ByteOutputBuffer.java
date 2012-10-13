package Utility;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ByteOutputBuffer {
	BufferedOutputStream buf;
	
	public ByteOutputBuffer(String filename, int bufSize){
		try{
			buf = new BufferedOutputStream(new FileOutputStream(filename),
					bufSize);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void write(String s){
		try{
			buf.write(s.getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void write(int i){
		try{
			byte[] intBytes = {(byte)(i >> 24), (byte)(i >> 16) , 
					(byte)(i >> 8), (byte)i};
			buf.write(intBytes);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void write(long i){
		try{
			byte[] intBytes = {(byte)(i >> 24), (byte)(i >> 16) , 
					(byte)(i >> 8), (byte)i};
			buf.write(intBytes);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void write(byte[] bs){
		try{
			buf.write(bs);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void close(){
		try{
			buf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

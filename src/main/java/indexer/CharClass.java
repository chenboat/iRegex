package indexer;

public class CharClass {
	public int lower;  //lower bound of char range
	public int upper;  //upper bound of char range
	public String str; // a string representation of the char class
	public boolean index;
	
	public CharClass(int l, int u, String str){
		this.lower = l;
		this.upper = u;
		this.str = str;
		this.index = true;
	}
	
	public CharClass(int l, int u, String str, boolean b){
		this.lower = l;
		this.upper = u;
		this.str = str;
		this.index = b;
	}
	
	public String toString(){
		return str;
	}
}


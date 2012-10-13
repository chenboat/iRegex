package indexer;

/**
 * @author tchen
 * a generic container classes which have string typed key and a value
 */

public class StringValuePair {
	public String key;
	public Object Value;
	public StringValuePair(String key, Object v){
		this.key = key;
		this.Value = v;
	}
	
	public String toString(){
		return key +"["+Value+"]";
	}
}

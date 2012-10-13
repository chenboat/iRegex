package Utility;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import sun.awt.windows.ThemeReader;

/**
 * Created by User: ting
 * Date: 5/18/12
 * Time: 6:14 AM
 */
public class FastHash {
  private static String[] alpha;
  public static void main(String[] args){
      int numEntries = 1000 * 1000 * 12;
      int len = 5;
      String all= "abcdefghijklmnopqrstuvwx";
      alpha = new String[all.length()];
      for(int i = 0; i < all.length(); i++)
      {
        alpha[i] = all.substring(i,i+1);
      }
      Object2LongOpenHashMap<String> map = new Object2LongOpenHashMap<String>(numEntries,0.8f);
      enumStrings(0,len,"",map);
      
      System.out.println("Map size: "+ map.size());
  }
  
  public static void enumStrings(int pos, int len, String pfx, Object2LongOpenHashMap<String> map)
  {
    if(pos == len){
        map.put(pfx,111L);
        return;
    }
      
    for(int i = 0; i < alpha.length;i++)
    {
        enumStrings(pos+1,len,pfx+alpha[i],map);
    }
      
  }
  
}

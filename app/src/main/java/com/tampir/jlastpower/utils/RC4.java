package com.tampir.jlastpower.utils;

/* RC4.java
 * author : rahmatul.hidayat@gmail.com
 * copyright 2014
 */
public class RC4 {

	public static String Encrypt(String salt, String data){
		  String a = salt;
		  String b = data;
		  StringBuilder f = new StringBuilder();
		  int[] c = new int[256];
	      for(int i=0;i<256;i++){
	    	  c[i]=i;
	      }
	      
	      int d;
	      int e;
	      
	      d = 0;
	      for(int i=0;i<256;i++){
	         d=(d+c[i]+a.charAt(i%a.length())) % 256;
	         //e=c[i];
	         //c[i]=c[d];
	         //c[d]=e;
	          c[i]=c[i] ^ c[d];
	          c[d]=c[i] ^ c[d];
	          c[i]=c[i] ^ c[d];
	      }
	      
	      int i;
	      i = 0;
	      d = 0;
	      for(int y=0;y<b.length();y++){
	         i=(i+1) % 256;
	         d=(d+c[i]) % 256;
	         
	         //e=c[i];
	         //c[i]=c[d];
	         //c[d]=e;
	          c[i]=c[i] ^ c[d];
	          c[d]=c[i] ^ c[d];
	          c[i]=c[i] ^ c[d];
	         f.append((char) (b.charAt(y) ^ c[(c[i]+c[d]) % 256]));
	      }
	      
	      return f.toString();
	   }
	   public static String Decrypt(String salt, String data){return Encrypt(salt,data);}
}
 

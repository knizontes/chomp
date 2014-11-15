package main_pack.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hasher {
	
	private int keySpace;
	
	public Hasher(int keySpace){
		this.keySpace=keySpace;
	}
	
	public int hash(String input) throws NoSuchAlgorithmException{
		int retval=bytes2uint(sha1(input));
		return retval;
	}
	
    private byte[] sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
         
        return result;
    }
    
    private int bytes2uint(byte[] buffer){
    	int retval=0;
    	for (int i=0; i<buffer.length; ++i)
    		retval+=(buffer[i]<<(i*8))%Integer.MAX_VALUE;
    	if (retval<0)
    		retval+=(-Integer.MIN_VALUE);
    	return (retval%keySpace);
    }
    
    private int _bytes2int(byte[] buffer){
    	int retval=0;
    	for (int i=0; i<buffer.length; ++i)
    		retval+=buffer[i];
    	return (retval%keySpace);
    }
    
    public static void main(String [] args){
    	Hasher hs = new Hasher(40);
    	byte[] hash1;
    	byte[] hash2;
    	byte[] hash3;
    	byte[] hash4;
    	byte[] hash5;
    	int h1;
    	int h2;
    	int h3;
    	int h4;
    	int h5;
    	
    	
    	try {
			hash1=hs.sha1("Knizontes en te kosmos estin");
			hash2=hs.sha1("Knizontes en te kosmos esti");
			hash3=hs.sha1("Kniznntes en te kosmos esti");
			hash4=hs.sha1("Knizootes en te kosmos esti");
			hash5=hs.sha1("Knizontes en te losmos esti");
			h1=hs.bytes2uint(hash1);
			h2=hs.bytes2uint(hash2);
			h3=hs.bytes2uint(hash3);
			h4=hs.bytes2uint(hash4);
			h5=hs.bytes2uint(hash5);
			System.out.println("The hash of \"Knizontes en te kosmos estin\" hashing is:"+h1);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h2);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h3);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h4);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h5);
	    	h1=hs._bytes2int(hash1);
			h2=hs._bytes2int(hash2);
			h3=hs._bytes2int(hash3);
			h4=hs._bytes2int(hash4);
			h5=hs._bytes2int(hash5);
			System.out.println("The hash of \"Knizontes en te kosmos estin\" hashing is:"+h1);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h2);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h3);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h4);
	    	System.out.println("The hash of \"Knizontes en te kosmos esti\" hashing is:"+h5);
	    	
	    	
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	
    	
    }
}

 
 
     

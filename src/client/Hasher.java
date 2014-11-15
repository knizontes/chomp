package client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class Hasher {
	public String stringHash(String input) throws NoSuchAlgorithmException{
		return new String (sha1(input));
	}
	
    private byte[] sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        byte [] retval = new byte [40];
        for (int i=0; i<40;++i){
        	retval[i]=result[i];
        }
        return retval;
    }
    
}

 
 
     

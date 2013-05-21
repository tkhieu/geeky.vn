package tgm.geeky.vn.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {
	
	public static String getMd5(String in) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] bytesOfMessage = String.valueOf(in).getBytes(
				"UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(bytesOfMessage);
		return thedigest.toString();
	}

	
	
}

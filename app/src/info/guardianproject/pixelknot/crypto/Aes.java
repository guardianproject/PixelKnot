package info.guardianproject.pixelknot.crypto;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.Logger;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class Aes {
	public static String DecryptWithPassword(String password, byte[] iv, byte[] message) {
		String new_message = null;
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), Constants.PASSWORD_SALT, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret_key = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret_key, new IvParameterSpec(iv));
			
			new_message = new String(cipher.doFinal(message));
			
		} catch (IllegalBlockSizeException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
		
		return new_message;
	}
	
	public static Map<String, String> EncryptWithPassword(String password, String message) {
		Map<String, String> pack = null;
		String new_message = null;
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), Constants.PASSWORD_SALT, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret_key = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret_key);
			
			AlgorithmParameters params = cipher.getParameters();
			String iv = Base64.encodeToString(params.getParameterSpec(IvParameterSpec.class).getIV(), Base64.DEFAULT);
			
			new_message = Base64.encodeToString(cipher.doFinal(message.getBytes("UTF-8")), Base64.DEFAULT);
			
			pack = new HashMap<String, String>();
			pack.put(iv, new_message);			
		} catch (IllegalBlockSizeException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
		
		
		return pack;
	}
}

package ch.SWITCH.aai.uApprove.components;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.symmetric.AES;
import edu.vt.middleware.crypt.symmetric.SymmetricAlgorithm;
import edu.vt.middleware.crypt.util.Base64Converter;

public class Crypt {
	private final Logger logger = LoggerFactory.getLogger(Crypt.class);
	private final SymmetricAlgorithm alg;
    	
	public Crypt(String sharedSecret) throws UApproveException {
		alg = new AES();
		alg.setIV("uApprove initial vector".substring(0, 16).getBytes());
		alg.setKey(new SecretKeySpec(sharedSecret.getBytes(), 0, 16, AES.ALGORITHM));
	}
	
	public String encrypt(String plaintext) throws UApproveException {
		try {
			alg.initEncrypt();
			String cipher = alg.encrypt(plaintext.getBytes(), new Base64Converter());
			logger.trace("Encrypt '{}' to '{}'", plaintext, cipher);
			return cipher;
		} catch (CryptException e) {
			logger.error("Encryption failed", e);
		    throw new UApproveException(e);
		}
	}
	
	public String decrypt(String cipher) throws UApproveException {
		try {
			alg.initDecrypt();
			String plaintext = new String(alg.decrypt(cipher, new Base64Converter()));
			logger.trace("Decrypt '{}' to '{}'", cipher, plaintext);
			return plaintext;
		} catch (CryptException e) {
			logger.error("Decryption failed", e);
		    throw new UApproveException(e);
		}
	}
}

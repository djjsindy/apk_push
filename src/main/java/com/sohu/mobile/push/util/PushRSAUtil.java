package com.sohu.mobile.push.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class PushRSAUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushRSAUtil.class);
	private static final int KEY_SIZE = 512;
	private static final int BLOCK_SIZE = 53;
	private static final int OUTPUT_BLOCK_SIZE = 64;
	private static SecureRandom secrand = new SecureRandom();
	public static Cipher rsaCipher;
	public static String Algorithm = "RSA";
	public PushRSAUtil() throws Exception {

	}

	public static PrivateKey getPrivateKey(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = Base64Encoder.decode(key);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(Algorithm);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	public static String encodeSecretByPriKey(String privateKeyString,
			String content) throws Exception {

		try {
			rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw e;
		}

		Key privateKey = getPrivateKey(privateKeyString);
		try {
			rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey, secrand);
			byte[] data = content.getBytes("utf-8");
			int blocks = data.length / BLOCK_SIZE;
			int lastBlockSize = data.length % BLOCK_SIZE;
			byte[] encryptedData = new byte[(lastBlockSize == 0 ? blocks
					: blocks + 1) * OUTPUT_BLOCK_SIZE];
			for (int i = 0; i < blocks; i++) {
				rsaCipher.doFinal(data, i * BLOCK_SIZE, BLOCK_SIZE,
						encryptedData, i * OUTPUT_BLOCK_SIZE);
			}
			if (lastBlockSize != 0) {
				rsaCipher.doFinal(data, blocks * BLOCK_SIZE, lastBlockSize,
						encryptedData, blocks * OUTPUT_BLOCK_SIZE);
			}

			return Base64Encoder.encode(encryptedData);

		} catch (InvalidKeyException e) {
			LOGGER.error("",e);
		} catch (ShortBufferException e) {
            LOGGER.error("",e);
		} catch (UnsupportedEncodingException e) {
            LOGGER.error("",e);
		} catch (IllegalBlockSizeException e) {
            LOGGER.error("",e);
		} catch (BadPaddingException e) {
            LOGGER.error("",e);
		}
        return null;
	}

}
package com.sdd.jborg.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class Crypt
{
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";

	private static SecretKey readSecret()
		throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException
	{
		return SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(bytes));
	}

	public static String encrypt(final String src)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, readSecret());
			return Base64.getEncoder().encodeToString(cipher.doFinal(src.getBytes()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(final String src)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, readSecret());
			return new String(cipher.doFinal(Base64.getDecoder().decode(src)));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
package com.sdd.jborg.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.sdd.jborg.util.Crypto.Algorithm.SHA_256;

public class Crypto
{
	public static String bytesToHexString(final byte[] bytes)
	{
		StringBuilder hexLine = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			hexLine.append(String.format("%02X", bytes[i]));
		}
		return hexLine.toString();
	}

	public static byte[] stringToByteArray(final String value)
	{
		return value.getBytes(StandardCharsets.UTF_8);
	}

	public enum Algorithm
	{
		SHA_1,
		SHA_256;

		public String toString()
		{
			return name().toLowerCase().replaceAll("_", "-");
		}
	}

	public static String computeHash(final Algorithm algorithm, final String value)
	{
		return computeHash(algorithm, stringToByteArray(value));
	}

	public static String computeHash(final Algorithm algorithm, final byte[] bytes)
	{
		try
		{
			final MessageDigest digest = MessageDigest.getInstance(algorithm.toString());
			digest.update(bytes);
			return bytesToHexString(digest.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	// Encryption
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
	private static final SecretKey secret = readSecret();
	private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

	private static SecretKey readSecret()
	{
		try
		{
			return new SecretKeySpec(MessageDigest.getInstance(SHA_256.toString())
				.digest(FileSystem.readFileToBytes("secret")), "AES");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String encrypt(final String s)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			return Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static String decrypt(final String s)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			return new String(cipher.doFinal(Base64.getDecoder().decode(s)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
}

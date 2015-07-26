package com.sdd.jborg.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileSystem
{
	public static String readFileToString(final String path)
	{
		return new String(readFileToBytes(path), StandardCharsets.UTF_8);
	}

	public static byte[] readFileToBytes(final String path)
	{
		try
		{
			final ClassLoader classLoader = FileSystem.class.getClassLoader();
			final File file = new File(classLoader.getResource(path).getFile());
			return Files.readAllBytes(file.toPath());
		}
		catch (Exception e)
		{
			System.err.println("Unable to load " + path + ". " + e.toString());
			System.exit(1);
			return new byte[0];
		}
	}
}

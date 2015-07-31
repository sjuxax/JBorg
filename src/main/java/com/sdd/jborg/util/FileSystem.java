package com.sdd.jborg.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

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

	/**
	 * Write file to local file system.
	 */
	public static void writeStringToFile(final Path path, final String content)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(path.toString());
			writer.write(content);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Delete file from local file system.
	 */
	public static void unlink(final Path path)
	{
		try {
			Files.delete(path);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package com.sdd.jborg.util;

import java.io.File;
import java.nio.file.Files;

public class FileSystem
{
	public static String readFile(final String path)
	{
		try {
			ClassLoader classLoader = FileSystem.class.getClassLoader();
			File file = new File(classLoader.getResource(path).getFile());
			return new String(
				Files.readAllBytes(file.toPath()), "UTF8");
		} catch (Exception e) {
			System.err.println("Unable to load " + path + ". " + e.toString());
			System.exit(1);
			return "";
		}
	}
}

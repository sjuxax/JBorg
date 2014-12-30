package com.sdd;

import org.apache.commons.io.IOUtils;

import static java.nio.charset.StandardCharsets.*;
import static javax.script.ScriptContext.*;

import java.io.*;
import javax.script.*;

public class CoffeeSipper
{
	private final CompiledScript compiledScript;
	private final Bindings bindings;

	public CoffeeSipper() {
		String script = readScript("coffee-script-1.8.0/coffee-script.min.js");

		ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
		try {
			compiledScript = ((Compilable) nashorn).compile(script + "\nCoffeeScript.compile(__source, {bare: true});");
			bindings = nashorn.getBindings(ENGINE_SCOPE);
		} catch (ScriptException e) {
			throw new RuntimeException("Unable to compile script", e);
		}
	}

	private static String readScript(String path) {
		try {
			InputStream input = ClassLoader.getSystemResourceAsStream(path);
			return IOUtils.toString(input, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException("Unable to read " + path, e);
		}
	}

	public synchronized String toJs(String coffee) throws ScriptException {
		bindings.put("__source", coffee);

		return compiledScript.eval(bindings).toString();
	}
}

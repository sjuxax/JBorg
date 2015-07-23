package com.sdd.jborg;

import javax.script.*;

import static javax.script.ScriptContext.*;

import com.sdd.jborg.util.FileSystem;
import com.sdd.jborg.util.JsonObject;

public final class CoffeeScript
{
	private static final CompiledScript compiledScript;
	private static final Bindings bindings;

	static
	{
		final String script = FileSystem.readFile("coffee-script-1.8.0/coffee-script.min.js");
		final ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
		try
		{
			compiledScript = ((Compilable) nashorn).compile(script + "\nCoffeeScript.compile(__source, {bare: true});");
			bindings = nashorn.getBindings(ENGINE_SCOPE);
		}
		catch (ScriptException e)
		{
			throw new RuntimeException("Unable to compile script", e);
		}
	}

	public static String toJs(String coffee)
	{
		bindings.put("__source", coffee);
		try
		{
			return compiledScript.eval(bindings).toString();
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static JsonObject readCsonFileToJsonObject(final String file) {
		final String js = CoffeeScript.toJs(FileSystem.readFile(file));
		return new JsonObject(js.substring(1, js.length()-3));
	}
}

package com.sdd.jborg.util;

import org.json.JSONArray;
import org.json.JSONException;

public class JsonArray
{
	private final JSONArray jsonArray;

	public JsonArray(final String... values)
	{
		this();
		for (final String value : values)
		{
			put(value);
		}
	}

	public JsonArray()
	{
		this.jsonArray = new JSONArray();
	}

	public JsonArray(final JSONArray jsonArray)
	{
		this.jsonArray = jsonArray;
	}

	public String get(final int index)
	{
		try
		{
			return jsonArray.getString(index);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public JsonArray put(final String value)
	{
		jsonArray.put(value);
		return this;
	}

	public JsonArray put(final Object value)
	{
		jsonArray.put(value);
		return this;
	}

	public JsonArray concat(final JsonArray values)
	{
		for (int i = 0; i < values.length(); i++)
		{
			jsonArray.put(values.jsonArray.get(i));
		}
		return this;
	}

	public int length()
	{
		return jsonArray.length();
	}

	@Override
	public String toString()
	{
		return jsonArray.toString();
	}

	public String toString(final int indentFactor)
	{
		return jsonArray.toString(indentFactor);
	}
}
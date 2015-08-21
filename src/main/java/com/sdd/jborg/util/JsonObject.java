package com.sdd.jborg.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonObject
{
	protected final JSONObject jsonObject;

	public JsonObject(final String json)
	{
		JSONObject j = new JSONObject();
		try
		{
			if (json != null)
			{
				j = new JSONObject(json);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		this.jsonObject = j;
	}

	public Iterator<String> keys()
	{
		return jsonObject.keys();
	}

	public JsonObject(final JSONObject jsonObject)
	{
		this.jsonObject = jsonObject;
	}

	public JsonObject()
	{
		jsonObject = new JSONObject();
	}

	public boolean has(final String key)
	{
		return jsonObject.has(key);
	}

	public Object get(final String key)
	{
		try
		{
			return jsonObject.get(key);
		}
		catch (final JSONException ignored)
		{
			return null;
		}
	}

	public boolean getBoolean(final String key)
	{
		return jsonObject.getBoolean(key);
	}

	public String getString(final String key)
	{
		try
		{
			return jsonObject.getString(key);
		}
		catch (JSONException e)
		{
//			e.printStackTrace();
			return null;
		}
	}

	public Number getNumber(final String key)
	{
		try
		{
			return jsonObject.getDouble(key);
		}
		catch (JSONException e)
		{
//			e.printStackTrace();
			return null;
		}
	}

	public Integer getInteger(final String key)
	{
		final Number n = getNumber(key);
		if (n != null)
		{
			return n.intValue();
		}
		return null;
	}

	public JsonObject getObject(final String key)
	{
		try
		{
			return new JsonObject(jsonObject.getJSONObject(key));
		}
		catch (JSONException e)
		{
//			e.printStackTrace();
			final JsonObject o = new JsonObject();
			put(key, o);
			return o;
		}
	}

	public JsonArray getArray(final String key)
	{
		try
		{
			return new JsonArray(jsonObject.getJSONArray(key));
		}
		catch (JSONException e)
		{
//			e.printStackTrace();
			final JsonArray a = new JsonArray();
			jsonObject.put(key, a);
			return a;
		}
	}


	// Setters

	public JsonObject append(final String key, final JsonArray value)
	{
		jsonObject.append(key, value);
		return this;
	}

	public JsonObject put(final String key, final Func0 value)
	{
		try
		{
			jsonObject.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final JsonObject value)
	{
		try
		{
			jsonObject.put(key, value.jsonObject);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final JsonArray value)
	{
		try
		{
			jsonObject.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final String value)
	{
		try
		{
			jsonObject.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final boolean value)
	{
		try
		{
			jsonObject.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final int value)
	{
		try
		{
			jsonObject.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final Object value)
	{
		jsonObject.put(key, value);
		return this;
	}

	public static JsonObject merge(final JsonObject... objects)
	{
		final JsonObject last = new JsonObject();
		for (int i = 0; i < objects.length; i++)
		{
			final JsonObject o = objects[i];
			final Iterator<String> keyIterator = o.keys();
			while (keyIterator.hasNext())
			{
				final String key = keyIterator.next();
				final Object valueA = last.get(key);
				final Object valueB = o.get(key);
				if (valueA instanceof JSONObject &&
					valueB instanceof JSONObject)
				{
					last.put(key, merge(
						new JsonObject((JSONObject) valueA),
						new JsonObject((JSONObject) valueB)
					));
				}
//				else if (valueA instanceof JsonArray &&
//					valueB instanceof JsonArray)
//				{
//					((JsonArray) valueA).concat((JsonArray) valueB); // concatenate
//				}
//				else if (valueA instanceof JsonArray) {
//					((JsonArray) valueA).put(valueB); // append
//				}
				else
				{
					last.put(key, valueB); // override
				}
			}
		}
		return last;
	}

	@Override
	public String toString()
	{
		return jsonObject.toString();
	}

	public String toString(final int indentFactor)
	{
		return jsonObject.toString(indentFactor);
	}
}

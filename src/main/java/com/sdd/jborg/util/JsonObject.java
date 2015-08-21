package com.sdd.jborg.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonObject
{
	protected final JSONObject jsonObject;

	public JsonObject(final String json) {
		JSONObject j = new JSONObject();
		try {
			if (json != null)
			{
				j = new JSONObject(json);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.jsonObject = j;
	}

	public Iterator<String> keys() {
		return jsonObject.keys();
	}

	public JsonObject(final JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JsonObject() {
		jsonObject = new JSONObject();
	}

	public boolean has(final String key)
	{
		return jsonObject.has(key);
	}

	public Object get(final String key)
	{
		try {
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

	public String getString(final String key) {
		try {
			return jsonObject.getString(key);
		} catch (JSONException e) {
//			e.printStackTrace();
			return null;
		}
	}

	public Number getNumber(final String key) {
		try {
			return jsonObject.getDouble(key);
		} catch (JSONException e) {
//			e.printStackTrace();
			return null;
		}
	}

	public Integer getInteger(final String key) {
		final Number n = getNumber(key);
		if (n != null) {
			return n.intValue();
		}
		return null;
	}

	public JsonObject getObject(final String key) {
		try {
			return new JsonObject(jsonObject.getJSONObject(key));
		} catch (JSONException e) {
//			e.printStackTrace();
			final JsonObject o = new JsonObject();
			put(key, o);
			return o;
		}
	}

	public JsonArray getArray(final String key) {
		try {
			return new JsonArray(jsonObject.getJSONArray(key));
		} catch (JSONException e) {
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

	public JsonObject put(final String key, final Func0 value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final JsonObject value) {
		try {
			jsonObject.put(key, value.jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final JsonArray value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final String value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final boolean value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final int value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject put(final String key, final Object value) {
		jsonObject.put(key, value);
		return this;
	}

	public static void merge(final JsonObject ... objects)
	{
		final JsonObject last = objects[0];
		for (int i=1; i<objects.length; i++)
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
					merge(
						new JsonObject((JSONObject) valueA),
						new JsonObject((JSONObject) valueB)
					);
				}
//				else if (valueA instanceof JsonArray &&
//					valueB instanceof JsonArray)
//				{
//					((JsonArray) valueA).concat((JsonArray) valueB); // concatenate
//				}
//				else if (valueA instanceof JsonArray) {
//					((JsonArray) valueA).put(valueB); // append
//				}
				else {
					last.put(key, valueB); // override
				}
			}
		}
	}

	public static class JsonArray {
		private final JSONArray jsonArray;

		public JsonArray(final String ... values)
		{
			this();
			for (final String value : values)
			{
				put(value);
			}
		}

		public JsonArray() {
			this.jsonArray = new JSONArray();
		}

		public JsonArray(final JSONArray jsonArray) {
			this.jsonArray = jsonArray;
		}

		public String get(final int index) {
			try {
				return jsonArray.getString(index);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		public JsonArray put(final String value) {
			jsonArray.put(value);
			return this;
		}

		public JsonArray put(final Object value) {
			jsonArray.put(value);
			return this;
		}

		public JsonArray concat(final JsonArray values)
		{
			for (int i=0; i<values.length(); i++)
			{
				jsonArray.put(values.jsonArray.get(i));
			}
			return this;
		}

		public int length() {
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

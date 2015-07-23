package com.sdd.jborg.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonObject
{
	private final JSONObject jsonObject;

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

	public JsonObject(final JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JsonObject() {
		this.jsonObject = new JSONObject();
	}

	public String getString(final String key) {
		try {
			return jsonObject.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Number getNumber(final String key) {
		try {
			return jsonObject.getDouble(key);
		} catch (JSONException e) {
			e.printStackTrace();
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
			e.printStackTrace();
			return null;
		}
	}

	public JsonArray getArray(final String key) {
		try {
			return new JsonArray(jsonObject.getJSONArray(key));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public JsonObject putString(final String key, final String value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JsonObject putNumber(final String key, final int value) {
		try {
			jsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public static class JsonArray {
		private final JSONArray jsonArray;

		public JsonArray() {
			this.jsonArray = new JSONArray();
		}

		public JsonArray(final String json) {
			JSONArray j;
			try {
				j = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
				j = new JSONArray();
			}
			this.jsonArray = j;
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

		public int length() {
			return jsonArray.length();
		}
	}
}

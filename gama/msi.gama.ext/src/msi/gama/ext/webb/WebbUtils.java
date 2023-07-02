/*******************************************************************************************************
 *
 * WebbUtils.java, in msi.gama.ext, is part of the source code of the GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.ext.webb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Static utility method and tools for HTTP traffic parsing and encoding.
 *
 * @author hgoebl
 */
public class WebbUtils {

	/**
	 * Instantiates a new webb utils.
	 */
	protected WebbUtils() {}

	/**
	 * Convert a Map to a query string.
	 *
	 * @param values
	 *            the map with the values <code>null</code> will be encoded as empty string, all other objects are
	 *            converted to String by calling its <code>toString()</code> method.
	 * @return e.g. "key1=value&amp;key2=&amp;email=max%40example.com"
	 */
	public static String queryString(final Map<String, Object> values) {
		final StringBuilder sbuf = new StringBuilder();
		String separator = "";

		for (final Map.Entry<String, Object> entry : values.entrySet()) {
			final Object entryValue = entry.getValue();
			if (entryValue instanceof Object[]) {
				for (final Object value : (Object[]) entryValue) {
					appendParam(sbuf, separator, entry.getKey(), value);
					separator = "&";
				}
			} else if (entryValue instanceof Iterable) {
				for (final Object multiValue : (Iterable<?>) entryValue) {
					appendParam(sbuf, separator, entry.getKey(), multiValue);
					separator = "&";
				}
			} else {
				appendParam(sbuf, separator, entry.getKey(), entryValue);
				separator = "&";
			}
		}

		return sbuf.toString();
	}

	/**
	 * Append param.
	 *
	 * @param sbuf
	 *            the sbuf
	 * @param separator
	 *            the separator
	 * @param entryKey
	 *            the entry key
	 * @param value
	 *            the value
	 */
	private static void appendParam(final StringBuilder sbuf, final String separator, final String entryKey,
			final Object value) {
		final String sValue = value == null ? "" : String.valueOf(value);
		sbuf.append(separator);
		sbuf.append(urlEncode(entryKey));
		sbuf.append('=');
		sbuf.append(urlEncode(sValue));
	}

	/**
	 * Convert a byte array to a JSONObject.
	 *
	 * @param bytes
	 *            a UTF-8 encoded string representing a JSON object.
	 * @return the parsed object
	 * @throws WebbException
	 *             in case of error (usually a parsing error due to invalid JSON)
	 */
	// public static JSONObject toJsonObject(final byte[] bytes) {
	// String json;
	// try {
	// json = new String(bytes, Const.UTF8);
	// new JSONObject().
	// return new JSONObject(json);
	// } catch (final UnsupportedEncodingException e) {
	// throw new WebbException(e);
	// } catch (final JSONException e) {
	// throw new WebbException("payload is not a valid JSON object", e);
	// }
	// }

	/**
	 * Convert a byte array to a JSONArray.
	 *
	 * @param bytes
	 *            a UTF-8 encoded string representing a JSON array.
	 * @return the parsed JSON array
	 * @throws WebbException
	 *             in case of error (usually a parsing error due to invalid JSON)
	 */
	// public static JSONArray toJsonArray(final byte[] bytes) {
	// String json;
	// try {
	// json = new String(bytes, Const.UTF8);
	// return new JSONArray(json);
	// } catch (final UnsupportedEncodingException e) {
	// throw new WebbException(e);
	// } catch (final JSONException e) {
	// throw new WebbException("payload is not a valid JSON array", e);
	// }
	// }

	/**
	 * Read an <code>InputStream</code> into <code>byte[]</code> until EOF. <br>
	 * Does not close the InputStream!
	 *
	 * @param is
	 *            the stream to read the bytes from
	 * @return all read bytes as an array
	 * @throws IOException
	 *             when read or write operation fails
	 */
	public static byte[] readBytes(final InputStream is) throws IOException {
		if (is == null) return null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copyStream(is, baos);
		return baos.toByteArray();
	}

	/**
	 * Copy complete content of <code>InputStream</code> to <code>OutputStream</code> until EOF. <br>
	 * Does not close the InputStream nor OutputStream!
	 *
	 * @param input
	 *            the stream to read the bytes from
	 * @param output
	 *            the stream to write the bytes to
	 * @throws IOException
	 *             when read or write operation fails
	 */
	public static void copyStream(final InputStream input, final OutputStream output) throws IOException {
		final byte[] buffer = new byte[1024];
		int count;
		while ((count = input.read(buffer)) != -1) { output.write(buffer, 0, count); }
	}

	/**
	 * Creates a new instance of a <code>DateFormat</code> for RFC1123 compliant dates. <br>
	 * Should be stored for later use but be aware that this DateFormat is not Thread-safe! <br>
	 * If you have to deal with dates in this format with JavaScript, it's easy, because the JavaScript Date object has
	 * a constructor for strings formatted this way.
	 *
	 * @return a new instance
	 */
	public static DateFormat getRfc1123DateFormat() {
		final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		format.setLenient(false);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}

	/**
	 * Url encode.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 */
	static String urlEncode(final String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			return value;
		}
	}

	/**
	 * Adds the request properties.
	 *
	 * @param connection
	 *            the connection
	 * @param map
	 *            the map
	 */
	static void addRequestProperties(final HttpURLConnection connection, final Map<String, Object> map) {
		if (map == null || map.isEmpty()) return;
		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			addRequestProperty(connection, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds the request property.
	 *
	 * @param connection
	 *            the connection
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	static void addRequestProperty(final HttpURLConnection connection, final String name, final Object value) {
		if (name == null || name.length() == 0 || value == null)
			throw new IllegalArgumentException("name and value must not be empty");

		String valueAsString;
		if (value instanceof Date) {
			valueAsString = getRfc1123DateFormat().format((Date) value);
		} else if (value instanceof Calendar) {
			valueAsString = getRfc1123DateFormat().format(((Calendar) value).getTime());
		} else {
			valueAsString = value.toString();
		}

		connection.addRequestProperty(name, valueAsString);
	}

	/**
	 * Ensure request property.
	 *
	 * @param connection
	 *            the connection
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	static void ensureRequestProperty(final HttpURLConnection connection, final String name, final Object value) {
		if (!connection.getRequestProperties().containsKey(name)) { addRequestProperty(connection, name, value); }
	}

	/**
	 * Gets the payload as bytes and set content type.
	 *
	 * @param connection
	 *            the connection
	 * @param request
	 *            the request
	 * @param compress
	 *            the compress
	 * @param jsonIndentFactor
	 *            the json indent factor
	 * @return the payload as bytes and set content type
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	static byte[] getPayloadAsBytesAndSetContentType(final HttpURLConnection connection, final Request request,
			final boolean compress, final int jsonIndentFactor) throws UnsupportedEncodingException {

		byte[] requestBody = null;
		String bodyStr = null;

		if (request.params != null) {
			WebbUtils.ensureRequestProperty(connection, Webb.HDR_CONTENT_TYPE, Webb.APP_FORM);
			bodyStr = WebbUtils.queryString(request.params);
		} else if (request.payload == null)
			return null;
		else if (request.payload instanceof byte[]) {
			WebbUtils.ensureRequestProperty(connection, Webb.HDR_CONTENT_TYPE, Webb.APP_BINARY);
			requestBody = (byte[]) request.payload;
		} else {
			WebbUtils.ensureRequestProperty(connection, Webb.HDR_CONTENT_TYPE, Webb.TEXT_PLAIN);
			bodyStr = request.payload.toString();
		}
		if (bodyStr != null) { requestBody = bodyStr.getBytes(Webb.UTF8); }

		if (requestBody == null) throw new IllegalStateException();

		// only compress if the new body is smaller than uncompressed body
		if (compress && requestBody.length > Webb.MIN_COMPRESSED_ADVANTAGE) {
			final byte[] compressedBody = gzip(requestBody);
			if (requestBody.length - compressedBody.length > Webb.MIN_COMPRESSED_ADVANTAGE) {
				requestBody = compressedBody;
				connection.setRequestProperty(Webb.HDR_CONTENT_ENCODING, "gzip");
			}
		}

		connection.setFixedLengthStreamingMode(requestBody.length);

		return requestBody;
	}

	/**
	 * Sets the content type and length for streaming.
	 *
	 * @param connection
	 *            the connection
	 * @param request
	 *            the request
	 * @param compress
	 *            the compress
	 */
	static void setContentTypeAndLengthForStreaming(final HttpURLConnection connection, final Request request,
			final boolean compress) {

		long length;

		if (request.payload instanceof File) {
			length = compress ? -1L : ((File) request.payload).length();
		} else if (request.payload instanceof InputStream) {
			length = -1L;
		} else
			throw new IllegalStateException();

		if (length > Integer.MAX_VALUE) {
			length = -1L; // use chunked streaming mode
		}

		WebbUtils.ensureRequestProperty(connection, Webb.HDR_CONTENT_TYPE, Webb.APP_BINARY);
		if (length < 0) {
			connection.setChunkedStreamingMode(-1); // use default chunk size
			if (compress) { connection.setRequestProperty(Webb.HDR_CONTENT_ENCODING, "gzip"); }
		} else {
			connection.setFixedLengthStreamingMode((int) length);
		}
	}

	/**
	 * Gzip.
	 *
	 * @param input
	 *            the input
	 * @return the byte[]
	 */
	static byte[] gzip(final byte[] input) {
		try (final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
				GZIPOutputStream gzipOS = new GZIPOutputStream(byteArrayOS);) {
			gzipOS.write(input);
			gzipOS.flush();
			return byteArrayOS.toByteArray();
		} catch (final Exception e) {
			throw new WebbException(e);
		}
	}

	/**
	 * Wrap stream.
	 *
	 * @param contentEncoding
	 *            the content encoding
	 * @param inputStream
	 *            the input stream
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	static InputStream wrapStream(final String contentEncoding, final InputStream inputStream) throws IOException {
		if (contentEncoding == null || "identity".equalsIgnoreCase(contentEncoding)) return inputStream;
		if ("gzip".equalsIgnoreCase(contentEncoding)) return new GZIPInputStream(inputStream);
		if ("deflate".equalsIgnoreCase(contentEncoding))
			return new InflaterInputStream(inputStream, new Inflater(false), 512);
		throw new WebbException("unsupported content-encoding: " + contentEncoding);
	}

	/**
	 * Parses the response body.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param response
	 *            the response
	 * @param responseBodyStream
	 *            the response body stream
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	static <T> void parseResponseBody(final Class<T> clazz, final Response<T> response,
			final InputStream responseBodyStream) throws UnsupportedEncodingException, IOException {

		if (responseBodyStream == null || clazz == Void.class) return;
		if (clazz == InputStream.class) {
			response.setBody(responseBodyStream);
			return;
		}

		final byte[] responseBody = WebbUtils.readBytes(responseBodyStream);
		// we are ignoring headers describing the content type of the response, instead
		// try to force the content based on the type the client is expecting it (clazz)
		if (clazz == String.class) {
			response.setBody(new String(responseBody, Webb.UTF8));
		} else if (clazz == Webb.BYTE_ARRAY_CLASS) { response.setBody(responseBody); }
		// else if (clazz == JSONObject.class) {
		// response.setBody(WebbUtils.toJsonObject(responseBody));
		// } else if (clazz == JSONArray.class) {
		// response.setBody(WebbUtils.toJsonArray(responseBody));
		// }

	}

	/**
	 * Parses the error response.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param response
	 *            the response
	 * @param responseBodyStream
	 *            the response body stream
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	static <T> void parseErrorResponse(final Class<T> clazz, final Response<T> response,
			final InputStream responseBodyStream) throws UnsupportedEncodingException, IOException {

		if (responseBodyStream == null) return;
		if (clazz == InputStream.class) {
			response.errorBody = responseBodyStream;
			return;
		}

		final byte[] responseBody = WebbUtils.readBytes(responseBodyStream);
		final String contentType = response.connection.getContentType();
		if (contentType == null || contentType.startsWith(Webb.APP_BINARY) || clazz == Webb.BYTE_ARRAY_CLASS) {
			response.errorBody = responseBody;
			return;
		}

		// fallback to String if bytes are valid UTF-8 characters ...
		try {
			response.errorBody = new String(responseBody, Webb.UTF8);
			return;
		} catch (final Exception ignored) {
			// ignored - was just a try!
		}

		// last fallback - return error object as byte[]
		response.errorBody = responseBody;
	}
}

package org.ambraproject.wombat.service.remote;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static java.lang.String.format;

/**
 * Created by jkrzemien on 7/15/14.
 */

public class TestUtils {

    final static String TEST_URL = "http://localhost";

    public static HttpGet buildExpectedURI(String anything) {
        try {
            return new HttpGet(new URI(format("%s/%s", TEST_URL, anything)));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static HttpGet buildExpectedRepoURI(String bucket, String key, String version) {
        String address = format("%s/repo/%s/%s/%s", TEST_URL, bucket, key, version);
        try {
            return new HttpGet(new URI(address));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String inputStream2String(InputStream inputStream) throws IOException {
        return inputStream2String(inputStream, Charset.forName("UTF-8"));
    }

    public static String reader2String(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int charsRead = -1;
        char[] chars = new char[128];
        do {
            charsRead = reader.read(chars, 0, chars.length);
            //if we have valid chars, append them to end of string.
            if (charsRead > 0)
                builder.append(chars, 0, charsRead);
        } while (charsRead > 0);
        return builder.toString();
    }

    public static String inputStream2String(InputStream inputStream, Charset encoding) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, encoding);
        return writer.toString();
    }

    public static InputStream string2InputStream(String string) throws IOException {
        return new ByteArrayInputStream(string.getBytes());
    }
}

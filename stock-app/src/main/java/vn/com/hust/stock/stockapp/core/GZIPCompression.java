package vn.com.hust.stock.stockapp.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZIPCompression {
    private static final String UTF_8 = "UTF_8";

    public static byte[] compress(final String stringToCompress) {
        if (stringToCompress ==null || stringToCompress.length() == 0) {
            return null;
        }

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOutput = new GZIPOutputStream(baos)) {
            gzipOutput.write(stringToCompress.getBytes(UTF_8));
            gzipOutput.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while compression!", e);
        }
    }

//    public static String decompress(final byte[] compressed) {
//        if (compressed ==null || compressed.length == 0) {
//            return null;
//        }
//
//        try (final GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(compressed));
//             final StringWriter stringWriter = new StringWriter()) {
//            IOUtils.copy(gzipInput, stringWriter);
//            return stringWriter.toString();
//        } catch (IOException e) {
//            throw new UncheckedIOException("Error while decompression!", e);
//        }
//    }
}

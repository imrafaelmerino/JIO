package jio.http.client;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

public final class Utils {

    /**
     * Creates a Content-Type header value for a multipart/form-data request with the specified boundary.
     *
     * @param boundary The boundary string used to separate different parts of the multipart request.
     * @return A string representing the Content-Type header value.
     * @throws NullPointerException If the provided boundary is null.
     */
    public static String createMultipartFormContentTypeHeader(final String boundary) {
        return String.format("multipart/form-data; boundary=%s",
                             Objects.requireNonNull(boundary)
                            );
    }

    /**
     * Create a body encoded as multipart/form-data from a form. It's important to send the
     * specified boundary in the Content-Type header (Content-Type: multipart/form-data; boundary={{boundary}})
     *
     * @param fields   A map of fields and their values.
     * @param files    A map of file names and their content as File objects.
     * @param boundary The boundary string used to separate different parts of the multipart request.
     * @return A String representing the request body with fields and files encoded in multipart/form-data format.
     * @throws UncheckedIOException If an IO exception occurs while reading file contents.
     * @see #createMultipartFormContentTypeHeader(String)
     */
    public static String createMultipartFormBody(final Map<String, String> fields,
                                                 final Map<String, File> files,
                                                 final String boundary
                                                ) throws UncheckedIOException {
        try {

            StringBuilder builder = new StringBuilder();

            // Add fields to body
            for (Map.Entry<String, String> field : Objects.requireNonNull(fields).entrySet()) {
                builder.append("--").append(Objects.requireNonNull(boundary)).append("\r\n")
                       .append("Content-Disposition: form-data; name=\"").append(field.getKey()).append("\"\r\n")
                       .append("\r\n")
                       .append(field.getValue()).append("\r\n");
            }

            // Add files to body (content type not added)
            for (Map.Entry<String, File> file : Objects.requireNonNull(files).entrySet()) {
                builder.append("--").append(boundary).append("\r\n")
                       .append("Content-Disposition: form-data; name=\"").append(file.getKey()).append("\"; filename=\"")
                       .append(file.getValue().getName()).append("\"\r\n")
                       .append("\r\n")
                       .append(new String(Files.readAllBytes(file.getValue().toPath()))).append("\r\n");
            }

            builder.append("--")
                   .append(boundary)
                   .append("--");

            return builder.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
package jio.http.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The `MultipartForm` class provides utility methods for creating HTTP requests with multipart/form-data content type.
 * This allows you to create a Content-Type header and request body for sending data, including fields and files, using
 * the multipart/form-data encoding.
 * <p>
 * Use the methods in this class to create the appropriate headers and request body for your HTTP requests when you need
 * to send data as multipart form data.
 */
public final class MultipartForm {

  private MultipartForm() {
  }

  /**
   * Creates a Content-Type header output for a multipart/form-data request with the specified boundary.
   *
   * @param boundary The boundary string used to separate different parts of the multipart request.
   * @return A string representing the Content-Type header output.
   * @throws NullPointerException If the provided boundary is null.
   */
  public static String createContentTypeHeader(final String boundary) {
    return String.format("multipart/form-data; boundary=%s",
                         Objects.requireNonNull(boundary)
                        );
  }

  public static boolean isValidBoundary(String boundary) {
    if (boundary.isEmpty()) {
      return false;
    }
    if (!isAscii(boundary)) {
      return false;
    }

    if (boundary.length() > 70) {
      return false;
    }

    return true;
  }

  private static boolean isAscii(String str) {
    return StandardCharsets.US_ASCII.newEncoder()
                                    .canEncode(str);
  }

  /**
   * Create a body encoded as multipart/form-data from a form. It's important to send the specified boundary in the
   * Content-Type header (Content-Type: multipart/form-data; boundary={{boundary}}).
   *
   * @param fields   A map of fields and their values.
   * @param files    A map of file names and their content as File objects.
   * @param boundary The boundary string used to separate different parts of the multipart request.
   * @return A String representing the request body with fields and files encoded in multipart/form-data format.
   * @throws UncheckedIOException If an IO exception occurs while reading file contents.
   * @see #createContentTypeHeader(String)
   */
  public static List<byte[]> createByteBody(final Map<String, String> fields,
                                            final Map<String, Path> files,
                                            final String boundary
                                           ) throws UncheckedIOException {
    Objects.requireNonNull(fields);
    Objects.requireNonNull(files);
    Objects.requireNonNull(boundary);

    if (!isValidBoundary(boundary)) {
      throw new IllegalArgumentException("The boundary should not exceed 70 bytes in length and must consist of ASCII"
                                         + " characters.");
    }

    var byteArrays = new ArrayList<byte[]>();

    byte[] separator =
        "--%s\r\nContent-Disposition: form-data; name=".formatted(boundary)
                                                       .getBytes(StandardCharsets.UTF_8);

    for (var entry : fields.entrySet()) {
      byteArrays.add(separator);

      byteArrays.add("\"%s\"\r\n\r\n%s\r\n".formatted(entry.getKey(),
                                                      entry.getValue())
                                           .getBytes(StandardCharsets.UTF_8));

    }

    try {
      for (var entry : files.entrySet()) {
        byteArrays.add(separator);
        Path path = entry.getValue();
        String mimeType = Files.probeContentType(path);
        byteArrays.add("\"%s\"; filename=\"%s\"\r\nContent-Type: %s\r\n\r\n".formatted(entry.getKey(),
                                                                                       path.getFileName(),
                                                                                       mimeType)
                                                                            .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(path));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    byteArrays.add("--%s--".formatted(boundary)
                           .getBytes(StandardCharsets.UTF_8));

    return byteArrays;
  }


  /**
   * Create a body encoded as multipart/form-data from a form. It's important to send the specified boundary in the
   * Content-Type header (Content-Type: multipart/form-data; boundary={{boundary}}). Use this method when sending * text
   * files, otherwise use {@link #createByteBody(Map, Map, String)}
   *
   * @param fields   A map of fields and their values.
   * @param files    A map of file names and their content as File objects.
   * @param boundary The boundary string used to separate different parts of the multipart request.
   * @return A String representing the request body with fields and files encoded in multipart/form-data format.
   * @throws UncheckedIOException If an IO exception occurs while reading file contents.
   * @see #createContentTypeHeader(String)
   */
  public static String createBody(final Map<String, String> fields,
                                  final Map<String, Path> files,
                                  final String boundary
                                 ) throws UncheckedIOException {
    try {
      Objects.requireNonNull(fields);
      Objects.requireNonNull(files);
      Objects.requireNonNull(boundary);

      if (!isValidBoundary(boundary)) {
        throw new IllegalArgumentException("The boundary should not exceed 70 bytes in length and must consist of ASCII"
                                           + " characters.");
      }

      StringBuilder builder = new StringBuilder();

      // Add fields to body
      for (Map.Entry<String, String> field : Objects.requireNonNull(fields)
                                                    .entrySet()) {
        builder.append("--")
               .append(Objects.requireNonNull(boundary))
               .append("\r\n")
               .append("Content-Disposition: form-data; name=\"")
               .append(field.getKey())
               .append("\"\r\n")
               .append("\r\n")
               .append(field.getValue())
               .append("\r\n");
      }

      // Add files to body (content type not added)
      for (var entry : Objects.requireNonNull(files)
                              .entrySet()) {
        Path file = entry.getValue();
        builder.append("--")
               .append(boundary)
               .append("\r\n")
               .append("Content-Disposition: form-data; name=\"")
               .append(entry.getKey())
               .append("\"; filename=\"")
               .append(file.getFileName())
               .append("\"\r\n")
               .append("Content-Type: ")
               .append(Files.probeContentType(file))
               .append("\r\n\n")
               .append(Files.readString(file))
               .append("\r\n");
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




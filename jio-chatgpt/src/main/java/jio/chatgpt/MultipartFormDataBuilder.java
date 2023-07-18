package jio.chatgpt;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * Builder to create bodies from forms encoding the user input data according to the media type multiform-data
 */
public class MultipartFormDataBuilder {

    /**
     * Create a body encoded as multiform-data from a form. It's important to send the
     * specified boundary in the Content-Type header:
     * Content-Type: multipart/form-data; boundary={{boundary}}
     *
     * @param fields   map of fields and their values
     * @param files    map of file names and their content
     * @param boundary the boundary.
     * @return a body with the fields and files encoded as multiform-data format
     */
    public static String build(Map<String, String> fields,
                               Map<String, File> files,
                               String boundary) {
        try {

            StringBuilder builder = new StringBuilder();

            // Add fields to body
            for (Map.Entry<String, String> field : fields.entrySet()) {
                builder.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"").append(field.getKey()).append("\"\r\n")
                        .append("\r\n")
                        .append(field.getValue()).append("\r\n");
            }

            // Add files to body (content type not added)
            for (Map.Entry<String, File> file : files.entrySet()) {
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

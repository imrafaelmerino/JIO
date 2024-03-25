package jio.chatgpt;

import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jio.http.client.Utils;
import jsonvalues.JsObj;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service for managing files, including uploading, deleting, retrieving, and listing files.
 */
public final class FileService extends AbstractService {
    /**
     * Uploads a file with the given purpose.
     *
     * @param file    The file to upload.
     * @param purpose The purpose of the file.
     * @return An IO (monadic) object representing the asynchronous result of the file upload request.
     */
    public final BiLambda<File, String, JsObj> upload;
    /**
     * Deletes a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to delete.
     * @return An IO (monadic) object representing the asynchronous result of the file deletion request.
     */
    public final Lambda<String, JsObj> delete;
    /**
     * Retrieves information about a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to retrieve.
     * @return An IO (monadic) object representing the asynchronous result of the file retrieval request.
     */
    public final Lambda<String, JsObj> retrieve;

    /**
     * Retrieves the content of a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to retrieve content from.
     * @return An IO (monadic) object representing the asynchronous result of the file content retrieval request.
     */
    public final Lambda<String, JsObj> retrieveFileContent;
    /**
     * Lists all available files.
     *
     * @return An IO (monadic) object representing the asynchronous result of the file listing request.
     */
    public final Supplier<IO<JsObj>> list = () -> get(uri);

    /**
     * Creates a FileService instance with the specified HTTP client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making requests.
     * @param builder       The configuration builder for this service.
     */
    FileService(JioHttpClientBuilder clientBuilder,
                ConfBuilder builder
               ) {
        super(clientBuilder,
              builder,
              "files");
        upload = (file, purpose) -> {
            String boundary = UUID.randomUUID().toString();
            return post(uri,
                        Utils.createMultipartFormBody(Map.of("purpose", purpose),
                                                      Map.of("file", file),
                                                      boundary
                                                     ),
                        Utils.createMultipartFormContentTypeHeader(boundary)
                       );
        };

        delete = id -> delete(uri.resolve("/" + id));
        retrieve = id -> get(uri.resolve("/" + id));
        retrieveFileContent = id -> get(uri.resolve("/" + id + "/" + "content"));
    }


}

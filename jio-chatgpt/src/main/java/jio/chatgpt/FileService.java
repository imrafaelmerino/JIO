package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jio.http.client.Utils;
import jsonvalues.JsObj;


import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing files, including uploading, deleting, retrieving, and listing files.
 */
public final class FileService extends AbstractService {
    /**
     * Creates a FileService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    FileService(MyHttpClient client, ConfBuilder builder) {
        super(client,
              builder,
              "files");
    }

    /**
     * Uploads a file with the given purpose.
     *
     * @param file    The file to upload.
     * @param purpose The purpose of the file.
     * @return An IO (monadic) object representing the asynchronous result of the file upload request.
     */
    public IO<JsObj> upload(File file,
                            String purpose
                           ) {
        String boundary = UUID.randomUUID().toString();
        return post(uri,
                    Utils.createMultipartFormBody(Map.of("purpose", purpose),
                                                  Map.of("file", file),
                                                  boundary
                                                 ),
                    Utils.createMultipartFormContentTypeHeader(boundary)
                   );
    }

    /**
     * Deletes a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to delete.
     * @return An IO (monadic) object representing the asynchronous result of the file deletion request.
     */
    public IO<JsObj> delete(String fileId) {
        return delete(uri.resolve("/" + fileId));
    }

    /**
     * Retrieves information about a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to retrieve.
     * @return An IO (monadic) object representing the asynchronous result of the file retrieval request.
     */
    public IO<JsObj> retrieve(String fileId) {
        return get(uri.resolve("/" + fileId));
    }

    /**
     * Retrieves the content of a file with the specified fileId.
     *
     * @param fileId The unique identifier of the file to retrieve content from.
     * @return An IO (monadic) object representing the asynchronous result of the file content retrieval request.
     */
    public IO<JsObj> retrieveFileContent(String fileId) {
        return get(uri.resolve("/" + fileId + "/" + "content"));
    }

    /**
     * Lists all available files.
     *
     * @return An IO (monadic) object representing the asynchronous result of the file listing request.
     */
    public IO<JsObj> list() {
        return get(uri);
    }


}

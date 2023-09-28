package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


import java.io.File;
import java.util.Map;
import java.util.UUID;

public class FileService extends AbstractService {

    public FileService(MyHttpClient client, ConfBuilder builder) {
        super(client,
              builder,
              "files");
    }

    public IO<JsObj> upload(File file,
                            String purpose
                           ) {
        String boundary = UUID.randomUUID().toString();
        return post(uri,
                    MultipartFormDataBuilder.build(Map.of("purpose", purpose),
                                                   Map.of("file", file),
                                                   boundary
                                                  ),
                    "multipart/form-data; boundary=" + boundary
                   );
    }

    public IO<JsObj> delete(String fileId) {
        return delete(uri.resolve("/" + fileId));
    }

    public IO<JsObj> retrieve(String fileId) {
        return get(uri.resolve("/" + fileId));
    }

    public IO<JsObj> retrieveFileContent(String fileId) {
        return get(uri.resolve("/" + fileId + "/" + "content"));
    }

    public IO<JsObj> list() {
        return get(uri);
    }


}

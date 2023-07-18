package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;



public class FineTunerService extends AbstractService {

    public FineTunerService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "fine-tunes");
    }


    /**
     * Creates a job that fine-tunes a specified model from a given dataset.
     * <p>
     * Response includes details of the enqueued job including job status and the name of the fine-tuned models once complete
     *
     * @param builder
     * @return
     */
    public IO<JsObj> create(FineTuneBuilder builder) {

        return post(uri, builder.build());
    }

    public IO<JsObj> list() {
        return get(uri);
    }

    public IO<JsObj> get(String id) {
        return get(uri.resolve("/" + id));
    }

    /**
     * Immediately cancel a fine-tune job.
     *
     * @param id
     * @return
     */
    public IO<JsObj> cancel(String id) {
        return post(uri.resolve("/" + id + "/cancel"));
    }

    public IO<JsObj> listEvents(String id) {

        return get(uri.resolve("/" + id + "/events"));
    }

    public IO<JsObj> delete(String model) {
        return delete(uri.resolve("/" + model));
    }


}

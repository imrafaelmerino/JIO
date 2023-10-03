package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


/**
 * Service for fine-tuning models using a specified dataset.
 */
public final class FineTunerService extends AbstractService {
    /**
     * Creates a FineTunerService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    FineTunerService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "fine-tunes");
    }


    /**
     * Creates a job that fine-tunes a specified model from a given dataset.
     * <p>
     * Response includes details of the enqueued job, including job status and the name of the fine-tuned model once
     * complete.
     *
     * @param builder The fine-tune builder containing configuration details for the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the fine-tuning job creation request.
     */
    public IO<JsObj> create(FineTuneBuilder builder) {

        return post(uri, builder.build());
    }

    /**
     * Lists all fine-tuning jobs.
     *
     * @return An IO (monadic) object representing the asynchronous result of the job listing request.
     */
    public IO<JsObj> list() {
        return get(uri);
    }

    /**
     * Gets details of a specific fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the job details retrieval request.
     */
    public IO<JsObj> get(String id) {
        return get(uri.resolve("/" + id));
    }

    /**
     * Immediately cancels a fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job to cancel.
     * @return An IO (monadic) object representing the asynchronous result of the job cancellation request.
     */
    public IO<JsObj> cancel(String id) {
        return post(uri.resolve("/" + id + "/cancel"));
    }

    /**
     * Lists events associated with a fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the event listing request.
     */
    public IO<JsObj> listEvents(String id) {

        return get(uri.resolve("/" + id + "/events"));
    }

    /**
     * Deletes a fine-tuned model by its name.
     *
     * @param model The name of the fine-tuned model to delete.
     * @return An IO (monadic) object representing the asynchronous result of the model deletion request.
     */
    public IO<JsObj> delete(String model) {
        return delete(uri.resolve("/" + model));
    }


}

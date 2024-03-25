package jio.chatgpt;

import jio.IO;
import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.function.Supplier;


/**
 * Service for fine-tuning models using a specified dataset.
 */
public final class FineTunerService extends AbstractService {
    /**
     * Creates a job that fine-tunes a specified model from a given dataset.
     * <p>
     * Response includes details of the enqueued job, including job status and the name of the fine-tuned model once
     * complete.
     *
     * @param builder The fine-tune builder containing configuration details for the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the fine-tuning job creation request.
     */
    public final Lambda<FineTuneBuilder, JsObj> create;
    /**
     * Gets details of a specific fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the job details retrieval request.
     */
    public final Lambda<String, JsObj> get;
    /**
     * Immediately cancels a fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job to cancel.
     * @return An IO (monadic) object representing the asynchronous result of the job cancellation request.
     */
    public final Lambda<String, JsObj> cancel;
    /**
     * Lists events associated with a fine-tuning job by its ID.
     *
     * @param id The unique identifier of the fine-tuning job.
     * @return An IO (monadic) object representing the asynchronous result of the event listing request.
     */
    public final Lambda<String, JsObj> listEvents;
    /**
     * Deletes a fine-tuned model by its name.
     *
     * @param model The name of the fine-tuned model to delete.
     * @return An IO (monadic) object representing the asynchronous result of the model deletion request.
     */
    public final Lambda<String, JsObj> delete;
    /**
     * Lists all fine-tuning jobs.
     *
     * @return An IO (monadic) object representing the asynchronous result of the job listing request.
     */
    public final Supplier<IO<JsObj>> list;


    /**
     * Creates a FineTunerService instance with the specified HTTP client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making requests.
     * @param builder       The configuration builder for this service.
     */
    FineTunerService(JioHttpClientBuilder clientBuilder,
                     ConfBuilder builder
                    ) {

        super(clientBuilder, builder, "fine-tunes");
        create = b -> post(uri, Objects.requireNonNull(b).build());
        list = () -> get(uri);
        get = id -> get(uri.resolve("/" + id));
        cancel = id -> post(uri.resolve("/" + id + "/cancel"));
        listEvents = id -> get(uri.resolve("/" + id + "/events"));
        delete = model -> delete(uri.resolve("/" + model));
    }


}

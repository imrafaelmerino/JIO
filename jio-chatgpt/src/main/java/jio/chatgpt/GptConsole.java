package jio.chatgpt;

import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.console.*;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsValue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GptConsole {

    public static void main(String[] args) throws IOException {

        if (args.length == 0)
            throw new IllegalArgumentException("Pass the absolute path to the configuration file!");
        String confArg = args[0];

        File confFile = new File(confArg);

        if (!confFile.exists() || !confFile.isFile())
            throw new IllegalArgumentException("%s is not a file".formatted(confArg));

        JsObj confJson = JsObj.parse(Files.readString(confFile.toPath()));
        String authHeader = confJson.getStr("auth_header");

        if (authHeader == null || authHeader.trim().isEmpty())
            throw new IllegalArgumentException(confArg + "auth_header is missing in " + confArg);

        var services = ChatGPTServices.of(ConfBuilder.of(authHeader),
                                          JioHttpClientBuilder.of(HttpClient.newBuilder())
                                         );

        List<Command> commands = new ArrayList<>();

        commands.add(new SupplierCommand("gpt-file-list", "Returns a list of files that belong to the user's organization.",
                                         () -> services.file.list.get().map(JsObj::toString).result()
                     )
                    );
        commands.add(new Command("gpt-file-upload", "Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by one organization can be up to 1 GB. Please contact us if you need to increase the storage limit.") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_PAIR(
                                               new Programs.AskForInputParams("Type the absolute path of the file",
                                                                              path -> path != null && !path.isBlank() && new File(path).exists(),
                                                                              "Introduce a valid path",
                                                                              RetryPolicies.limitRetries(3)
                                               ),
                                               new Programs.AskForInputParams("Type the purpose",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "purpose is re required",
                                                                              RetryPolicies.limitRetries(3)
                                               )
                                                    )
                                       .then(pair -> services.file.upload.apply(new File(pair.first()),
                                                                                pair.second()
                                                                               )
                                                                         .map(JsObj::toString)
                                            );
                    String path = tokens[1];
                    String purpose = tokens[2];
                    File file = new File(path);
                    return !file.exists() || !file.isFile() ?
                            IO.fail(new IllegalArgumentException("%s is not a file".formatted(path))) :
                            services.file.upload.apply(file, purpose).map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-file-delete", "Delete a file") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(
                                               new Programs.AskForInputParams("Type the id of the file",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "Id is required",
                                                                              RetryPolicies.limitRetries(3)
                                               ))
                                       .then(id -> services.file.delete.apply(id).map(JsObj::toString));
                    String id = tokens[1];
                    return services.file.delete.apply(id).map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-file-retrieve", "Returns information about a specific file") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(
                                               new Programs.AskForInputParams("Type the id of the file",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "Id is required",
                                                                              RetryPolicies.limitRetries(3)
                                               ))
                                       .then(id -> services.file.retrieve.apply(id).map(JsObj::toString));
                    String id = tokens[1];
                    return services.file.retrieve.apply(id).map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-file-content", "Returns the contents of the specified file") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(
                                               new Programs.AskForInputParams("Type the id of the file",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "Id is required",
                                                                              RetryPolicies.limitRetries(3)
                                               ))
                                       .then(id -> services.file.retrieveFileContent.apply(id).map(JsObj::toString));
                    String id = tokens[1];
                    return services.file.retrieveFileContent.apply(id).map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-completion", "Creates a completion for the provided prompt and parameters") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUTS(
                                               new Programs.AskForInputParams("Type the model (curie, davinci, babbage,gpt-3.5-turbo)",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "Model is required",
                                                                              RetryPolicies.limitRetries(3)
                                               ),
                                               new Programs.AskForInputParams("Type the prompt",
                                                                              str -> str != null && !str.trim().isBlank(),
                                                                              "Prompt is required",
                                                                              RetryPolicies.limitRetries(3)
                                               ),
                                               new Programs.AskForInputParams("Type the max_tokens",
                                                                              n -> {
                                                                                  try {
                                                                                      int i = Integer.parseInt(n);
                                                                                      return i < 4096 && i > 0;
                                                                                  } catch (NumberFormatException e) {
                                                                                      return false;
                                                                                  }
                                                                              },
                                                                              "max tokens is a number (0, 4096)",
                                                                              RetryPolicies.limitRetries(3)
                                               )
                                                      )
                                       .then(list -> services.completion
                                               .create.apply(CompletionBuilder.of(list.get(0),
                                                                                  list.get(1))
                                                                              .withMaxTokens(Integer.parseInt(list.get(2))))
                                                      .map(JsObj::toString));
                    String model = tokens[1];
                    List<String> promptList = Arrays.stream(tokens).toList().subList(2, tokens.length);
                    String prompt = String.join(" ", promptList);
                    return services.completion.create.apply(CompletionBuilder.of(model,
                                                                                 prompt))
                                                     .map(JsObj::toString);
                };
            }
        });

        commands.add(new SupplierCommand("gpt-models",
                                         "Lists the currently available models, and provides basic information about each one such as the owner and availability.",
                                         () -> services.model.list.get().result().toString()
                     )
                    );


        commands.add(new Command("gpt-finetune-create", "Creates a job that fine-tunes a specified model from a given dataset.\n" +
                                                        "Response includes details of the enqueued job including job status and the name of the fine-tuned models once complete.") {
            @Override
            public Function<String[], IO<String>> apply(JsObj conf, State state) {

                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the file id",
                                                                                     str -> str != null && !str.trim().isBlank(),
                                                                                     "file id is required",
                                                                                     RetryPolicies.limitRetries(3)
                                       ))
                                       .then(id -> services.fineTuner.create.apply(FineTuneBuilder.of(id))
                                                                            .map(JsObj::toString));

                    return services.fineTuner.create.apply(FineTuneBuilder.of(tokens[1]))
                                                    .map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-finetune-get", "Gets info about the fine-tune job") {
            @Override
            public Function<String[], IO<String>> apply(JsObj conf, State state) {

                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the fine-tune job id",
                                                                                     str -> str != null && !str.trim().isBlank(),
                                                                                     "job id is required",
                                                                                     RetryPolicies.limitRetries(3)
                                       ))
                                       .then(id -> services.fineTuner.get.apply(id)
                                                                         .map(JsObj::toString));
                    return services.fineTuner.get.apply(tokens[1])
                                                 .map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-finetune-cancel", "Immediately cancel a fine-tune job.") {
            @Override
            public Function<String[], IO<String>> apply(JsObj conf, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the file id",
                                                                                     str -> str != null && !str.trim().isBlank(),
                                                                                     "file id is required",
                                                                                     RetryPolicies.limitRetries(3)
                                       ))
                                       .then(id -> services.fineTuner.cancel.apply(id)
                                                                            .map(JsObj::toString));
                    return services.fineTuner.cancel.apply(tokens[1])
                                                    .map(JsObj::toString);
                };
            }
        });

        commands.add(new Command("gpt-finetune-events", "Get fine-grained status updates for a fine-tune job") {
            @Override
            public Function<String[], IO<String>> apply(JsObj conf, State state) {
                return tokens -> {
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the file id",
                                                                                     str -> str != null && !str.trim().isBlank(),
                                                                                     "file id is required",
                                                                                     RetryPolicies.limitRetries(3)
                                       ))
                                       .then(id -> services.fineTuner.listEvents.apply(id)
                                                                                .map(JsObj::toString));
                    return services.fineTuner.listEvents.apply(tokens[1])
                                                        .map(JsObj::toString);
                };
            }
        });


        commands.add(new Command("gpt-finetune-list", "List your organization's fine-tuning jobs") {
            @Override
            public Function<String[], IO<String>> apply(JsObj conf, State state) {
                return tokens -> services.fineTuner.list.get()
                                                        .map(JsObj::toString);
            }
        });

        commands.add(new Command("gpt-chat-load-messages", "Load an array of messages from a specified file into the chat conversation") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (!state.listsVariables.containsKey("#chat#"))
                        state.listsVariables.put("#chat#", new ArrayList<>());
                    Lambda<String, String> loadFile = path -> {
                        try {
                            JsArray array = JsArray.parse(Files.readString(Path.of(path)));
                            for (JsValue message : array) {
                                state.listsVariables.get("#chat#").add(message.toString());
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        return IO.succeed("loaded messages from %s".formatted(path));
                    };

                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the absolute path with the chat messages",
                                                                                     str -> str != null && new File(str).exists(),
                                                                                     "File must exist and be an array of messages",
                                                                                     RetryPolicies.limitRetries(3)
                                                      )
                                                     )
                                       .then(loadFile);

                    return loadFile.apply(tokens[1]);

                };
            }
        });

        commands.add(new Command("gpt-chat-add-message", "Append the specified message to the chat conversation") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    if (!state.listsVariables.containsKey("#chat#"))
                        state.listsVariables.put("#chat#", new ArrayList<>());

                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUTS(new Programs.AskForInputParams("Type the role (system, user or assistant)",
                                                                                      str -> str != null && !str.trim().isBlank(),
                                                                                      "role is required",
                                                                                      RetryPolicies.limitRetries(3)
                                                       ),
                                                       new Programs.AskForInputParams("Type the message",
                                                                                      str -> str != null && !str.trim().isBlank(),
                                                                                      "message is required",
                                                                                      RetryPolicies.limitRetries(3)
                                                       )
                                                      )
                                       .then(list -> {
                                                 state.listsVariables.get("#chat#")
                                                                     .add(ChatMessageBuilder.of(Data.ROLE.valueOf(list.get(0)), list.get(1))
                                                                                            .build()
                                                                                            .toString()
                                                                         );
                                                 return IO.succeed("Added a new message to the chat!");
                                             }
                                            );

                    String role = tokens[1];
                    String content = String.join(" ", Arrays.stream(tokens).toList().subList(2, tokens.length));
                    state.listsVariables.get("#chat#")
                                        .add(ChatMessageBuilder.of(Data.ROLE.valueOf(role), content)
                                                               .build()
                                                               .toString()
                                            );
                    return IO.succeed("Added a new message to the chat!");

                };
            }
        });

        commands.add(new Command("gpt-chat-echo", "List all the messages of the chat conversation") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> IO.lazy(() -> {
                    List<String> chat = state.listsVariables.get("#chat#");
                    if (chat == null || chat.isEmpty()) return "chat is empty!";
                    return String.join("\n", chat);
                });
            }
        });


        commands.add(new Command("gpt-chat-clear", "Clear the chat conversation.") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> IO.lazy(() -> {
                    state.listsVariables.get("#chat#").clear();
                    return "Chat cleared!";
                });
            }
        });


        commands.add(new Command("gpt-chat-send", """
                Creates a model response for the given chat conversation.
                Add new chat messages with the command addMessageChat.
                The first message choice of the response is appended to the chat conversation""") {
            @Override
            public Function<String[], IO<String>> apply(JsObj obj, State state) {
                return tokens -> {
                    List<ChatMessageBuilder> messages = state.listsVariables
                            .get("#chat#")
                            .stream()
                            .map(JsObj::parse)
                            .map(a -> ChatMessageBuilder.of(Data.ROLE.valueOf(a.getStr("role")),
                                                            a.getStr("content")))
                            .collect(Collectors.toList());
                    if (tokens.length == 1)
                        return Programs.ASK_FOR_INPUTS(new Programs.AskForInputParams("Type the model (gpt-3.5-turbo) ",
                                                                                      str -> str != null && !str.trim().isBlank(),
                                                                                      "model is required",
                                                                                      RetryPolicies.limitRetries(3)
                                       ))
                                       .then(list -> services.chat.create.apply(ChatBuilder.of(list.get(0), messages))
                                                                         .peekSuccess(resp -> state.listsVariables.get("#chat#")
                                                                                                                  .add(resp.getObj("/choices/0/message")
                                                                                                                           .toString()
                                                                                                                      )
                                                                                     )
                                                                         .map(JsObj::toString));
                    String model = tokens[1];
                    return services.chat.create.apply(ChatBuilder.of(model, messages))
                                               .peekSuccess(resp -> state.listsVariables.get("#chat#")
                                                                                        .add(resp.getObj("/choices/0/message")
                                                                                                 .toString()
                                                                                            )
                                                           )
                                               .map(JsObj::toString);
                };
            }
        });


        Console console = new Console(commands);

        console.eval(confJson);

    }
}

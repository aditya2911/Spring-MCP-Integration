package org.example.springai;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final OllamaChatModel chatModel;

    private final ChatClient chatClient;

    private List<McpAsyncClient> clients;

    private SyncMcpToolCallbackProvider toolCallbackProvider;


    @Autowired
    public ChatController(OllamaChatModel chatModel, ChatClient chatClient) {
        this.chatModel = chatModel;
        this.chatClient = chatClient;
     }

    @GetMapping("/ai/generate")
    public Mono<Map<String, String>> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Mono.fromCallable(() -> this.chatModel.call(message)).subscribeOn(Schedulers.boundedElastic())
                .map(response -> Map.of("generation", response));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

    @GetMapping(value = "/ai/generateMcp")
    public Flux<String> generateMcp(@RequestParam String message) {
        Prompt prompt = new Prompt(new UserMessage(message));

        return chatClient.prompt()
                .user(message)

                .system("You are an helpful AI agent which will perform action on JIRA and confluence using mcp server.")
                 .stream().content();// returns a Flux<ChatResponse>


//      return   Flux.just(ChatClient.create(chatModel).prompt(message)
//              .system("You are an AI agent which will perform action on JIRA and confluence using atlassian-mcp. Call the tool for jira search whenever user provides you a JIRA ID")
//              .tools(new AsyncMcpToolCallbackProvider(clients)).call().content());


    }



}
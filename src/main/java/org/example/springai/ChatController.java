package org.example.springai;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ChatController {

    private final ChatModel chatModel;

    private final ChatClient chatClient;

    private final List<McpSyncClient> clients;


    @Autowired
    public ChatController(ChatModel chatModel, ChatClient chatClient,List<McpSyncClient> clients) {
        this.chatModel = chatModel;
        this.chatClient = chatClient;
        this.clients = clients;
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

    @PostMapping("/api/chat/completions")
    public Mono<Map<String, Object>> chatCompletions(@RequestBody RequestBodyDTO requestBody) {
        return chatClient.prompt()
                .user(requestBody.getMessages().get(requestBody.getMessages().size() - 1).getContent())
                .system("You are a helpful AI agent...")
                .stream()
                .content()
                .collectList()  // collect all streamed chunks
                .map(contents -> String.join("", contents)) // merge into single string
                .map(finalContent -> Map.of(
                        "id", UUID.randomUUID().toString(),
                        "object", "chat.completion",
                        "model", "mistral",
                        "choices", List.of(
                                Map.of("index", 0,
                                        "message", Map.of("role", "assistant",
                                                "content", finalContent),
                                        "finish_reason", "stop")
                        )
                ));
    }






}
package org.example.springai; // IMPORTANT: Ensure this package matches your actual file location

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.List;

@Configuration
public class ChatConfig {


    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout((int) Duration.ofMinutes(10).toMillis());
            factory.setReadTimeout((int) Duration.ofMinutes(10).toMillis());
            restClientBuilder.defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
            restClientBuilder.requestFactory(factory);
        };
    }

    /**
     * Configures the ChatClient to use the provided ChatModel and
     * to register tools obtained specifically from the MCP ToolCallbackProvider.
     * This method correctly extracts the individual ToolCallback instances
     * from the MCP provider and passes them to the ChatClient.
     *
     * @param chatModel The AI chat model (e.g., OllamaChatModel).
     * @param toolCallbackProviders A list of all ToolCallbackProvider beans
     * discovered by Spring.
     * @return A configured ChatClient instance.
     */



//    @Primary
//    @Bean public List<McpAsyncClient> getClients(List<McpAsyncClient> clients){
//        return clients;
//    }
    @Bean
    public ChatClient chatClient(ChatModel chatModel, List<McpSyncClient> clients, ChatClient.Builder chatClientBuilder, List<McpAsyncClient> mcpAsyncClients) {



        // Build the ChatClient, passing the collected list of ToolCallback objects.
        // This should invoke the defaultTools(Collection<ToolCallback>) method,
        // which expects pre-built ToolCallback instances, not objects to be introspected.
        return ChatClient.builder(chatModel)
//                .defaultToolCallbacks(new AsyncMcpToolCallbackProvider(mcpAsyncClients))
                   .defaultToolCallbacks(new SyncMcpToolCallbackProvider(clients))
                // This is the correct method for a list of ToolCallback instances
                 .build();
    }

//    @Bean
//    public RestClientCustomizer restClientCustomizer() {
//        return restClientBuilder -> restClientBuilder
//                .requestFactory(ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
//                        .withConnectTimeout(Duration.ofMinutes(15))
//                        .withReadTimeout(Duration.ofMinutes(15))));
//    }

//   x
}
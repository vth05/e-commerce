package com.e_commerce.e_commerce.configuration;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import redis.clients.jedis.JedisPooled;

@Configuration
public class AiConfig {
    @Value("${mistral.api.key}")
    String apiKey;
    @Value("${mistral.api.base-url}")
    String baseUrl;

    @Bean
    public MistralAiApi mistralAiApi(RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder) {
        return new MistralAiApi(
                baseUrl,
                apiKey,
                restClientBuilder,
                webClientBuilder,
                new DefaultResponseErrorHandler()
        );
    }

    @Bean
    // use @Primary to select Mistral as the embedding model instead of OpenAI
    @Primary
    public MistralAiEmbeddingModel mistralAiEmbeddingModel(MistralAiApi mistralAiApi) {
        return new MistralAiEmbeddingModel(
                mistralAiApi,
                // include metadata in the content when generating embeddings/vectors
                MetadataMode.EMBED,
                MistralAiEmbeddingOptions.builder()
                        .withModel("mistral-embed")
                        .withEncodingFormat("float")
                        .build(),
                RetryTemplate.defaultInstance(),
                ObservationRegistry.NOOP
        );
    }

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("custom-index")                // Optional: defaults to "spring-ai-index"
                .prefix("custom-prefix")                  // Optional: defaults to "embedding:"
//                .metadataFields(                         // Optional: define metadata fields for filtering
//                        RedisVectorStore.MetadataField.tag("country"),
//                        RedisVectorStore.MetadataField.numeric("year"))
                .initializeSchema(true)                   // Optional: defaults to false
                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }
}

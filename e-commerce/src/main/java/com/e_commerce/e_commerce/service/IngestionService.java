package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IngestionService implements CommandLineRunner {
    private final VectorStore vectorStore;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        List<Product> products = productRepository.findAllWithVariantsForChatbot();
        List<Document> documents = products.stream().map(product -> toDocument(product)).toList();
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> chunks = textSplitter.apply(documents);
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            String productId = chunk.getMetadata().get("parent_document_id") + "-chunk-" + chunk.getMetadata().get("chunk_index");
            chunks.set(i, new Document(
                    productId,
                    chunk.getText(),
                    chunk.getMetadata()
            ));
        }
        vectorStore.accept(chunks);
    }

    private String toText(Product product) {
        String variantsText = product.getProductVariants().stream().map(variant -> """
                Variant name: %s
                Price: %s
                Quantity: %s
                Ram: %s
                Storage: %s
                Cpu: %s
                Gpu: %s
                Screen size: %s″
                Screen resolution: %s
                Refresh rate: %sHz
                """.formatted(
                variant.getProductVariantName(),
                variant.getPrice(),
                variant.getQuantity(),
                variant.getRam(),
                variant.getStorage(),
                variant.getCpu(),
                variant.getGpu(),
                variant.getScreenSize(),
                variant.getScreenResolution(),
                variant.getRefreshRateHz()
        )).collect(Collectors.joining("\n"));

        return """
                Product id: %s
                Name: %s
                Brand: %s
                Category: %s
                Description: %s
                Variants:
                %s
                """.formatted(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getDescription(),
                variantsText
        );
    }

    private Document toDocument(Product product) {
        return new Document(
                String.valueOf(product.getId()),
                toText(product),
                Map.of(
                        "productId", product.getId(),
                        "category", product.getCategory()
                )
        );
    }
}

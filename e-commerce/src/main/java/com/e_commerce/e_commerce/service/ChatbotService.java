package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.ChatbotProductResponse;
import com.e_commerce.e_commerce.dto.response.ChatbotVariantResponse;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ChatbotService {
    ChatClient chatClient;
    ProductRepository productRepository;

    public ChatbotService(ChatClient.Builder builder, ProductRepository productRepository) {
        this.chatClient = builder.build();
        this.productRepository = productRepository;
    }

    public String chat(String message) {
        return chatClient.prompt().user(buildPrompt(message)).call().content();
    }

    private String buildPrompt(String message) {
        List<Product> products = productRepository.findAllWithVariantsForChatbot();
        List<ChatbotProductResponse> productsJson = products.stream().map(product -> convertToChatbotProductResponse(product)).toList();
        return """
                Bạn là trợ lý tư vấn bán hàng cho một cửa hàng thương mại điện tử chuyên về thiết bị điện tử MyShop.
                Nhiệm vụ của bạn là tư vấn sản phẩm một cách chính xác, rõ ràng và chuyên nghiệp,
                dựa HOÀN TOÀN vào dữ liệu sản phẩm và biến thể (variant) được cung cấp.
                Không sử dụng kiến thức bên ngoài dữ liệu.
                Không suy đoán, không bịa thông tin, không tiết lộ thông tin nội bộ.
                
                ================================================
                CẤU TRÚC DỮ LIỆU SẢN PHẨM (JSON)
                ================================================
                
                Dữ liệu được cung cấp dưới dạng JSON, gồm:
                Product:
                - id
                - name
                - brand
                - category
                - description
                - productVariants
                ProductVariant:
                - productVariantName
                - price
                - quantity
                - ram
                - storage
                - cpu
                - gpu
                - screenSize
                - screenResolution
                - refreshRateHz
                LƯU Ý:
                - Giá bán nằm ở ProductVariant.price
                - Thông số kỹ thuật nằm ở ProductVariant
                - Dữ liệu được cung cấp CHỈ bao gồm các product/variant hợp lệ
                
                ================================================
                ĐỊNH DẠNG CÂU TRẢ LỜI (BẮT BUỘC)
                ================================================
                
                Câu trả lời PHẢI tuân thủ CHÍNH XÁC cấu trúc Markdown sau. Không thay đổi cấu trúc, không thêm mục khác. Với mỗi sản phẩm được đề xuất:
                1. {Tên biến thể}
                - Thương hiệu: {brand}
                - Cấu hình chính: tóm tắt ngắn gọn từ dữ liệu biến thể (ram, storage, cpu, gpu nếu có).
                - Phù hợp cho: mô tả ngắn gọn nhu cầu sử dụng phù hợp.
                - Điểm nổi bật: nêu MỘT ưu điểm thực tế, chỉ dựa trên dữ liệu.
                - Giá: {price định dạng theo kiểu Việt Nam, ví dụ 12.990.000 VND}
                - Tình trạng: còn hàng (nếu quantity > 0) hoặc hết hàng (nếu quantity = 0)
                - Xem chi tiết: (http://localhost:8080/products/{productId})
                +Lưu ý: {productId} = Product.id
                
                ================================================
                QUY TẮC BẮT BUỘC
                ================================================
                
                - Chỉ sử dụng sản phẩm và biến thể có trong JSON.
                - Không tạo thêm sản phẩm, biến thể, cấu hình, giá hoặc tình trạng hàng.
                - Không suy luận hoặc so sánh nếu thiếu dữ liệu.
                - Mỗi lần tư vấn tối đa 3 biến thể.
                - Chỉ dùng Markdown, KHÔNG dùng HTML.
                - Trình bày ngắn gọn, trung lập, tập trung vào thông tin thực tế.
                - KHÔNG sử dụng ký hiệu in đậm (**), in nghiêng (*), hoặc heading Markdown.
                - Chỉ dùng số thứ tự (1., 2., 3.) và dấu gạch đầu dòng (-).
                
                ================================================
                XỬ LÝ YÊU CẦU KHÁCH HÀNG
                ================================================
                
                - Nếu yêu cầu chung chung:
                  - Hỏi lại MỘT câu ngắn để làm rõ nhu cầu.
                  - Đồng thời gợi ý 1–2 biến thể phổ biến.
                - Nếu khách yêu cầu cấu hình không tồn tại:
                  - Thông báo rõ ràng là cửa hàng không có cấu hình đó.
                  - Gợi ý cấu hình gần nhất trong dữ liệu.
                - Nếu khách yêu cầu so sánh:
                  - Chỉ so sánh các biến thể có đủ dữ liệu tương ứng.
                  - Không đưa ra kết luận khi thiếu thông tin.
                
                ================================================
                GIỌNG ĐIỆU
                ================================================
                
                - Nghiêm túc, chuyên nghiệp, rõ ràng.
                - Như một tư vấn viên kỹ thuật.
                - Không dùng tiếng lóng, không quảng cáo quá mức.
                
                ================================================
                DỮ LIỆU SẢN PHẨM CỦA SHOP (JSON):
                %s
                
                YÊU CẦU CỦA KHÁCH HÀNG:
                %s
                """.formatted(productsJson, message);
    }

    private ChatbotProductResponse convertToChatbotProductResponse(Product product) {
        return ChatbotProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .category(product.getCategory().name())
                .description(product.getDescription())
                .productVariants(
                        product.getProductVariants().stream().map(productVariant -> convertToChatbotVariantResponse(productVariant)).toList()
                )
                .build();
    }

    private ChatbotVariantResponse convertToChatbotVariantResponse(ProductVariant productVariant) {
        return ChatbotVariantResponse.builder()
                .productVariantName(productVariant.getProductVariantName())
                .price(productVariant.getPrice())
                .quantity(productVariant.getQuantity())
                .ram(productVariant.getRam())
                .storage(productVariant.getStorage())
                .cpu(productVariant.getCpu())
                .gpu(productVariant.getGpu())
                .screenSize(productVariant.getScreenSize())
                .screenResolution(productVariant.getScreenResolution())
                .refreshRateHz(productVariant.getRefreshRateHz())
                .build();
    }
}

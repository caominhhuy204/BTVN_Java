package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.dtos.BookCoverExtractDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookCoverAiService {

    private final ObjectMapper objectMapper;

    // OpenAI (fallback)
    @Value("${openai.api.key:}")
    private String openAiKey;
    @Value("${openai.api.model:gpt-4o-mini}")
    private String openAiModel;
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiUrl;

    // OpenRouter (ưu tiên nếu có)
    @Value("${openrouter.api.key:}")
    private String openRouterKey;
    @Value("${openrouter.api.model:}")
    private String openRouterModel;
    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterUrl;

    private RestClient restClient() {
        Assert.hasText(resolveKey(), "Cần cấu hình OPENROUTER_API_KEY hoặc OPENAI_API_KEY");
        return RestClient.builder()
                .baseUrl(resolveUrl())
                .defaultHeader("Authorization", "Bearer " + resolveKey())
                .build();
    }

    /**
     * Extract title/author using GPT-4o (OpenRouter/OpenAI).
     */
    public BookCoverExtractDto extractFromCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh bìa không được trống");
        }
        if (resolveKey() == null || resolveKey().isBlank()) {
            throw new IllegalStateException("Thiếu OPENROUTER_API_KEY hoặc OPENAI_API_KEY");
        }

        String imageData = encode(file);
        Map<String, Object> payload = Map.of(
                "model", resolveModel(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", prompt()),
                                Map.of("type", "image_url", "image_url", imageData)
                        )
                ))
        );

        try {
            String response = restClient().post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (response == null) return BookCoverExtractDto.empty();

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) return BookCoverExtractDto.empty();

            BookCoverExtractDto dto = objectMapper.readValue(content, BookCoverExtractDto.class);
            return dto == null ? BookCoverExtractDto.empty() : dto;
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            int code = e.getStatusCode() != null ? e.getStatusCode().value() : -1;
            String message = "OpenAI/OpenRouter error " + code + " - " + (body != null ? body : e.getMessage());
            throw new IllegalStateException(message, e);
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được phản hồi AI: " + e.getMessage(), e);
        } catch (Exception ex) {
            throw new IllegalStateException("Gọi AI thất bại: " + ex.getMessage(), ex);
        }
    }

    private String encode(MultipartFile file) {
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String type = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            return "data:" + type + ";base64," + base64;
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được file ảnh", e);
        }
    }

    private String prompt() {
        return "Bạn là trợ lý nhập liệu. Đọc nội dung trên ảnh bìa sách và trích xuất JSON với 2 khóa: \"title\" và \"author\". "
                + "Nếu không chắc, trả về null cho trường đó. Chỉ dùng chữ trên ảnh, không suy đoán từ tên file.";
    }

    private String resolveKey() {
        return (openRouterKey != null && !openRouterKey.isBlank()) ? openRouterKey : openAiKey;
    }

    private String resolveModel() {
        if (openRouterKey != null && !openRouterKey.isBlank() && openRouterModel != null && !openRouterModel.isBlank()) {
            return openRouterModel;
        }
        return openAiModel;
    }

    private String resolveUrl() {
        if (openRouterKey != null && !openRouterKey.isBlank()) {
            return openRouterUrl;
        }
        return openAiUrl;
    }
}

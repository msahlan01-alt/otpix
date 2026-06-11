package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== 1. AUTO-FILL METADATA PAKAIAN =====
    public ClothingMetadata analyzeClothingImage(MultipartFile image, String name, String category) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        String prompt = "Analisis gambar pakaian ini dan berikan informasi dalam format JSON.\n" +
                "Nama item: \"" + name + "\", Kategori: \"" + category + "\"\n\n" +
                "Kembalikan HANYA JSON ini tanpa teks lain:\n" +
                "{\n" +
                "  \"color\": \"<kode hex warna dominan, contoh: #3A5F8C>\",\n" +
                "  \"secondaryColor\": \"<kode hex warna sekunder, contoh: #FFFFFF>\",\n" +
                "  \"fit\": \"<salah satu: Regular, Slim, Oversized, Loose>\",\n" +
                "  \"conditionStatus\": \"<salah satu: Good, Fair, Poor>\",\n" +
                "  \"formalityLevel\": <angka 1-3, 1=Casual, 2=Smart Casual, 3=Formal>,\n" +
                "  \"tags\": [\"<tag1>\", \"<tag2>\", \"<tag3>\"]\n" +
                "}\n\n" +
                "Untuk tags, pilih 2-4 yang paling relevan dari:\n" +
                "casual, formal, smart-casual, sporty, elegant, streetwear, vintage,\n" +
                "minimalist, colorful, monochrome, summer, rainy, outdoor, office, party, daily";

        String requestBody = buildRequestWithImage(mimeType, base64Image, prompt);
        HttpResponse<String> response = sendRequest(requestBody);
        return parseClothingMetadata(response.body());
    }

    // ===== 2. GENERATE OUTFIT RECOMMENDATION =====
    public OutfitResult generateOutfit(com.example.demo.Model.Schedule schedule, List<ClothingItemSummary> items)
            throws Exception {
        String itemsJson = objectMapper.writeValueAsString(items);

        String prompt = "Kamu adalah fashion stylist AI bernama Outfix.\n" +
                "Rekomendasikan kombinasi outfit terbaik berdasarkan agenda dan koleksi pakaian berikut.\n\n" +
                "Agenda:\n" +
                "- Judul: \"" + schedule.getTitle() + "\"\n" +
                "- Tipe Event: \"" + (schedule.getEventType() != null ? schedule.getEventType() : "Umum") + "\"\n" +
                "- Dress Code: \"" + (schedule.getDressCode() != null ? schedule.getDressCode() : "Bebas") + "\"\n" +
                "- Lokasi: \"" + (schedule.getLocation() != null ? schedule.getLocation() : "Tidak ditentukan") + "\"\n"
                +
                "- Tanggal: \"" + schedule.getEventDate() + "\"\n\n" +
                "Koleksi pakaian user (formalityLevel: 1=Casual, 2=Smart Casual, 3=Formal):\n" +
                itemsJson + "\n\n" +
                "Aturan:\n" +
                "- Pilih maksimal 1 item per kategori (Atasan, Bawahan, Luaran, Sepatu, Aksesori, Tas)\n" +
                "- Sesuaikan formalityLevel dengan agenda\n" +
                "- Luaran, Aksesori, Tas bersifat opsional\n" +
                "- Jika lokasi Outdoor, pertimbangkan Luaran\n\n" +
                "Kembalikan HANYA JSON ini tanpa teks lain:\n" +
                "{\n" +
                "  \"outfitName\": \"<nama outfit singkat>\",\n" +
                "  \"reason\": \"<alasan singkat kenapa kombinasi ini cocok, max 2 kalimat>\",\n" +
                "  \"itemIds\": [<id1>, <id2>, <id3>],\n" +
                "  \"tips\": \"<tips styling singkat, max 1 kalimat>\"\n" +
                "}";

        String requestBody = buildRequestText(prompt);
        HttpResponse<String> response = sendRequest(requestBody);
        return parseOutfitResult(response.body());
    }

    // ===== PARSER =====

    private ClothingMetadata parseClothingMetadata(String responseBody) throws Exception {
        String text = extractText(responseBody);
        JsonNode data = objectMapper.readTree(text);

        ClothingMetadata metadata = new ClothingMetadata();
        metadata.color = data.path("color").asText("#000000");
        metadata.secondaryColor = data.path("secondaryColor").asText("#333333");
        metadata.fit = data.path("fit").asText("Regular");
        metadata.conditionStatus = data.path("conditionStatus").asText("Good");
        metadata.formalityLevel = data.path("formalityLevel").asInt(2);

        for (JsonNode tag : data.path("tags")) {
            metadata.tags.add(tag.asText());
        }
        return metadata;
    }

    private OutfitResult parseOutfitResult(String responseBody) throws Exception {
        String text = extractText(responseBody);
        JsonNode data = objectMapper.readTree(text);

        OutfitResult result = new OutfitResult();
        result.outfitName = data.path("outfitName").asText("Outfit Rekomendasi");
        result.reason = data.path("reason").asText("");
        result.tips = data.path("tips").asText("");

        for (JsonNode id : data.path("itemIds")) {
            result.itemIds.add(id.asLong());
        }
        return result;
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
        return text.replaceAll("```json|```", "").trim();
    }

    // ===== REQUEST BUILDER =====

    private String buildRequestWithImage(String mimeType, String base64, String prompt) throws Exception {
        return "{\"contents\":[{\"parts\":[{\"inline_data\":{\"mime_type\":\"" + mimeType +
                "\",\"data\":\"" + base64 + "\"}},{\"text\":" +
                objectMapper.writeValueAsString(prompt) + "}]}]}";
    }

    private String buildRequestText(String prompt) throws Exception {
        return "{\"contents\":[{\"parts\":[{\"text\":" +
                objectMapper.writeValueAsString(prompt) + "}]}]}";
    }

    private HttpResponse<String> sendRequest(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ===== INNER CLASSES =====

    public static class ClothingMetadata {
        public String color = "#000000";
        public String secondaryColor = "#333333";
        public String fit = "Regular";
        public String conditionStatus = "Good";
        public int formalityLevel = 2;
        public List<String> tags = new ArrayList<>();
    }

    public static class OutfitResult {
        public String outfitName;
        public String reason;
        public String tips;
        public List<Long> itemIds = new ArrayList<>();
    }

    public static class ClothingItemSummary {
        public Long id;
        public String name;
        public String category;
        public int formalityLevel;
        public List<String> tags;

        public ClothingItemSummary(Long id, String name, String category,
                int formalityLevel, List<String> tags) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.formalityLevel = formalityLevel;
            this.tags = tags;
        }
    }
}
package com.hjw.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RecursiveTextSplitter {
    private static final List<String> DEFAULT_SEPARATORS = List.of(
            "\n\n\n",  // 章节分隔
            "\n\n",    // 段落分隔
            "\n",      // 换行
            "。",      // 中文句号
            "！",      // 中文感叹号
            "？",      // 中文问号
            ".",       // 英文句号
            "!",       // 英文感叹号
            "?",       // 英文问号
            "；",      // 中文分号
            ";",       // 英文分号
            "，",      // 中文逗号
            ",",       // 英文逗号
            " ",       // 空格
            ""         // 空字符
    );
    private final int chunkSize;
    private final int chunkOverlap;
    private final List<String> separators;

    public RecursiveTextSplitter() {
        this(400, 60, DEFAULT_SEPARATORS);
    }

    public RecursiveTextSplitter(int chunkSize, int chunkOverlap, List<String> separators) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separators = separators;
    }
    public List<Document> split(Document document) {
        List<Document> chunks = split(document, 0);
        int total = chunks.size();
        for (int i = 0; i < total; i++) {
            Document chunk = chunks.get(i);
            chunk.getMetadata().put("chunk_index", i);
        }
        return chunks;
    }

    private List<Document> split(Document document, int separatorIndex) {
        String text = document.getText();

        if (text.length() <= chunkSize) {
            return List.of(document);
        }

        if (separatorIndex >= separators.size()) {
            return splitBySize(document);
        }

        String separator = separators.get(separatorIndex);
        List<String> splits = splitBySeparator(text, separator);

        List<Document> result = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String split : splits) {
            if (split.isEmpty()) continue;

            if (currentChunk.length() + split.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    result.add(createDocument(currentChunk.toString(), document));
                    currentChunk = new StringBuilder();

                    if (chunkOverlap > 0 && !result.isEmpty()) {
                        String lastChunk = result.get(result.size() - 1).getText();
                        String overlap = lastChunk.substring(Math.max(0, lastChunk.length() - chunkOverlap));
                        currentChunk.append(overlap);
                    }
                }

                if (split.length() > chunkSize) {
                    Document subDoc = createDocument(split, document);
                    result.addAll(split(subDoc, separatorIndex + 1));
                } else {
                    currentChunk.append(split);
                }
            } else {
                currentChunk.append(split);
            }
        }

        if (currentChunk.length() > 0) {
            result.add(createDocument(currentChunk.toString(), document));
        }

        return result;
    }

    private List<String> splitBySeparator(String text, String separator) {
        List<String> result = new ArrayList<>();

        if (separator.isEmpty()) {
            for (char c : text.toCharArray()) {
                result.add(String.valueOf(c));
            }
            return result;
        }

        int start = 0;
        int index;

        while ((index = text.indexOf(separator, start)) != -1) {
            result.add(text.substring(start, index + separator.length()));
            start = index + separator.length();
        }

        if (start < text.length()) {
            result.add(text.substring(start));
        }

        return result;
    }

    private List<Document> splitBySize(Document document) {
        List<Document> result = new ArrayList<>();
        String text = document.getText();

        for (int i = 0; i < text.length(); i += chunkSize - chunkOverlap) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end);
            result.add(createDocument(chunk, document));
        }

        return result;
    }

    //复制元信息给分块
    private Document createDocument(String text, Document source) {
        return new Document(text, new java.util.HashMap<>(source.getMetadata()));
    }
}

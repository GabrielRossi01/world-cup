package br.com.fiap.worldcup.service;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagInjection {

    private final VectorStore vectorStore;

    public RagInjection(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadPdfToVector() {
        String pdf = "classpath:doc/fwc26-match-schedule.pdf";

        var reader = new PagePdfDocumentReader(pdf);
        var splitter = TokenTextSplitter.builder().build();

        List<Document> documents = splitter.apply(reader.get());
        vectorStore.add(documents);
    }

}

package br.com.fiap.worldcup.service;

import br.com.fiap.worldcup.tools.WorldCupTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final String DEFAULT_CONVERSATION_ID = "world-cup-ui";

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, WorldCupTools worldCupTools) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem("""
                            Você é um assistente especialista na Copa do Mundo 2026.
                            Responda sempre em português do Brasil.
                            Regras:
                            Use tools apenas quando o usuário perguntar sobre datas, estreia, agenda, jogos ou calendário de uma seleção.
                            Quando usar uma tool, use somente o nome exato da seleção mencionado pelo usuário.
                            Se o nome da seleção não estiver claro, peça esclarecimento em vez de chamar a tool.
                            Nunca invente argumentos para tools.
                            Para contexto geral do torneio, grupos e regulamento, use o contexto recuperado do RAG.
                """)
                .defaultTools(worldCupTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public Flux<String> sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, DEFAULT_CONVERSATION_ID))
                .stream()
                .content();
    }
}
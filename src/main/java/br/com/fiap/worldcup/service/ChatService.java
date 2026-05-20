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
                            Você é um agente especialista na Copa do Mundo 2026.
                            Responda em português do Brasil.
                            Seja claro, objetivo e didático.
                            Não invente informações.
                            Regras:
                            Sempre que a pergunta envolver calendário, estreia, datas, horários, grupo, estádio ou agenda de jogos,
                            consulte obrigatoriamente as ferramentas disponíveis antes de responder.
                            Use o contexto recuperado do RAG para complementar informações históricas, regulamento e contexto do torneio.
                            Se a informação não estiver na base, diga explicitamente isso.
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
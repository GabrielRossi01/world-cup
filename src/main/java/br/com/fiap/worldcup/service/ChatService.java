package br.com.fiap.worldcup.service;

import br.com.fiap.worldcup.dto.MatchAnalysis;
import br.com.fiap.worldcup.tools.WorldCupTools;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, WorldCupTools worldCupTools) {
        this.chatClient = builder
                .defaultSystem("""
                        Você é um agente especialista na Copa do Mundo 2026.
                        Responda em português do Brasil.
                        Seja claro, objetivo e didático.
                        Use markdown quando fizer sentido.
                        Você deve usar as ferramentas disponíveis sempre que precisar consultar ou salvar dados.
                        Quando houver skills disponíveis, siga as instruções das skills.
                        Não invente dados.
                        """)
                .defaultTools(worldCupTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().maxMessages(20).build()
                        ).build()
                )
                .build();
    }

    public Flux<String> sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    public String sendMessageSync(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public MatchAnalysis generateAnalysis(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(MatchAnalysis.class);
    }
}
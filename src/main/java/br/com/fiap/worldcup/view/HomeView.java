package br.com.fiap.worldcup.view;

import br.com.fiap.worldcup.service.ChatService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.Instant;

@Route("")
public class HomeView extends VerticalLayout {

    private final ChatService chatService;
    private final MessageList list = new MessageList();

    public HomeView(ChatService chatService) {
        this.chatService = chatService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        getStyle()
                .set("background",
                        "radial-gradient(circle at top left, rgba(42,57,141,0.22), transparent 30%)," +
                                "radial-gradient(circle at top right, rgba(230,29,37,0.16), transparent 24%)," +
                                "radial-gradient(circle at bottom left, rgba(60,172,59,0.14), transparent 28%)," +
                                "linear-gradient(180deg, #06101d 0%, #0a1220 55%, #08111d 100%)")
                .set("padding", "28px")
                .set("color", "#F5F7FB");

        Div shell = buildShell();
        add(shell);
        expand(shell);
    }

    private Div buildShell() {
        Div shell = new Div();
        shell.setWidthFull();

        shell.getStyle()
                .set("max-width", "1080px")
                .set("height", "100%")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "24px")
                .set("border-radius", "26px")
                .set("background", "rgba(10, 18, 32, 0.72)")
                .set("backdrop-filter", "blur(16px)")
                .set("-webkit-backdrop-filter", "blur(16px)")
                .set("border", "1px solid rgba(255,255,255,0.08)")
                .set("box-shadow", "0 20px 60px rgba(0,0,0,0.35)")
                .set("font-family", "Inter, Segoe UI, Arial, sans-serif");

        shell.add(buildHeader(), buildChatArea(), buildInputArea());
        return shell;
    }

    private Div buildHeader() {
        Div header = new Div();
        header.setWidthFull();

        header.getStyle()
                .set("padding", "2px 4px 18px 4px")
                .set("margin-bottom", "12px")
                .set("border-bottom", "1px solid rgba(255,255,255,0.08)");

        H1 title = new H1("World Cup 2026 AI Assistant");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("letter-spacing", "-0.03em")
                .set("color", "#FFFFFF");

        Paragraph subtitle = new Paragraph(
                "Converse sobre a Copa do Mundo 2026 com um agente inteligente usando memória, tool calling, embeddings com Ollama e RAG."
        );
        subtitle.getStyle()
                .set("margin", "10px 0 0 0")
                .set("font-size", "1rem")
                .set("line-height", "1.65")
                .set("color", "rgba(245,247,251,0.78)")
                .set("max-width", "860px");

        Div chips = new Div();
        chips.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "10px")
                .set("margin-top", "16px");

        chips.add(
                createChip("Spring AI", "#2A398D"),
                createChip("Tool Calling", "#E61D25"),
                createChip("RAG + Ollama", "#3CAC3B"),
                createChip("Vaadin", "#2A398D")
        );

        header.add(title, subtitle, chips);
        return header;
    }

    private Div buildChatArea() {
        Div area = new Div();
        area.setWidthFull();

        area.getStyle()
                .set("flex", "1")
                .set("min-height", "0")
                .set("display", "flex")
                .set("padding", "8px 0");

        list.setWidthFull();
        list.setHeightFull();
        list.setMarkdown(true);

        list.getStyle()
                .set("background", "rgba(255,255,255,0.03)")
                .set("border", "1px solid rgba(255,255,255,0.06)")
                .set("border-radius", "20px")
                .set("padding", "16px")
                .set("overflow", "auto")
                .set("color", "#F5F7FB");

        addWelcomeMessage();

        area.add(list);
        return area;
    }

    private Div buildInputArea() {
        Div area = new Div();
        area.setWidthFull();

        area.getStyle()
                .set("padding-top", "14px")
                .set("margin-top", "8px")
                .set("border-top", "1px solid rgba(255,255,255,0.08)");

        MessageInput input = new MessageInput();
        input.setWidthFull();

        input.getStyle()
                .set("background", "rgba(255,255,255,0.04)")
                .set("border", "1px solid rgba(255,255,255,0.08)")
                .set("border-radius", "18px")
                .set("padding", "6px")
                .set("box-shadow", "inset 0 0 0 1px rgba(42,57,141,0.04)");

        input.addSubmitListener(submitEvent -> {
            String messageText = submitEvent.getValue();

            if (messageText == null || messageText.isBlank()) {
                Notification notification = Notification.show("Digite uma pergunta antes de enviar.");
                notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                notification.setPosition(Notification.Position.BOTTOM_END);
                return;
            }

            addUserMessage(messageText);

            MessageListItem responseMessage = new MessageListItem("", Instant.now(), "Assistente");
            responseMessage.setUserColorIndex(2);
            list.addItem(responseMessage);

            chatService.sendMessage(messageText)
                    .doOnNext(partialResponse -> getUI().ifPresent(ui -> ui.access(() -> {
                        responseMessage.appendText(partialResponse);
                    })))
                    .doOnError(error -> getUI().ifPresent(ui -> ui.access(() -> {
                        responseMessage.setText("Erro: " + error.getMessage());

                        Notification notification = Notification.show("Erro ao consultar o agente.");
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        notification.setPosition(Notification.Position.BOTTOM_END);
                    })))
                    .subscribe();
        });

        area.add(input);
        return area;
    }

    private void addWelcomeMessage() {
        MessageListItem welcome = new MessageListItem(
                """
                Olá! Sou o seu assistente da **Copa do Mundo 2026**.

                Posso te ajudar com:
                - agenda e estreia de seleções
                - análises de confrontos
                - grupos e contexto do torneio
                - previsões com IA

                Exemplos:
                - Quando o Brasil estreia?
                - Qual é a agenda da Argentina?
                - Quem é favorito entre França e Inglaterra?
                """,
                Instant.now(),
                "Assistente"
        );
        welcome.setUserColorIndex(2);
        list.addItem(welcome);
    }

    private void addUserMessage(String messageText) {
        MessageListItem message = new MessageListItem(messageText, Instant.now(), "Você");
        message.setUserColorIndex(1);
        list.addItem(message);
    }

    private Div createChip(String text, String color) {
        Div chip = new Div(text);
        chip.getStyle()
                .set("padding", "8px 12px")
                .set("border-radius", "999px")
                .set("font-size", "0.84rem")
                .set("font-weight", "600")
                .set("color", "#F5F7FB")
                .set("background", "color-mix(in srgb, " + color + " 22%, transparent)")
                .set("border", "1px solid color-mix(in srgb, " + color + " 44%, rgba(255,255,255,0.08))")
                .set("backdrop-filter", "blur(6px)");

        return chip;
    }
}
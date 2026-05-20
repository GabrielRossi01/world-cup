package br.com.fiap.worldcup.view;

import br.com.fiap.worldcup.model.MatchPrediction;
import br.com.fiap.worldcup.repository.MatchPredictionRepository;
import br.com.fiap.worldcup.service.ChatService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class HomeView extends VerticalLayout {

    private final ChatService chatService;
    private final MatchPredictionRepository matchPredictionRepository;
    private final Grid<MatchPrediction> grid = new Grid<>(MatchPrediction.class, false);
    private final Div responseBox = new Div();

    public HomeView(ChatService chatService, MatchPredictionRepository matchPredictionRepository) {
        this.chatService = chatService;
        this.matchPredictionRepository = matchPredictionRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader(), buildInteractionSection(), buildGridSection());

        loadDataToGrid();
    }

    private Component buildHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);

        H1 title = new H1("World Cup 2026 AI Assistant");
        H2 subtitle = new H2("Análises, previsões e consultas com Spring AI");
        Paragraph description = new Paragraph(
                "Faça perguntas sobre seleções, jogadores, confrontos e previsões da Copa do Mundo 2026. " +
                        "O agente utiliza memória, tool calling e recuperação de contexto para responder."
        );

        subtitle.getStyle()
                .set("margin-top", "0")
                .set("font-size", "1.2rem")
                .set("color", "var(--lumo-secondary-text-color)");

        description.getStyle()
                .set("max-width", "900px")
                .set("margin-top", "0.5rem");

        header.add(title, subtitle, description);
        return header;
    }

    private Component buildInteractionSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);

        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");

        H3 sectionTitle = new H3("Pergunte ao agente");
        Paragraph helper = new Paragraph(
                "Exemplos: “Me fale sobre o Brasil”, “Quem é favorito entre França e Argentina?”, " +
                        "“Salve uma previsão de Brasil x Alemanha com placar 2x1”."
        );

        responseBox.setWidthFull();
        responseBox.getStyle()
                .set("min-height", "180px")
                .set("padding", "16px")
                .set("border-radius", "10px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("white-space", "pre-wrap");

        responseBox.add(new Text("A resposta do agente aparecerá aqui."));

        Scroller scroller = new Scroller(responseBox);
        scroller.setWidthFull();
        scroller.setHeight("220px");

        MessageInput input = new MessageInput();
        input.setWidthFull();
        input.addSubmitListener(event -> runPrompt(event.getValue()));

        section.add(sectionTitle, helper, scroller, input);
        return section;
    }

    private Component buildGridSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(false);
        section.setSpacing(true);

        H3 sectionTitle = new H3("Previsões salvas");

        grid.addColumn(MatchPrediction::getId)
                .setHeader("ID")
                .setFlexGrow(0)
                .setWidth("90px");

        grid.addColumn(MatchPrediction::getHomeTeam)
                .setHeader("Mandante")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(MatchPrediction::getAwayTeam)
                .setHeader("Visitante")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(MatchPrediction::getPredictedScore)
                .setHeader("Placar")
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addColumn(MatchPrediction::getFavoriteTeam)
                .setHeader("Favorito")
                .setAutoWidth(true);

        grid.addColumn(MatchPrediction::getAnalysis)
                .setHeader("Análise")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setWidthFull();
        grid.setHeight("320px");

        section.add(sectionTitle, grid);
        return section;
    }

    private void runPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            Notification notification = Notification.show("Digite uma pergunta antes de enviar.");
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            notification.setPosition(Notification.Position.BOTTOM_END);
            return;
        }

        responseBox.removeAll();
        Pre responsePre = new Pre();
        responsePre.getStyle().set("white-space", "pre-wrap");
        responseBox.add(responsePre);

        StringBuilder builder = new StringBuilder();

        chatService.sendMessage(prompt)
                .doOnNext(chunk -> getUI().ifPresent(ui -> ui.access(() -> {
                    builder.append(chunk);
                    responsePre.setText(builder.toString());
                })))
                .doOnError(error -> getUI().ifPresent(ui -> ui.access(() -> {
                    responsePre.setText("Erro real: " + error.getMessage());
                    Notification notification = Notification.show("Erro ao consultar o agente.");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setPosition(Notification.Position.BOTTOM_END);
                })))
                .doOnComplete(() -> getUI().ifPresent(ui -> ui.access(this::loadDataToGrid)))
                .subscribe();
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable result = throwable;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    private void loadDataToGrid() {
        grid.setItems(matchPredictionRepository.findAll());
    }
}
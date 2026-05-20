package br.com.fiap.worldcup.view;

import br.com.fiap.worldcup.service.ChatService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

@Route("")
public class HomeView extends VerticalLayout {

    public HomeView(ChatService chatService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("World Cup AI Assistant");

        TextArea question = new TextArea("Pergunta");
        question.setWidthFull();
        question.setPlaceholder("Ex: O Brasil é favorito?");

        Button askButton = new Button("Perguntar");

        Pre answer = new Pre();
        answer.setWidthFull();
        answer.getStyle()
                .set("background", "#f4f4f4")
                .set("padding", "16px")
                .set("border-radius", "8px")
                .set("white-space", "pre-wrap");

        askButton.addClickListener(event -> {
            String response = chatService.sendMessageSync(question.getValue());
            answer.setText(response);
        });

        add(title, question, askButton, answer);
    }
}
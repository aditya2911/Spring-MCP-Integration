package org.example.springai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RequestBodyDTO {

    String model;
    List<MessagesDTO> messages;

    public RequestBodyDTO(String model, List<MessagesDTO> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<MessagesDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagesDTO> messages) {
        this.messages = messages;
    }
}

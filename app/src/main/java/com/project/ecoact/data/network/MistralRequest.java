package com.project.ecoact.data.network;

import java.util.List;
// Represente le  body JSON envoyé à Mistral
public class MistralRequest {

    private String model;
    private List<Message> messages;

    public MistralRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
// On envoie toujours un seul message avec le rôle "user" 
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

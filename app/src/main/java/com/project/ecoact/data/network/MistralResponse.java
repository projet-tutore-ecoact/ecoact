package com.project.ecoact.data.network;

import java.util.List;
/**
 * Représente la réponse JSON de l'API Mistral.
 * On lit uniquement choices[0].message.content pour récupérer le conseil.
 */
public class MistralResponse {

    private List<Choice> choices;// Mistral peut retourner plusieurs choix, on prend le premier

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        private String content;   // le texte généré par Mistral

        public String getContent() {
            return content;
        }
    }
}

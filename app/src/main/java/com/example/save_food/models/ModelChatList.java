package com.example.save_food.models;

public class ModelChatList {
    private String sender;
    private String receiver;
    private String message;
    private String timestamp;
    private boolean dilihat;
    private String type;
    private String id;

    // Constructor không đối số
    public ModelChatList() { }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isDilihat() { return dilihat; }
    public void setDilihat(boolean dilihat) { this.dilihat = dilihat; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

// ChatMessage.java - Modelo de mensajes de chat
package com.example.telehotel.core.storage;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderRole; // CLIENT, ADMIN, SYSTEM
    private String message;
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM_NOTIFICATION
    private String attachmentUrl;
    private String attachmentType; // IMAGE, PDF, DOC
    private long timestamp;
    private boolean isRead;
    private boolean isDelivered;
    private String hotelId;
    private String replyToMessageId;

    // Tipos de roles
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SYSTEM = "SYSTEM";

    // Tipos de mensajes
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.isDelivered = false;
        this.messageType = TYPE_TEXT;
    }


    public ChatMessage(String senderId, String senderName, String senderRole, String message, String hotelId) {
        this.messageId = generateMessageId();
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.hotelId = hotelId;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.isDelivered = false;
        this.messageType = TYPE_TEXT;
    }

    // Método para generar ID de mensaje único
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters y Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    // Métodos utilitarios
    public boolean isFromClient() {
        return ROLE_CLIENT.equals(senderRole);
    }

    public boolean isFromAdmin() {
        return ROLE_ADMIN.equals(senderRole);
    }

    public boolean isSystemMessage() {
        return ROLE_SYSTEM.equals(senderRole);
    }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }

    public boolean isImageMessage() {
        return TYPE_IMAGE.equals(messageType);
    }

    public boolean isFileMessage() {
        return TYPE_FILE.equals(messageType);
    }

    public boolean isReply() {
        return replyToMessageId != null && !replyToMessageId.isEmpty();
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new java.util.Date(timestamp));
    }

    public String getFormattedDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    // Método para crear mensaje del sistema
    public static ChatMessage createSystemMessage(String message, String hotelId) {
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.messageId = systemMsg.generateMessageId();
        systemMsg.senderId = "SYSTEM";
        systemMsg.senderName = "Sistema";
        systemMsg.senderRole = ROLE_SYSTEM;
        systemMsg.message = message;
        systemMsg.hotelId = hotelId;
        systemMsg.messageType = TYPE_SYSTEM_NOTIFICATION;
        systemMsg.isDelivered = true;
        systemMsg.isRead = true;
        return systemMsg;
    }

    // Método para crear mensaje con adjunto
    public static ChatMessage createMessageWithAttachment(String senderId, String senderName,
                                                          String senderRole, String message,
                                                          String hotelId, String attachmentUrl,
                                                          String attachmentType) {
        ChatMessage msg = new ChatMessage(senderId, senderName, senderRole, message, hotelId);
        msg.attachmentUrl = attachmentUrl;
        msg.attachmentType = attachmentType;
        msg.messageType = attachmentType.toLowerCase().contains("image") ? TYPE_IMAGE : TYPE_FILE;
        return msg;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderRole='" + senderRole + '\'' +
                ", message='" + message + '\'' +
                ", messageType='" + messageType + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}
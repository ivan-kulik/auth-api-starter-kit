package com.starter.feature.auth.email;

public interface EmailSender {

    void send(String to, String subject, String text);
}

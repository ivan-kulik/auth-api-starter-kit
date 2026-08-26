package com.starter.common.email;

public interface EmailSender {

    void send(String to, String subject, String text);
}

package com.example.socialnetworkingbackend.service;

public interface MailService {
    public boolean sendEmailWithObject(String receivedEmail, Object object, String subject);
}

package com.memoize.api.Service;

public interface MailService {
    void sendVerificationEmail(String email, String verificationCode);
}

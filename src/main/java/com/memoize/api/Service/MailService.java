package com.memoize.api.Service;

import com.memoize.api.Dto.EmailRequest;

public interface MailService {
    void sendEmail(EmailRequest emailRequest);

    void sendVerificationEmail(String email, String verificationCode);
}

package com.memoize.api.Service;

import com.memoize.api.Dto.EmailRequest;

public interface MailService {
    void sendVerificationEmail(String email, String verificationCode);
}

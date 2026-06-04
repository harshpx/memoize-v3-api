package com.memoize.api.Service;

import com.memoize.api.Enum.VerificationType;

public interface MailService {
    void sendVerificationCodeEmail(String email, String verificationCode, VerificationType verificationType);
}

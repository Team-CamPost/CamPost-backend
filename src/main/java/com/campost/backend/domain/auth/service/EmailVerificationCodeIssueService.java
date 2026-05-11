package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import com.campost.backend.domain.auth.repository.EmailVerificationRepository;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationCodeIssueService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public EmailVerificationCodeIssueService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Transactional
    public void issueCode(EmailVerificationCodeCreateCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicatedEmailException();
        }

        emailVerificationRepository.saveCode(command);
    }
}

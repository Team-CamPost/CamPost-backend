package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;

public interface UserRepository {

    User save(SignupUserCreateCommand command);
}

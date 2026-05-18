package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcClient jdbcClient;

    public JdbcUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public User save(SignupUserCreateCommand command) {
        String sql = """
                INSERT INTO users (name, username, email, password_hash, role)
                VALUES (:name, :username, :email, :passwordHash, 'GUEST')
                RETURNING id, username, name, email, password_hash, role, created_at
                """;

        return jdbcClient.sql(sql)
                .param("name", command.name())
                .param("username", command.username())
                .param("email", command.email())
                .param("passwordHash", command.passwordHash())
                .query((rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .single();
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE email = :email
                )
                """;

        return Boolean.TRUE.equals(jdbcClient.sql(sql)
                .param("email", email)
                .query(Boolean.class)
                .single());
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE username = :username
                )
                """;

        return Boolean.TRUE.equals(jdbcClient.sql(sql)
                .param("username", username)
                .query(Boolean.class)
                .single());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT id, username, name, email, password_hash, role, created_at
                FROM users
                WHERE username = :username
                """;

        return jdbcClient.sql(sql)
                .param("username", username)
                .query((rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
        String sql = """
                UPDATE users
                SET department = :department,
                    grade = :grade,
                    name = :nickname,
                    profile_completed = true
                WHERE id = :userId
                RETURNING id, department, grade, name, profile_completed
                """;

        return jdbcClient.sql(sql)
                .param("department", command.department())
                .param("grade", command.grade())
                .param("nickname", command.nickname())
                .param("userId", command.userId())
                .query((rs, rowNum) -> new UserOnboardingProfile(
                        rs.getLong("id"),
                        rs.getString("department"),
                        rs.getInt("grade"),
                        rs.getString("name"),
                        rs.getBoolean("profile_completed")
                ))
                .optional();
    }
}

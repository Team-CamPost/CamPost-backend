package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final RowMapper<UserProfile> USER_PROFILE_ROW_MAPPER = (rs, rowNum) -> new UserProfile(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("department"),
            rs.getObject("grade", Integer.class),
            rs.getString("role"),
            rs.getBoolean("profile_completed"),
            rs.getObject("created_at", java.time.OffsetDateTime.class),
            rs.getObject("last_login_at", java.time.OffsetDateTime.class)
    );

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getObject("created_at", java.time.OffsetDateTime.class)
    );

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
                .query(USER_ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<User> findById(long userId) {
        String sql = """
                SELECT id, username, name, email, password_hash, role, created_at
                FROM users
                WHERE id = :userId
                """;

        return jdbcClient.sql(sql)
                .param("userId", userId)
                .query(USER_ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<UserProfile> findProfileById(long userId) {
        String sql = """
                SELECT id, username, email, name, department, grade, role,
                       profile_completed, created_at, last_login_at
                FROM users
                WHERE id = :userId
                """;

        return jdbcClient.sql(sql)
                .param("userId", userId)
                .query(USER_PROFILE_ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<UserProfile> updateProfile(UserProfileUpdateCommand command) {
        String sql = """
                UPDATE users
                SET department = :department,
                    grade = :grade,
                    name = :nickname
                WHERE id = :userId
                RETURNING id, username, email, name, department, grade, role,
                          profile_completed, created_at, last_login_at
                """;

        return jdbcClient.sql(sql)
                .param("department", command.department())
                .param("grade", command.grade())
                .param("nickname", command.nickname())
                .param("userId", command.userId())
                .query(USER_PROFILE_ROW_MAPPER)
                .optional();
    }

    @Override
    public boolean updatePasswordHash(long userId, String passwordHash) {
        String sql = """
                UPDATE users
                SET password_hash = :passwordHash
                WHERE id = :userId
                """;

        return jdbcClient.sql(sql)
                .param("passwordHash", passwordHash)
                .param("userId", userId)
                .update() == 1;
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

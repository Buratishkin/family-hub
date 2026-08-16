package org.buratishkin.familyhub.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);
    UserEntity findByEmailIgnoreCase(String email);
}

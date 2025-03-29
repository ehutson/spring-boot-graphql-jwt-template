package dev.ehutson.template.repository;

import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {
    String USERS_BY_USERNAME_CACHE = "usersByUsername";
    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    boolean existsByRolesContaining(RoleModel role);

    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE, unless = "#result == null")
    Optional<UserModel> findOneByEmailIgnoreCase(String email);

    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE, unless = "#result == null")
    boolean existsByEmail(String email);

    @Cacheable(cacheNames = USERS_BY_USERNAME_CACHE, unless = "#result == null")
    Optional<UserModel> findOneByUsername(String username);

    @Cacheable(cacheNames = USERS_BY_USERNAME_CACHE, unless = "#result == null")
    boolean existsByUsername(String username);

    Page<UserModel> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    Optional<UserModel> findOneByActivationKey(String activationKey);

    List<UserModel> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedAtBefore(Instant dateTime);

    Optional<UserModel> findOneByResetKey(String resetKey);

    @NotNull
    @CacheEvict(cacheNames = {USERS_BY_USERNAME_CACHE, USERS_BY_EMAIL_CACHE}, key = "#entity.username")
    <S extends UserModel> S save(@NotNull S entity);
}

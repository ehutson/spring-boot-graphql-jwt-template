package dev.ehutson.template.repository;

import dev.ehutson.template.domain.RefreshTokenModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenModel, String> {
    Optional<RefreshTokenModel> findByToken(String token);

    List<RefreshTokenModel> findByUserId(String userId);

    List<RefreshTokenModel> findByUserIdAndRevokedFalse(String userId);

    void deleteByExpiresAtBefore(OffsetDateTime now);

    Optional<RefreshTokenModel> findByTokenAndRevokedFalse(String token);
}

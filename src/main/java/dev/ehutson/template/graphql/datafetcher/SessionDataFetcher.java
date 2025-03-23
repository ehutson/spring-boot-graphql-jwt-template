package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import dev.ehutson.template.codegen.types.Session;
import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.mapper.RefreshTokenMapper;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class SessionDataFetcher {

    private final RefreshTokenService refreshTokenService;
    private final AuthorizationService authorizationService;
    private final RefreshTokenMapper refreshTokenMapper;

    @DgsQuery(field = "activeSessions")
    @PreAuthorize("isAuthenticated()")
    public List<Session> getActiveSessions() {
        return authorizationService.getCurrentUser()
                .map(user -> {
                    List<RefreshTokenModel> sessions = refreshTokenService.getUserActiveSessions(user.getId());
                    return sessions.stream()
                            .map(refreshTokenMapper::toSession)
                            .toList();
                })
                .orElseThrow(() -> new RuntimeException("No active sessions found"));
    }
}

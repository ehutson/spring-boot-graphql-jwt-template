package dev.ehutson.template.security.fingerprint;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.security.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class FingerprintValidator {

    /**
     * Validates the fingerprint of a refresh token against the current request
     *
     * @param storedToken The stored refresh token
     * @param request     The HTTP request
     * @param properties  JWT properties
     * @return true if the fingerprint is valid
     */
    public boolean validateFingerprint(RefreshTokenModel storedToken, HttpServletRequest request, JwtProperties properties) {
        boolean isValid = true;

        if (properties.isFingerprintUserAgent()) {
            isValid = validateUserAgent(storedToken, request);
        }

        if (isValid && properties.isFingerprintIpAddress()) {
            isValid = validateIpAddress(storedToken, request);
        }

        return isValid;
    }

    private boolean validateUserAgent(RefreshTokenModel storedToken, HttpServletRequest request) {
        String currentUserAgent = request.getHeader("User-Agent");
        String storedUserAgent = storedToken.getUserAgent();

        if (currentUserAgent == null || storedUserAgent == null) {
            return false;
        }

        // Normalize user agents for comparison
        String normalizedCurrent = normalizeUserAgent(currentUserAgent);
        String normalizedStored = normalizeUserAgent(storedUserAgent);

        boolean isValid = normalizedCurrent.equals(normalizedStored);

        if (!isValid) {
            log.warn("User agent mismatch for user: {} - Expected hash: {}, Got hash: {}.",
                    storedToken.getUserId(),
                    hashForLogging(normalizedStored),
                    hashForLogging(normalizedCurrent));
        }

        return isValid;
    }

    private boolean validateIpAddress(RefreshTokenModel storedToken, HttpServletRequest request) {
        String currentIp = getClientIp(request);
        String storedIp = storedToken.getIpAddress();

        if (currentIp == null || storedIp == null) {
            return false;
        }

        // For IP validation, we use a more flexible approach
        boolean isValid = isIpAddressMatch(storedIp, currentIp);

        if (!isValid) {
            log.warn("IP address mismatch for user: {} - Expected subnet: {}, Got subnet: {}",
                    storedToken.getUserId(),
                    getSubnetForLogging(storedIp),
                    getSubnetForLogging(currentIp));
        }

        return isValid;
    }

    private String normalizeUserAgent(String userAgent) {
        // Remove the version numbers and minor variations that might change
        // but keep the core browser/platform information
        return userAgent
                .replaceAll("\\d+\\.\\d+(\\.\\d+)?", "X.X") // Replace version numbers
                .replaceAll("\\s+", " ") // Normalize whitespace
                .toLowerCase();
    }

    private boolean isIpAddressMatch(String storedIp, String currentIp) {
        try {
            InetAddress stored = InetAddress.getByName(storedIp);
            InetAddress current = InetAddress.getByName(currentIp);

            // For IPv4, check if they're in the same /24 subnet
            if (stored instanceof java.net.Inet4Address && current instanceof java.net.Inet4Address) {
                return isSameSubnet24(stored.getAddress(), current.getAddress());
            }

            // For IPv6, check if they're in the same /64 subnet
            if (stored instanceof java.net.Inet6Address && current instanceof java.net.Inet6Address) {
                return isSameSubnet64(stored.getAddress(), current.getAddress());
            }

            // If mixed IPv4/IPv6, fall back to exact match
            return stored.equals(current);

        } catch (UnknownHostException e) {
            log.warn("Failed to parse IP addresses for comparison: {} vs {}", storedIp, currentIp);
            return false;
        }
    }

    private boolean isSameSubnet24(byte[] ip1, byte[] ip2) {
        // Check if first 3 octets match (24-bit subnet)
        return ip1[0] == ip2[0] && ip1[1] == ip2[1] && ip1[2] == ip2[2];
    }

    private boolean isSameSubnet64(byte[] ip1, byte[] ip2) {
        // Check if first 8 octets match (64-bit subnet)
        for (int i = 0; i < 8; i++) {
            if (ip1[i] != ip2[i]) {
                return false;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String hashForLogging(String value) {
        // Return a short hash for logging purposes without exposing the actual value
        return Integer.toHexString(value.hashCode());
    }

    private String getSubnetForLogging(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            byte[] bytes = addr.getAddress();

            if (addr instanceof java.net.Inet4Address) {
                // Return the first 3 octets for IPv4
                return String.format("%d.%d.%d.x", bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF);
            } else {
                // Return the first 4 groups for IPv6
                return String.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x::/64",
                        bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF,
                        bytes[4] & 0xFF, bytes[5] & 0xFF, bytes[6] & 0xFF, bytes[7] & 0xFF);
            }
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}

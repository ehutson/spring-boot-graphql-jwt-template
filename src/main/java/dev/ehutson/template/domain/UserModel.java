package dev.ehutson.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.ehutson.template.domain.validation.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"password", "activationKey", "resetKey"})
@ToString(callSuper = true, exclude = {"password", "activationKey", "resetKey"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
//@org.springframework.data.elasticsearch.annotations.Document(indexName = "user")
public class UserModel extends AbstractTrackingModel<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @NotNull(message = "Username is required")
    @Pattern(regexp = ValidationConstants.USERNAME_PATTERN, message = ValidationConstants.USERNAME_MESSAGE)
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH,
            max = ValidationConstants.USERNAME_MAX_LENGTH,
            message = "Username must be between " + ValidationConstants.USERNAME_MIN_LENGTH +
                    " and " + ValidationConstants.USERNAME_MAX_LENGTH + " characters")
    @Indexed(unique = true)
    private String username;

    @JsonIgnore
    @NotNull(message = "Password is required")
    @Size(min = 60, max = 60, message = "Invalid password hash format")
    private String password;

    @Size(max = ValidationConstants.NAME_MAX_LENGTH, message = ValidationConstants.NAME_MESSAGE)
    @Field("first_name")
    private String firstName;

    @Size(max = ValidationConstants.NAME_MAX_LENGTH, message = ValidationConstants.NAME_MESSAGE)
    @Field("last_name")
    private String lastName;

    @NotNull(message = "Email is required")
    @Email(message = ValidationConstants.EMAIL_MESSAGE)
    @Size(min = ValidationConstants.EMAIL_MIN_LENGTH,
            max = ValidationConstants.EMAIL_MAX_LENGTH,
            message = "Email must be between " + ValidationConstants.EMAIL_MIN_LENGTH +
                    " and " + ValidationConstants.EMAIL_MAX_LENGTH + " characters")
    @Indexed(unique = true)
    private String email;

    @Builder.Default
    private boolean activated = false;

    @Size(min = ValidationConstants.LANG_KEY_MIN_LENGTH,
            max = ValidationConstants.LANG_KEY_MAX_LENGTH,
            message = "Language key must be between " + ValidationConstants.LANG_KEY_MIN_LENGTH +
                    " and " + ValidationConstants.LANG_KEY_MAX_LENGTH + " characters")
    @Field("lang_key")
    private String langKey;

    @Size(max = ValidationConstants.TIMEZONE_MAX_LENGTH,
            message = "Timezone cannot exceed " + ValidationConstants.TIMEZONE_MAX_LENGTH + " characters")
    private String timezone;

    @Size(max = ValidationConstants.TOKEN_MAX_LENGTH,
            message = "Activation key cannot exceed " + ValidationConstants.TOKEN_MAX_LENGTH + " characters")
    @Field("activation_key")
    @JsonIgnore
    private String activationKey;

    @Builder.Default
    @Field("activation_date")
    private Instant activationDate = null;

    @Size(max = ValidationConstants.TOKEN_MAX_LENGTH,
            message = "Reset key cannot exceed " + ValidationConstants.TOKEN_MAX_LENGTH + " characters")
    @Field("reset_key")
    @JsonIgnore
    private String resetKey;

    @Builder.Default
    @Field("reset_date")
    private Instant resetDate = null;

    @Builder.Default
    @JsonIgnore
    @DBRef(lazy = true)
    private List<RoleModel> roles = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(username) ? username : fullName + " (" + username + ")";
    }

    // Validation methods using centralized constants

    public boolean hasValidUsername() {
        return ValidationConstants.Utils.isValidUsername(this.username);
    }

    public boolean hasValidEmail() {
        return ValidationConstants.Utils.isValidEmail(this.email);
    }
}

package dev.ehutson.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
//@org.springframework.data.elasticsearch.annotations.Document(indexName = "user")
public class UserModel extends AbstractTrackingModel<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String USERNAME_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+)$";
    ;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @NotNull
    @Pattern(regexp = USERNAME_REGEX)
    @Size(min = 1, max = 50)
    @Indexed(unique = true)
    private String username;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    private String password;

    @Size(max = 50)
    @Field("first_name")
    private String firstName;

    @Size(max = 50)
    @Field("last_name")
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Indexed(unique = true)
    private String email;

    @Builder.Default
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Field("lang_key")
    private String langKey;

    @Size(max = 20)
    @Field("activation_key")
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Field("reset_key")
    @JsonIgnore
    private String resetKey;

    @Builder.Default
    @Field("reset_date")
    private Instant resetDate = null;

    @Builder.Default
    @JsonIgnore
    @DBRef
    private List<RoleModel> roles = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }
}

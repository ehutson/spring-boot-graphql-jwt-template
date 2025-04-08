package dev.ehutson.template.service;

import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserModel registerUser(RegisterInput input, HttpServletRequest request, HttpServletResponse response);

    Boolean verifyEmail(String token);

    Boolean requestPasswordReset(String email);

    Boolean resetPassword(String token, String password);
}

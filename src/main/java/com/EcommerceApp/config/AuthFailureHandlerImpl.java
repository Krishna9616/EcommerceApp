package com.EcommerceApp.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.repository.UserRepository;
import com.EcommerceApp.service.UserService;
import com.EcommerceApp.util.AppConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl implements AuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");

        UserDtls userDtls = userRepository.findByEmail(email);

        String message = "Invalid email or password";

        if (userDtls != null) {

            if (userDtls.getIsEnable()) {

                if (userDtls.getAccountNonLocked()) {

                    if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
                        userService.increaseFailedAttempt(userDtls);
                        message = "Invalid email or password";
                    } else {
                        userService.userAccountLock(userDtls);
                        exception = new LockedException("Your account is locked! Failed attempt 3 times");
                        message = exception.getMessage();
                    }

                } else {

                    if (userService.unlockAccountTimeExpired(userDtls)) {
                        exception = new LockedException("Your account is unlocked! Please try login again");
                        message = exception.getMessage();
                    } else {
                        exception = new LockedException("Your account is locked! Please try after some time");
                        message = exception.getMessage();
                    }
                }

            } else {
                exception = new LockedException("Your account is inactive");
                message = exception.getMessage();
            }

        } else {
            exception = new LockedException("Email & password invalid");
            message = exception.getMessage();
        }


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);

        response.getWriter().write(mapper.writeValueAsString(res));
    }
}

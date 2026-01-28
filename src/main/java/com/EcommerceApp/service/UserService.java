package com.EcommerceApp.service;

import java.util.List;

import com.EcommerceApp.model.UserDtls;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserDtls saveUser(UserDtls user);

    UserDtls saveAdmin(UserDtls user);

    UserDtls getUserByEmail(String email);

    List<UserDtls> getUsers(String role);

    Boolean updateAccountStatus(Integer id, Boolean status);

    void increaseFailedAttempt(UserDtls user);

    void userAccountLock(UserDtls user);

    boolean unlockAccountTimeExpired(UserDtls user);

    void resetAttempt(int userId);

    void updateUserResetToken(String email, String resetToken);

    UserDtls getUserByToken(String token);

    UserDtls updateUser(UserDtls user);

    UserDtls updateUserProfile(UserDtls user, MultipartFile img);

    Boolean existsEmail(String email);


    // UserDtls getUserById(Integer id);


    // Boolean deleteUser(Integer id);
}

package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.ChangePasswordDTO;
import com.patelsamaj.patelsamaj_backend.dto.ForgotPasswordDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.dto.UserProfileDTO;
import org.springframework.data.domain.Page;

public interface UserService {
    UserProfileDTO updateProfile(int userId, UserProfileDTO dto);
    String inviteNewUser(UserProfileDTO dto, int userId);
    Page<UserProfileDTO> getAllUsers(PaginationDTO request);
    Boolean toggleAdminActivation(int userId);
    void resetAndEmailPassword(ForgotPasswordDTO email);
    UserProfileDTO getUserProfile(int adminId);
    void changePassword(int userId, ChangePasswordDTO dto);
}

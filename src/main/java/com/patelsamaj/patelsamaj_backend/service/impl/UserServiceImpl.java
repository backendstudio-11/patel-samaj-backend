package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.ChangePasswordDTO;
import com.patelsamaj.patelsamaj_backend.dto.ForgotPasswordDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.dto.UserProfileDTO;
import com.patelsamaj.patelsamaj_backend.exception.DuplicateResourceFoundException;
import com.patelsamaj.patelsamaj_backend.exception.ResourceNotFoundException;
import com.patelsamaj.patelsamaj_backend.mapper.UserProfileMapper;
import com.patelsamaj.patelsamaj_backend.model.Role;
import com.patelsamaj.patelsamaj_backend.model.User;
import com.patelsamaj.patelsamaj_backend.repository.RoleRepository;
import com.patelsamaj.patelsamaj_backend.repository.UserRepository;
import com.patelsamaj.patelsamaj_backend.service.EmailService;
import com.patelsamaj.patelsamaj_backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileDTO getUserProfile(int userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userProfileMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(int userId, UserProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        userRepository.save(user);
        return userProfileMapper.toDto(user);
    }

    @Override
    @Transactional
    public String inviteNewUser(UserProfileDTO dto, int userId){
        boolean isNotExist = userRepository.findByEmailOrMobileNumber(dto.getEmail(), dto.getMobileNumber()).isEmpty();
        if(!isNotExist){
            throw new DuplicateResourceFoundException("User with same mobile number or email present");
        }

        User user = userProfileMapper.toEntity(dto);
        Role adminRole = roleRepository.findById(1)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        user.setRole(adminRole);
        user.setIsActive(true);
        user.setCreatedBy(userId);
        String randomPlainPassword = saveUserPassword(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName(), dto.getMobileNumber() , randomPlainPassword);
        return "Mail Send Successfully";
    }

    @Override
    public Page<UserProfileDTO> getAllUsers(PaginationDTO request){
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getDirection(), request.getSortBy())
        );
        Page<User> userPage = userRepository.findByRoleIdAndSearch(1, request.getSearch(), pageable);
        return userPage.map(userProfileMapper::toDto);
    }

    @Override
    public Boolean toggleAdminActivation(int userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Boolean isActive = !user.getIsActive();
        user.setIsActive(isActive);
        userRepository.save(user);
        return isActive;
    }

    @Override
    public void changePassword(int userId, ChangePasswordDTO dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password. Cannot change password.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetAndEmailPassword(ForgotPasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));
        String randomPlainPassword = saveUserPassword(user);
        emailService.sendResetPasswordEmail(user.getEmail(), randomPlainPassword, user.getFullName());
    }

    private String saveUserPassword(User user){
        String randomPlainPassword = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String hashedPassword = passwordEncoder.encode(randomPlainPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);

        return randomPlainPassword;
    }
}

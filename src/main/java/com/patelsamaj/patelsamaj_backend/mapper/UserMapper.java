package com.patelsamaj.patelsamaj_backend.mapper;

import com.patelsamaj.patelsamaj_backend.dto.UserProfileDTO;
import com.patelsamaj.patelsamaj_backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<UserProfileDTO, User> {

    @Override
    @Mapping(source = "role.roleName", target = "roleName")
    UserProfileDTO toDto(User user);
}

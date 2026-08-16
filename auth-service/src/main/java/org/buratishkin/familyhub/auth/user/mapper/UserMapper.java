package org.buratishkin.familyhub.auth.user.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.auth.user.UserEntity;
import org.buratishkin.familyhub.auth.user.dto.SignUpReq;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<SignUpReq, UserEntity>{
    @Override
    public UserEntity toEntity(SignUpReq source) {
        UserEntity user = new UserEntity();
        user.setUsername(source.getUsername());
        user.setEmail(source.getEmail());

        return user;
    }

    public UserEntity toEntity(SignUpReq source, String hashPassword){
        UserEntity user = toEntity(source);
        user.setPassword(hashPassword);
        return user;
    }
}

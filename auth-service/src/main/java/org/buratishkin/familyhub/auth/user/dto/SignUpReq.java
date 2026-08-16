package org.buratishkin.familyhub.auth.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SignUpReq {
    public SignUpReq(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }

    String username;
    String password;
    String email;
}

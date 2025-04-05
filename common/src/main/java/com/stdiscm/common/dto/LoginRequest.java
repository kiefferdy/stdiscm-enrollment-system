package com.stdiscm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest implements Serializable {
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}

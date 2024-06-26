package com.umc.StudyFlexBE.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class LoginDto {


    @JsonProperty
    @NotNull
    private String email;


    @JsonProperty
    @NotNull
    private String password;


}

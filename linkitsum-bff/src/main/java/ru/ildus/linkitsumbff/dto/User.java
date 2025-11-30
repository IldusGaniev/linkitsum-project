package ru.ildus.linkitsumbff.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User {
    private Long id;
    private String user_id;
    private String given_name;
    private String family_name;
    private String email;
    private Boolean email_verified;
}

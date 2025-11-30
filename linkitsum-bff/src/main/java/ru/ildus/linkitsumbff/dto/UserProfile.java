package ru.ildus.linkitsumbff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

// полные данные профиля пользователя, для отображения в frontend
public class UserProfile {

    private String givenName;
    private String familyName;
    private String address;
    private String email;
    private String id;

    // можно добавлять любые поля, которые вам необходимы (из keycloak или другого Auth Server)

}

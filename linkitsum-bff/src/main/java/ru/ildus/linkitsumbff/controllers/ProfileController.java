package ru.ildus.linkitsumbff.controllers;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.ildus.linkitsumbff.dto.User;
import ru.ildus.linkitsumbff.dto.UserProfile;
import ru.ildus.linkitsumbff.utils.ParserUtil;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/profile") // базовый URI
public class ProfileController {
    // можно также использовать WebClient вместо RestTemplate, если нужны асинхронные запросы
    private static final RestTemplate restTemplate = new RestTemplate(); // для выполнения веб запросов на KeyCloak
    //private static final WebClient;

    @Value("${usersresourceserver.url}")
    private String userResourceServerURL;


    // класс-утилита для работы с куками
    private final ParserUtil parserUtil;


    @Autowired
    public ProfileController(ParserUtil parserUtil) { // внедряем объекты
        this.parserUtil = parserUtil;
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR; }
        });
    }

    // получение подробных данных пользователя (профайл)
    // все данные берем из ранее полученного idToken
    // запроса в RS не делаем, т.к. бизнес-данные тут не запрашиваются
    @GetMapping("/my")
    public ResponseEntity<UserProfile> profile(@CookieValue(value = "IT", required = false) String iToken) {

        if (iToken == null) {
            return new ResponseEntity<>(
                    HttpStatus.FORBIDDEN);
        }
        // все данные пользователя (профайл)
        String payloadPart = iToken.split("\\.")[1]; // берем значение раздела payload в формате Base64
        String payloadStr = new String(Base64.getUrlDecoder().decode(payloadPart)); // декодируем из Base64 в обычный текст JSON
        JSONObject newPayload = new JSONObject(payloadStr); // формируем удобный формат JSON - из него теперь можно получать любе поля

        UserProfile userProfile = new UserProfile(
                parserUtil.getPayloadValueFromSomePayload(newPayload, "given_name"),
                parserUtil.getPayloadValueFromSomePayload(newPayload, "family_name"),
                "",
                //getPayloadValueFromSomePayload(newPayload, "address"),
                parserUtil.getPayloadValueFromSomePayload(newPayload, "email"),
                parserUtil.getPayloadValueFromSomePayload(newPayload, "sid")
        );

        return ResponseEntity.ok(userProfile);

    }


//    private boolean getUserFromResourseServer(String uuid, String aToken){
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setBearerAuth(aToken);
//
//        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
//        mapForm.add("uuid", uuid);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);
//
//        ResponseEntity<User> response = restTemplate.exchange(
//                userResourceServerURL + "uuid",
//                HttpMethod.POST,
//                request,
//                User.class);
//
////       System.out.println(response.getBody().getEmail());
//        if (response.getStatusCode() == HttpStatus.OK) {
//            return (response.getBody() != null ? true : false);
//        } else
//            return false;
//    }
//
//    private ResponseEntity<User> addUser(User user, String aToken){
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(aToken);
//
//        JSONObject jo = new JSONObject(user);
//        HttpEntity<String> entity = new HttpEntity<>(jo.toString(), headers);
//
//        return  restTemplate.exchange(
//                userResourceServerURL + "add",
//                HttpMethod.POST,
//                entity,
//                User.class);
//    }

}

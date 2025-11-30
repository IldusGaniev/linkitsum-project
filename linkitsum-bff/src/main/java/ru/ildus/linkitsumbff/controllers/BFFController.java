package ru.ildus.linkitsumbff.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.ildus.linkitsumbff.dto.Group;
import ru.ildus.linkitsumbff.dto.User;
import ru.ildus.linkitsumbff.dto.UserProfile;
import ru.ildus.linkitsumbff.utils.CookieUtils;
import ru.ildus.linkitsumbff.utils.ParserUtil;
//import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff") // базовый URI
public class BFFController {

    // можно также использовать WebClient вместо RestTemplate, если нужны асинхронные запросы
    private static final RestTemplate restTemplate = new RestTemplate(); // для выполнения веб запросов на KeyCloak
    //private static final WebClient;
    // значения из настроек (значения внедряются автоматически)
    @Value("${keycloak.secret}")
    private String clientSecret;

    @Value("${resourceserver.url}")
    private String resourceServerURL;

    @Value("${usersresourceserver.url}")
    private String userResourceServerURL;

    @Value("${keycloak.url}")
    private String keyCloakURI;

    @Value("${client.url}")
    private String clientURL;

    @Value("${keycloak.clientid}")
    private String clientId;

    @Value("${keycloak.granttype.code}")
    private String grantTypeCode;

    @Value("${keycloak.granttype.refresh}")
    private String grantTypeRefresh;

    // класс-утилита для работы с куками
    private final CookieUtils cookieUtils;
    private final ParserUtil parserUtil;

    // значения куков
    private int accessTokenDuration;
    private int refreshTokenDuration;

    private String accessToken;
    private String idToken;
    private String refreshToken;

    // используется, чтобы получать любые значения пользователя из JSON
    private JSONObject payload;

    // идентификатор пользователя из KC, может использоваться для поиска
    private String userId;


    @Autowired
    public BFFController(CookieUtils cookieUtils, ParserUtil parserUtil) { // внедряем объекты
        this.cookieUtils = cookieUtils;
        this.parserUtil = parserUtil;
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR; }
        });
    }

    // получение всех токенов и запись в куки
    // сами токены сохраняться в браузере не будут, а только будут передаваться в куках
    // таким образом к ним не будет доступа из кода браузера (защита от XSS атак)
    @PostMapping("/token")
    public ResponseEntity<String> token(@RequestBody String code) {// получаем auth code, чтобы обменять его на токены

        // 1. обменять auth code на токены
        // 2. сохранить токены в защищенные куки
        System.out.println("TOKEN");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // параметры запроса
        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
        mapForm.add("grant_type", grantTypeCode);
        mapForm.add("client_id", clientId);
        mapForm.add("client_secret", clientSecret); // используем статичный секрет (можем его хранить безопасно), вместо code verifier из PKCE
        mapForm.add("code", code);

        // В случае работы клиента через BFF - этот redirect_uri может быть любым, т.к. мы не открываем окно вручную, а значит не будет автоматического перехода в redirect_uri
        // Клиент получает ответ в объекте ResponseEntity
        // НО! Значение все равно передавать нужно, без этого grant type не сработает и будет ошибка.
        // Значение обязательно должно быть с адресом и портом клиента, например https://localhost:8080  иначе будет ошибка Incorrect redirect_uri, потому что изначально запрос на авторизацию выполнялся именно с адреса клиента
        mapForm.add("redirect_uri", "https://linkitsum.ru:443");

        // добавляем в запрос заголовки и параметры
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);

        // выполняем запрос
        ResponseEntity<String> response = restTemplate.exchange(keyCloakURI + "/token", HttpMethod.POST, request, String.class);
        // мы получаем JSON в виде текста
        System.out.println(response.getBody());
        System.out.println(response.getStatusCode());
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        parseResponse(response); // получить все нужные поля ответа KC
        String uuid = parserUtil.getUserUUIDFromIT(idToken);

        if(!getUserFromResourseServer(uuid, accessToken)){
            User user = parserUtil.getUserFromIT(idToken);
            ResponseEntity<?> responseToAddUser = addUser(user, accessToken);
        }


        // считать данные из JSON и записать в куки
        HttpHeaders responseHeaders = cookieUtils.createCookies(
                accessToken,
                refreshToken,
                idToken,
                accessTokenDuration,
                refreshTokenDuration
        );
        // отправляем клиенту данные пользователя (и jwt-кук в заголовке Set-Cookie)
        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    // удаление сессий пользователя внутри KeyCloak и также зануление всех куков
    @GetMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = "IT", required = false) String iToken) {
        // 1. закрыть сессии в KeyCloak для данного пользователя
        // 2. занулить куки в браузере
        if (iToken == null) {
            return new ResponseEntity<>(
                    HttpStatus.FORBIDDEN);
        }

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(keyCloakURI + "/logout")
                .queryParam("id_token_hint", iToken)
                .queryParam("client_id", clientId)
                .build()
                .toUriString();

        ResponseEntity<String> response= restTemplate.getForEntity(
                urlTemplate, // шаблон GET запроса - туда будут подставляться значения из params
                String.class
        );

        // занулить значения и сроки годности всех куков (тогда браузер их удалит автоматически)
        HttpHeaders responseHeaders = cookieUtils.clearCookies();
        accessToken = "";
        this.idToken = "";
        refreshToken = "";
        accessTokenDuration = 0;
        refreshTokenDuration = 0;

        // отправляем браузеру ответ с пустыми куками для их удаления (зануления), т.к. пользователь вышел из системы
        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    // получение новых токенов на основе старого RT
    @GetMapping("/exchange")
    public ResponseEntity<String> exchangeRefreshToken(@CookieValue(value = "RT", required = false) String oldRefreshToken) {
        if (oldRefreshToken == null) {
            System.out.println("MISSING REFRESH TOKEN");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        System.out.println("REFRESH TOKEN");
        System.out.println(oldRefreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // параметры запроса (в формате ключ-значение)
        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
        mapForm.add("grant_type", grantTypeRefresh);
        mapForm.add("client_id", clientId);
        mapForm.add("client_secret", clientSecret);
        mapForm.add("refresh_token", oldRefreshToken);

        // собираем запрос для выполнения
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);

        // выполняем запрос (можно применять разные методы, не только exchange)
        ResponseEntity<String> response = restTemplate.exchange(keyCloakURI + "/token", HttpMethod.POST, request, String.class);

        parseResponse(response); // получить все нужные поля ответа KC
        // создаем куки для их записи в браузер (frontend)
        HttpHeaders responseHeaders = cookieUtils.createCookies(
                accessToken,
                refreshToken,
                idToken,
                accessTokenDuration,
                refreshTokenDuration
        );

        // отправляем клиенту ответ со всеми куками (которые запишутся в браузер автоматически)
        // значения куков с новыми токенами перезапишутся в браузер
        return ResponseEntity.ok().headers(responseHeaders).build();

    }

    // получение нужных полей из ответа KC
    private void parseResponse(ResponseEntity<String> response) {

        // парсер JSON
        ObjectMapper mapper = new ObjectMapper();

        // сначала нужно получить корневой элемент JSON
        try {
            JsonNode root = mapper.readTree(response.getBody());

            // получаем значения токенов из корневого элемента JSON в формате Base64
            accessToken = root.get("access_token").asText();
            idToken = root.get("id_token").asText();
            refreshToken = root.get("refresh_token").asText();

            // Сроки действия для токенов берем также из JSON
            // Куки станут неактивные в то же время, как выйдет срок действия токенов в KeyCloak
            accessTokenDuration = root.get("expires_in").asInt();
            refreshTokenDuration = root.get("refresh_expires_in").asInt();

            // все данные пользователя (профайл)
            String payloadPart = idToken.split("\\.")[1]; // берем значение раздела payload в формате Base64
            String payloadStr = new String(Base64.getUrlDecoder().decode(payloadPart)); // декодируем из Base64 в обычный текст JSON
            payload = new JSONObject(payloadStr); // формируем удобный формат JSON - из него теперь можно получать любе поля

        } catch (JsonProcessingException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getUserFromResourseServer(String uuid, String aToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(aToken);

        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
        mapForm.add("uuid", uuid);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);

        ResponseEntity<User> response = restTemplate.exchange(
                userResourceServerURL + "uuid",
                HttpMethod.POST,
                request,
                User.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (response.getBody() != null ? true : false);
        } else
            return false;
    }

    private ResponseEntity<?> addUser(User user, String aToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aToken);

        JSONObject jo = new JSONObject(user);
        HttpEntity<String> entity = new HttpEntity<>(jo.toString(), headers);

        ResponseEntity<?> response = restTemplate.exchange(
                userResourceServerURL + "add",
                HttpMethod.POST,
                entity,
                User.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

}

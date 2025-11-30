package ru.ildus.linkitsumbff.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.ildus.linkitsumbff.dto.Interval;
import ru.ildus.linkitsumbff.dto.SplitTaskTemplate;
import ru.ildus.linkitsumbff.utils.ParserUtil;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/splittask") // базовый URI
public class SplitTaskTemplController {
    private static final RestTemplate restTemplate = new RestTemplate(); // для выполнения веб запросов на KeyCloak
    @Value("${resourceserver.url}")
    private String resourceServerURL;

    // класс-утилита для работы с куками
    private final ParserUtil parserUtil;

    public SplitTaskTemplController(ParserUtil parserUtil) {
        this.parserUtil = parserUtil;
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR; }
        });
    }

    @PostMapping("/all")
    public ResponseEntity<?> getAllSplitTasks(@CookieValue(value = "AT", required = false) String acToken, @CookieValue(value = "IT", required = false) String iToken) {
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(acToken);

        String uuid = parserUtil.getUserUUIDFromIT(iToken);

        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
        mapForm.add("uuid", uuid);
        // специальный контейнер для передачи объекта внутри запроса
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);
        //HttpEntity<String> request = new HttpEntity<>("", headers);
//        System.out.println();

        // получение бизнес-данных пользователя (ответ обернется в DataResult)

        ResponseEntity<?> response = restTemplate.exchange(
                resourceServerURL + "splittask/all",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                });

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

}

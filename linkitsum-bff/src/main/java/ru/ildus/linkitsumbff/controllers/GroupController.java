package ru.ildus.linkitsumbff.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.ildus.linkitsumbff.dto.Group;
import ru.ildus.linkitsumbff.dto.GroupWithTasks;
import ru.ildus.linkitsumbff.dto.NewGroupPaylod;
import ru.ildus.linkitsumbff.dto.UpdateGroupPaylod;
import ru.ildus.linkitsumbff.errors.MyErrorHandler;
import ru.ildus.linkitsumbff.utils.ParserUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/group") // базовый URI
public class GroupController {

    // можно также использовать WebClient вместо RestTemplate, если нужны асинхронные запросы
    private static final RestTemplate restTemplate = new RestTemplate(); // для выполнения веб запросов на KeyCloak
    //private static final WebClient;


    @Value("${resourceserver.url}")
    private String resourceServerURL;

    // класс-утилита для работы с куками
    private final ParserUtil parserUtil;


    @Autowired
    public GroupController(ParserUtil parserUtil) { // внедряем объекты
        this.parserUtil = parserUtil;
        restTemplate.setErrorHandler(new MyErrorHandler());
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
//            public boolean hasError(ClientHttpResponse response) throws IOException {
//                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
//                return statusCode.series() == HttpStatus.Series.SERVER_ERROR; }
//        });
    }

    @PostMapping("/all")
    public ResponseEntity<List<Group>> getAllGroups(@CookieValue(value = "AT", required = false) String acToken, @CookieValue(value = "IT", required = false) String iToken) {
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
        ResponseEntity<List<Group>> response = restTemplate.exchange(
                resourceServerURL + "group/all",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<Group>>(){});

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    @PostMapping("/allwithtasks")
    public ResponseEntity<List<GroupWithTasks>> getAllGroupsWithTasks(@CookieValue(value = "AT", required = false) String acToken, @CookieValue(value = "IT", required = false) String iToken) {
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
        ResponseEntity<List<GroupWithTasks>> response = restTemplate.exchange(
                resourceServerURL + "group/allwithtasks",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<GroupWithTasks>>(){});

//        return response;
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    @PostMapping("/add")
    public ResponseEntity<?> saveGroup(
            @CookieValue(value = "AT", required = false) String acToken,
            @RequestBody NewGroupPaylod group) {
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String uuid = parserUtil.getUserUUIDFromIT(acToken);

        if(group.name() == null)
            return new ResponseEntity<>("Название группы не задано", HttpStatus.BAD_REQUEST);
        if(group.color() == null)
            return new ResponseEntity<>("Цвет группы не задан", HttpStatus.BAD_REQUEST);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);
        Group g = new Group(null, group.name(), group.color(), uuid);
        JSONObject jsonGroup = new JSONObject(g);

        HttpEntity<String> entity = new HttpEntity<>(jsonGroup.toString(), headers);
        String errorMessage="";
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    resourceServerURL + "group/add",
                    HttpMethod.POST,
                    entity,
                    Group.class);
            System.out.println(response.getBody());
            return  ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/update")
    public ResponseEntity<?> updateGroup(
            @CookieValue(value = "AT", required = false) String acToken,
            @RequestBody UpdateGroupPaylod group){
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String uuid = parserUtil.getUserUUIDFromIT(acToken);

        if(group.name() == null)
            return new ResponseEntity<>("Название группы не задано", HttpStatus.BAD_REQUEST);
        if(group.color() == null)
            return new ResponseEntity<>("Цвет группы не задан", HttpStatus.BAD_REQUEST);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);
        Group g = new Group(group.id(), group.name(), group.color(), uuid);
        JSONObject jsonGroup = new JSONObject(g);

        HttpEntity<String> entity = new HttpEntity<>(jsonGroup.toString(), headers);
        String errorMessage="";
        try {
            ResponseEntity<?> response =   restTemplate.exchange(
                resourceServerURL + "group/update",
                HttpMethod.POST,
                entity,
                Group.class);
            return  ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteGroup(
            @CookieValue(value = "AT", required = false) String acToken,
            @PathVariable Long id
    ){
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                resourceServerURL + "group/delete/{id}",
                HttpMethod.DELETE,
                entity,
                String.class,
                id // Deletes resource with ID 456
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/{id}")
    public ResponseEntity getGroupById(
            @CookieValue(value = "AT", required = false) String acToken,
            @PathVariable Long id
    ){
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Group> response = restTemplate.exchange(
                resourceServerURL + "group/{id}",
                HttpMethod.POST,
                entity,
                Group.class,
                id
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}

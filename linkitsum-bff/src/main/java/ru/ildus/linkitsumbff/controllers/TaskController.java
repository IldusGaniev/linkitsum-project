package ru.ildus.linkitsumbff.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestTemplate;
import ru.ildus.linkitsumbff.dto.Group;
import ru.ildus.linkitsumbff.dto.NewTaskPayload;
import ru.ildus.linkitsumbff.dto.Task;
import ru.ildus.linkitsumbff.utils.ParserUtil;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/task") // базовый URI
public class TaskController {
    // можно также использовать WebClient вместо RestTemplate, если нужны асинхронные запросы
    private static final RestTemplate restTemplate = new RestTemplate(); // для выполнения веб запросов на KeyCloak

    @Value("${resourceserver.url}")
    private String resourceServerURL;

    // класс-утилита для работы с куками
    private final ParserUtil parserUtil;

    @Autowired
    public TaskController(ParserUtil parserUtil) { // внедряем объекты
        this.parserUtil = parserUtil;
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR; }
        });
    }

    @PostMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks(@CookieValue(value = "AT", required = false) String acToken, @CookieValue(value = "IT", required = false) String iToken) {
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
        ResponseEntity<List<Task>> response = restTemplate.exchange(
                resourceServerURL + "task/all",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<Task>>(){});

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveTask(
            @CookieValue(value = "AT", required = false) String acToken,
            @RequestBody Task task) throws JsonProcessingException {
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String uuid = parserUtil.getUserUUIDFromIT(acToken);

        if(task.getTitle() == null)
            return new ResponseEntity<>("Название задачи не задано", HttpStatus.BAD_REQUEST);
        if(task.getWith_the_end())
            if (task.getEnd_time() == null)
                return new ResponseEntity<>("Не задана Дата завершения", HttpStatus.BAD_REQUEST);



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        task.setUser_id(uuid);
        ObjectMapper objectMapper = new ObjectMapper();

        String objecInJSON = objectMapper.writeValueAsString(task);
        HttpEntity<String> entity = new HttpEntity<>(objecInJSON, headers);

        ResponseEntity<?> response = restTemplate.exchange(
                resourceServerURL + "task/add",
                HttpMethod.POST,
                entity,
                Task.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteGroup(
            @CookieValue(value = "AT", required = false) String acToken,
            @PathVariable Long id
    ){
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                resourceServerURL + "task/delete/{id}",
                HttpMethod.DELETE,
                entity,
                String.class,
                id // Deletes resource with ID 456
        );
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @CookieValue(value = "AT", required = false) String acToken,
            @PathVariable Long id
    ){
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> response = restTemplate.exchange(
                resourceServerURL + "task/{id}",
                HttpMethod.POST,
                entity,
                Task.class,
                id
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateGroup(
            @CookieValue(value = "AT", required = false) String acToken,
            @RequestBody Task task) throws JsonProcessingException {
        if (acToken == null) return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String uuid = parserUtil.getUserUUIDFromIT(acToken);

        if(task.getTitle() == null)
            return new ResponseEntity<>("Название задачи не задано", HttpStatus.BAD_REQUEST);
        if(task.getWith_the_end())
            if (task.getEnd_time() == null)
                return new ResponseEntity<>("Не задана Дата завершения", HttpStatus.BAD_REQUEST);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(acToken);

        task.setUser_id(uuid);
        ObjectMapper objectMapper = new ObjectMapper();

        String objecInJSON = objectMapper.writeValueAsString(task);
        HttpEntity<String> entity = new HttpEntity<>(objecInJSON, headers);

//        HttpEntity<String> entity = new HttpEntity<>(task, headers);

        ResponseEntity<?> response = restTemplate.exchange(
                resourceServerURL + "task/update",
                HttpMethod.POST,
                entity,
                Task.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}

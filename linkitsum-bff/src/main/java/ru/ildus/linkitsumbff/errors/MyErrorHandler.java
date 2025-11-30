package ru.ildus.linkitsumbff.errors;


import java.io.IOException;
import java.net.URI;

import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.*;

public class MyErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @SneakyThrows
    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError()) {
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ClassNotFoundException();
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // You can read the response body for more details if needed
                String errorMessage = new String(response.getBody().readAllBytes());
                throw new BadRequestException(errorMessage);
            }
        } else if (response.getStatusCode().is5xxServerError()) {
            // Handle server errors, e.g., throw a generic server error exception
            throw new RuntimeException("Server error: " + response.getStatusCode());
        }

    }
}

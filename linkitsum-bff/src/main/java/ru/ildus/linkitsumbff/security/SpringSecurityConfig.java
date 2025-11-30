package ru.ildus.linkitsumbff.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration // данный класс будет считан как конфиг для spring контейнера
@EnableWebSecurity // включает механизм защиты адресов, которые настраиваются в SecurityFilterChain
//@EnableWebMvc
// для BFF не нунжно исп-е БД в нашем варианте, поэтому отключаем автоконфигурацию связи с БД
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})

public class SpringSecurityConfig {

    @Value("${client.url}")
    private String clientURL; // клиентский URL


    // создается спец. бин, который отвечает за настройки запросов по http (метод вызывается автоматически) Spring контейнером
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // все сетевые настройки
        http.authorizeHttpRequests(requests -> requests
                //.requestMatchers("/bff/**").permitAll()
                .anyRequest().permitAll()
        )
                .csrf(csrf->csrf.disable())
                .cors(Customizer.withDefaults())
//                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
//                .requiresChannel(channel -> channel // только защищенный канал
//                    .anyRequest().requiresSecure())
                .sessionManagement((session) -> session // отключаем куки для сессии
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );


        return http.build();
    }


    // все эти настройки обязательны для корректного сохранения куков в браузере
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // без этого куки могут не сохраняться
        configuration.setAllowedOrigins(Arrays.asList("https://linkitsum.ru"));
//        configuration.setAllowedOrigins(Arrays.asList("https://linkitsum.ru","https://linkitsum.ru:443", "https://linkitsum.ru:8922", "https://linkitsum.ru:8902"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // CORS настройка, чтобы разрешить запросы из других серверов, например из клиентского приложения
    // без этой настройки не будет работать BFF, потому что все входящие запросы будут просто блокироваться
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.
//                        addMapping("/**"). // для всех URL
//                        allowedOrigins(clientURL). // с каких адресов разрешать запросы (можно указывать через запятую)
////                        allowedOrigins(clientURL). // с каких адресов разрешать запросы (можно указывать через запятую)
//                        allowCredentials(true). // разрешить отправлять куки для межсайтового запроса
//                        allowedHeaders("*"). // разрешить все заголовки - без этой настройки в некоторых браузерах может не работать
//                        allowedMethods("*"); // все методы разрешены (GET,POST и пр.) - без этой настройки CORS не будет работать!
//            }
//        };
//    }

}

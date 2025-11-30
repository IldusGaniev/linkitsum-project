package ru.ildus.linkitsumbff.utils;

import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;



/*

Утилита для работы с куками

Напоминание: кук jwt создается на сервере и управляется только сервером (создается, удаляется) - "server-side cookie"

На клиенте этот кук нельзя считать с помощью JavaScript (т.к. стоит флаг httpOnly) - для безопасности и защиты от XSS атак.

Смотрите более подробные комментарии в методе создания кука.

Также, обязательно канал должен быть HTTPS, чтобы нельзя было дешифровать данные запросов между клиентом (браузером) и сервером


 */


@Component // добавится в Spring контейнер и будет доступен для любого Spring компонента (контроллеры, сервисы и пр.)
public class CookieUtils {
    public static final String IDTOKEN_COOKIE_KEY = "IT";
    public static final String REFRESHTOKEN_COOKIE_KEY = "RT";
    public static final String ACCESSTOKEN_COOKIE_KEY = "AT";

    @Value("${cookie.domain}")
    private String cookieDomain; // тот домен, который будет прописываться в сервер-куке при его создании на бекенде

    // создает server-side cookie со значением jwt. Важно: этот кук сможет считывать только сервер, клиент не сможет с помощью JS или другого клиентского кода (сделано для безопасности)
    public HttpCookie createCookie(String name, String value, int durationInSeconds) { // jwt - значение для кука
        return ResponseCookie
                // настройки кука
                .from(name, value) // название и значение кука
                .maxAge(durationInSeconds) // 86400 сек = 1 сутки
                .sameSite(SameSiteCookies.STRICT.getValue()) // запрет на отправку кука на сервер, если выполняется межсайтовый запрос (доп. защита от CSRF атак) - кук будет отправляться только если пользователь сам набрал URL в адресной строке
                .httpOnly(true) // кук будет доступен для считывания только на сервере (на клиенте НЕ будет доступен с помощью JavaScript - тем самым защищаемся от XSS атак)
                .secure(true) // кук будет передаваться браузером на backend только если канал будет защищен (https)
                .domain(cookieDomain) // для какого домена действует кук (перед отправкой запроса на backend - браузер "смотрит" на какой домен он отправляется - и если совпадает со значением из кука - тогда прикрепляет кук к запросу)
                .path("/") // кук будет доступен для всех URL

                // создание объекта
                .build();

        /* примечание: все настройки кука (domain, path и пр.) - влияют на то, будет ли браузер отправлять их при запросе.

            Браузер сверяет URL запроса (который набрали в адресной строке или любой ajax запрос с формы) с параметрами кука.
            И если есть хотя бы одно несовпадение (например domain или path) - кук отправлен не будет.

          */
    }


    // зануляет (удаляет) кук
    public HttpCookie deleteCookie(String name) {
        return ResponseCookie.
                from(name, "") // пустое значение
                .maxAge(0) // кук с нулевым сроком действия браузер удалит автоматически
                .sameSite(SameSiteCookies.STRICT.getValue()) // запрет на отправку кука, если запрос пришел со стороннего сайта (доп. защита от CSRF атак) - кук будет отправляться только если пользователь набрал URL в адресной строке
                .httpOnly(true) // кук будет доступен для считывания только на сервере (на клиенте НЕ будет доступен с помощью JavaScript - тем самым защищаемся от XSS атак)
                .secure(true) // кук будет передаваться браузером на backend только если канал будет защищен (https)
                .domain(cookieDomain)
                .path("/") // кук будет доступен на любой странице
                .build();

    }

    // создание куков для response
    public HttpHeaders createCookies(String AT, String RT, String IT, int ATDuration, int RTDuration) {

        // создаем куки, которые браузер будет отправлять автоматически на BFF при каждом запросе
        HttpCookie accessTokenCookie = createCookie(ACCESSTOKEN_COOKIE_KEY, AT, ATDuration);
        HttpCookie refreshTokenCookie = createCookie(REFRESHTOKEN_COOKIE_KEY, RT, RTDuration);
        HttpCookie idTokenCookie = createCookie(IDTOKEN_COOKIE_KEY, IT, ATDuration); // задаем такой же срок, что и AT

        // чтобы браузер применил куки к бразуеру - указываем их в заголовке Set-Cookie в response
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        responseHeaders.add(HttpHeaders.SET_COOKIE, idTokenCookie.toString());

        return responseHeaders;
    }


    // зануляет все куки, чтобы браузер их удалил у себя
    public HttpHeaders clearCookies() {
        // зануляем куки, которые отправляем обратно клиенту в response, тогда браузер автоматически удалит их
        HttpCookie accessTokenCookie = deleteCookie(ACCESSTOKEN_COOKIE_KEY);
        HttpCookie refreshTokenCookie = deleteCookie(REFRESHTOKEN_COOKIE_KEY);
        HttpCookie idTokenCookie = deleteCookie(IDTOKEN_COOKIE_KEY);

        // чтобы браузер применил куки к бразуеру - указываем их в заголовке Set-Cookie в response
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        responseHeaders.add(HttpHeaders.SET_COOKIE, idTokenCookie.toString());
        return responseHeaders;
    }

}

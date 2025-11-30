package ru.ildus.linkitsumbff.configs;

//import javax.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import javax.servlet.http.Cookie;
//import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        System.out.println("INTERCEPTORS IS WORKING");


        String paths[] = request.getRequestURI().split("/");
        String path = paths[1];
        System.out.println(request.getRequestURI());
        System.out.println(request.getRequestURL());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemotePort());
        System.out.println(request.getRemoteAddr());
        System.out.println(request.getServerPort());
        System.out.println(request.getServerName());
        System.out.println(request.getContextPath());
//        System.out.println(request.getCookies().toString());
        System.out.println(request.getLocalAddr());
        System.out.println(request.getLocalPort());
        System.out.println(request.getLocalName());
        System.out.println(request.getHeader("Origin"));

//        String method = request.getMethod();
//        if (method.equals("OPTIONS"))
//            return true;
//        if (path.equals("group") || path.equals("task") || path.equals("interval") || path.equals("splittask") || path.equals("profile") ){
//            String authToken = getCookieValueByName(request, "AT");
//            String infoToken = getCookieValueByName(request, "IT");.................................................
//            String refreshToken = getCookieValueByName(request, "RT");
//            if (authToken == null && infoToken == null && refreshToken != null) {
//                response.setStatus(403);
//                return false;
////                response.setStatus();
//            }
//            return true;
//        }
//        System.out.println(authToken);
//        if (authToken == null)
//            return false;
//        String authToken = getCookieValueByName(request, "AT");
//        String infoToken = getCookieValueByName(request, "IT");
//        String refreshToken = getCookieValueByName(request, "RT");


        return true;
    }

    public String getCookieValueByName(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null; // Cookie not found
    }
}

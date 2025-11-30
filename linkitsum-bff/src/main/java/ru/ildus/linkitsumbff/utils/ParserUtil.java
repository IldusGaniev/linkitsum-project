package ru.ildus.linkitsumbff.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ru.ildus.linkitsumbff.dto.Group;
import ru.ildus.linkitsumbff.dto.Task;
import ru.ildus.linkitsumbff.dto.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

@Component
public class ParserUtil {

    public User getUserFromIT(String iToken){
        String payloadPart = iToken.split("\\.")[1]; // берем значение раздела payload в формате Base64
        String payloadStr = new String(Base64.getUrlDecoder().decode(payloadPart)); // декодируем из Base64 в обычный текст JSON
        JSONObject payloadNew = new JSONObject(payloadStr);
        Long id = null;
        String user_id = getPayloadValueFromSomePayload(payloadNew, "sub");
        String given_name = getPayloadValueFromSomePayload(payloadNew, "given_name");
        String family_name = getPayloadValueFromSomePayload(payloadNew, "family_name");
        String email = getPayloadValueFromSomePayload(payloadNew, "email");
        Boolean email_verified = Boolean.valueOf(getPayloadValueFromSomePayload(payloadNew, "sub"));
        return new User(id, user_id, given_name, family_name, email, email_verified);
    }

    public String getPayloadValueFromSomePayload(JSONObject somePayloadString, String claim) {
        try {
            return somePayloadString.getString(claim);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserUUIDFromIT(String iToken){
        String payloadPart = iToken.split("\\.")[1]; // берем значение раздела payload в формате Base64
        String payloadStr = new String(Base64.getUrlDecoder().decode(payloadPart)); // декодируем из Base64 в обычный текст JSON
        JSONObject payloadNew = new JSONObject(payloadStr);
        return getPayloadValueFromSomePayload(payloadNew, "sub");
    }

    public Group getGroupFromJSON(String group){
        JSONObject jo = new JSONObject(group);
        Long id = jo.getLong("id");;
        String name = jo.getString("name");
        String color = jo.getString("color");
        String user_id = jo.getString("user_id");

        return new Group(id, name, color, user_id);
    }

//    public Task getTaskFronJSON(String task){
//        SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
//        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//
//        JSONObject jo = new JSONObject(task);
//        Long id =( jo.isNull("id") ? null : jo.getLong("id"));
//        String title = jo.getString("title");
//        Boolean completed = jo.getBoolean("completed");
//        Boolean with_the_end =jo.getBoolean("completed");
//        //LocalDate end_time = LocalDate.parse(jo.getString("end_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//        Date end_time = null;
//        Date creating_time = null;
//        try {
//            if (!jo.isNull("end_time"))
//                end_time = formatterDate.parse(jo.getString("end_time"));
//            if (!jo.isNull("creating_time"))
//                creating_time = formatterDateTime.parse(jo.getString("creating_time"));
//        } catch (ParseException e) {
////            throw new RuntimeException(e);
//        }
//
//
//        String user_id =( jo.isNull("user_id") ? null : jo.getString("user_id"));
//
//        JSONObject group =( jo.isNull("group") ? null : jo.getJSONObject("group"));
//        Group gr = getGroupFromJSON(group.toString());
//
//        return new Task(id,
//                title,
//                gr,
//                completed,
//                with_the_end,
//                end_time,
//                user_id,
//                creating_time);
//    }
}

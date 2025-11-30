package ru.ildus.linkitsumbff.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Interval implements Serializable {
    private Long id;
    private Task task;
    private String user_id;
    private IntrvalType type;
    private int period;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date finished_time;
    private short day;
    private short week_day;
    private short month;
    private short year;
    private short start_time_in_min;
    private short end_time_in_min;
    private short is_two_day;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return Objects.equals(id, interval.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

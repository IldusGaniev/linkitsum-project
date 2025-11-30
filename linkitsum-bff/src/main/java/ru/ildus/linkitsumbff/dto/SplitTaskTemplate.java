package ru.ildus.linkitsumbff.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SplitTaskTemplate {

    private Long id;
    private Task task;
    private Interval interval;
    private Date week_start;
    private Date split_task_start;
    private Date split_task_end;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SplitTaskTemplate that = (SplitTaskTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package ru.ildus.linkitsumbff.dto;

import java.util.Date;
import java.util.List;

public record NewTaskPayload(
        Long id,
        String title,
        Group group,
        Boolean completed, // 1 = true, 0 = false
        Boolean with_the_end, // 1 = true, 0 = false
        Date end_time,
        String user_id,
        Date creating_time,
        List<IntervalPayload> intervals
) {
}

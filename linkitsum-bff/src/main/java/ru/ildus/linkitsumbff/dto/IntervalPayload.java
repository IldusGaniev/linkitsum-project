package ru.ildus.linkitsumbff.dto;

import java.util.Date;

public record IntervalPayload(
         Long id,
         Task task,
         String user_id,
         IntrvalType type,
         int period,
         Date finished_time,
         short day,
         short week_day,
         short month,
         short year,
         short start_time_in_min,
         short end_time_in_min,
         short is_two_day
) {
}

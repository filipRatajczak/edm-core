package com.edm.edmcore.controller;

import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.model.ScheduleRequest;
import com.edm.edmcore.repository.ScheduleRepository;
import com.edm.edmcore.schedule.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleGenerator scheduleGenerator;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/api/v1/schedule")
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleRequest scheduleRequest) {

        Schedule schedule = scheduleGenerator.generateSchedule(scheduleRequest);

        return ResponseEntity.ok(schedule);

    }

    @GetMapping("/api/v1/schedule")
    public ResponseEntity<Set<Schedule>> getSchedule(@RequestParam LocalDate from, @RequestParam LocalDate to) {

        List<LocalDate> period = createDateObjectsBetween(from, to);

        Set<Schedule> schedules = new LinkedHashSet<>();
        period.forEach(day -> schedules.add(scheduleRepository.findFirstByDay(day)));

        return ResponseEntity.ok(schedules);
    }

    private static List<LocalDate> createDateObjectsBetween(LocalDate from, LocalDate to) {
        List<LocalDate> dateObjects = new LinkedList<>();

        dateObjects.add(from);
        dateObjects.add(to);

        long interval = DAYS.between(from, to);

        for (int i = 1; i < interval; i++) {
            LocalDate date = from.plusDays(i);
            dateObjects.add(date);
        }

        dateObjects.sort(LocalDate::compareTo);

        return dateObjects;
    }


}


package com.edm.edmcore.controller;

import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.repository.ScheduleRepository;
import com.edm.edmcore.schedule.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleGenerator scheduleGenerator;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/api/v1/schedule")
    public ResponseEntity<List<Schedule>> createSchedule(@RequestParam LocalDate from, @RequestParam LocalDate to) {

        List<Schedule> schedules = scheduleGenerator.generateSchedule(from, to);

        return ResponseEntity.ok(schedules);

    }

    @GetMapping("/api/v1/schedule")
    public ResponseEntity<List<Schedule>> getSchedule(@RequestParam LocalDate from, @RequestParam LocalDate to) {

        List<LocalDate> period = createDateObjectsBetween(from, to);

        List<Schedule> schedules = new ArrayList<>();
        period.forEach(day -> schedules.add(scheduleRepository.findFirstByDay(day)));

        return ResponseEntity.ok(schedules);
    }

    private static List<LocalDate> createDateObjectsBetween(LocalDate from, LocalDate to) {
        List<LocalDate> dateObjects = new ArrayList<>();

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


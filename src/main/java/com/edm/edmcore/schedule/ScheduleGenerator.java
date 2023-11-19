package com.edm.edmcore.schedule;

import com.edm.edmcore.client.DispositionClient;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.util.sorter.ScheduleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerator {
    private final DispositionClient dispositionClient;
    @Value("${edm.schedule.min-employees}")
    private Integer minEmployeeRule;
    @Value("${edm.schedule.max-employees}")
    private Integer maxEmployeeRule;
    private final ScheduleUtil scheduleUtil;


    public Schedule generate() {

        LinkedHashSet<DispositionDto> allDisposition = new LinkedHashSet<>(dispositionClient.getAllDisposition(LocalDate.now(), LocalDate.now()));

        Set<DispositionDto> xd = scheduleUtil.checkScheduleCoverage(allDisposition);

        log.info(xd.toString());

        return new Schedule();

    }
}

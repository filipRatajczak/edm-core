package com.edm.edmcore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class ScheduleRequest {

    private String organizationCode;
    private LocalDate date;
    private List<SchedulePeriod> schedulePeriods;

}

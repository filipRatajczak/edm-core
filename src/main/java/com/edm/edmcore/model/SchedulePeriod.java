package com.edm.edmcore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SchedulePeriod {

    private LocalTime from;
    private LocalTime to;
    private int minEmployees;
    private int maxEmployees;

}

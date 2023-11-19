package com.edm.edmcore.util;

import com.edm.edmcore.model.DispositionDto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class DateUtil {


    public static LocalTime parseStringToLocalTime(String time) {
        int timeO1Hours = Integer.parseInt(time.split(":")[0]);
        int timeO1Minutes = Integer.parseInt(time.split(":")[1]);
        return LocalTime.of(timeO1Hours, timeO1Minutes);
    }

    public static BigDecimal calculateHourlySurplus(DispositionDto dispositionDto, LocalTime time) {

        LocalTime stop = parseStringToLocalTime(dispositionDto.getStop());

        return BigDecimal.valueOf(stop.until(time, ChronoUnit.MINUTES)).divide(BigDecimal.valueOf(60l));

    }

}

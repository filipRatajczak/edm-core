package com.edm.edmcore;

import com.edm.edmcore.schedule.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Init implements InitializingBean {

    private final ScheduleGenerator scheduleGenerator;

    @Override
    public void afterPropertiesSet() {
        scheduleGenerator.generate();
    }
}

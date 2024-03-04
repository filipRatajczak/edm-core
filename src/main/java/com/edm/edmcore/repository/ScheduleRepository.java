package com.edm.edmcore.repository;

import com.edm.edmcore.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, UUID> {

    Schedule findFirstByDay(LocalDate day);
    List<Schedule> findByDay(LocalDate day);


    void deleteAllByDay(LocalDate day);

}
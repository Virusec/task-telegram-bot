package com.example.tasktelegrambot.repository;



import com.example.tasktelegrambot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findAllByDate(LocalDateTime localDateTime);
}

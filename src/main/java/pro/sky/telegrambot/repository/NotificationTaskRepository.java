package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.model.jpa.entities.NotificationTaskEntity;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTaskEntity, Integer> {

    List<NotificationTaskEntity> findAllByChatIdAndSendStatusFalse(String chatId);
    List<NotificationTaskEntity> findAllByChatIdAndSendStatusTrue(String chatId);

}

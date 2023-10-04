package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.jpa.entities.NotificationTaskEntity;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;

@Component
public class NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSender.class);

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendReminder() {
        notificationTaskRepository.findAllByDateBeforeAndSendStatusFalse(LocalDateTime.now()).forEach(task -> {
            SendMessage sendMessage = new SendMessage(Long.parseLong(task.getChatId()), task.getTask());
            telegramBot.execute(sendMessage);
            NotificationTaskEntity taskForUpdate = notificationTaskRepository.findById(task.getId()).get();
            taskForUpdate.setSendStatus(true);
            notificationTaskRepository.save(taskForUpdate);
            LOG.info("Notification was sent on task " + task.getTask());
        });
    }
}

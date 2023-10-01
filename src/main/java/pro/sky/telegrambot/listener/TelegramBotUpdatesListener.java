package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.jpa.entities.NotificationTaskEntity;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})\\s+(.*)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long chatId;
            try {
                if (!(update.callbackQuery() == null)) {
                    chatId = update.callbackQuery().message().chat().id();
                } else if (!(update.editedMessage() == null)) {
                    chatId = update.editedMessage().chat().id();
                } else {
                    chatId = update.message().chat().id();
                }
            } catch (NullPointerException e) {
                logger.error("Chat_id is null");
                return;
            }
            String message;
            try {
                if (!(update.callbackQuery() == null)) {
                    message = update.callbackQuery().data();
                } else if (!(update.editedMessage() == null)) {
                    message = update.editedMessage().text();
                } else {
                    message = update.message().text();
                }
            } catch (NullPointerException e) {
                telegramBot.execute(new SendMessage(chatId, "Ой, кажется Вы не указали задачу"));
                return;
            }
            Matcher matcher = PATTERN.matcher(message);
            if (message.equals("/start")) {
                String welcomeMessage = "Привет! Этот бот умеет сохранять Ваши задачи и отправлять напоминания о них в назначенное время.\n" +
                        "Чтобы добавить задачу, отправьте ее мне в формате: 01.10.2023 22:30 Играть в FFXVI";
                SendMessage sendMessage = new SendMessage(chatId, welcomeMessage).replyMarkup(prepareInlineKeyBoard());
                telegramBot.execute(sendMessage);
            } else if (message.equals("/active_tasks")) {
                List<NotificationTaskEntity> unfinishedTasks = notificationTaskRepository.findAllByChatIdAndSendStatusFalse(chatId.toString());
                SendMessage sendMessage = new SendMessage(chatId, printTasks(unfinishedTasks));
                telegramBot.execute(sendMessage);
            } else if (message.equals("/completed_tasks")) {
                List<NotificationTaskEntity> finishedTasks = notificationTaskRepository.findAllByChatIdAndSendStatusTrue(chatId.toString());
                SendMessage sendMessage = new SendMessage(chatId, printTasks(finishedTasks));
                telegramBot.execute(sendMessage);
            } else if (!matcher.matches() || LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER).isBefore(LocalDateTime.now()) ) {
                String incorrectTaskMessage = "Простите, но у меня пока нет машины времени;)\nПроверьте дату задачи.";
                SendMessage sendMessage = new SendMessage(chatId, incorrectTaskMessage);
                telegramBot.execute(sendMessage);
            } else if (matcher.matches()) {
                NotificationTaskEntity notificationTask = new NotificationTaskEntity();
                notificationTask.setTask(matcher.group(2));
                notificationTask.setChatId(chatId.toString());
                try {
                    notificationTask.setDate(LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER));
                } catch (DateTimeParseException e) {
                    SendMessage sendMessage = new SendMessage(chatId, "Ой, кажется Вы живете по другому календарю. Проверьте дату и время");
                    telegramBot.execute(sendMessage);
                    return;
                }
                notificationTask.setSendStatus(false);
                notificationTaskRepository.save(notificationTask);
                String taskSavedMessage = "Сохранил Вашу задачу на будущее, обязательно о ней напомню.";
                SendMessage sendMessage = new SendMessage(chatId, taskSavedMessage);
                telegramBot.execute(sendMessage);
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private static String printTasks(List<NotificationTaskEntity> tasks) {
        StringBuilder sb = new StringBuilder();
        for (NotificationTaskEntity task : tasks) {
            sb.append(task).append("\n");
        }
        return sb.toString();
    }

    public static InlineKeyboardMarkup prepareInlineKeyBoard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.addRow(new InlineKeyboardButton("Предстоящие задачи").callbackData("/active_tasks"));
        keyboardMarkup.addRow(new InlineKeyboardButton("Выполненные задачи").callbackData("/completed_tasks"));
        return keyboardMarkup;
    }

}

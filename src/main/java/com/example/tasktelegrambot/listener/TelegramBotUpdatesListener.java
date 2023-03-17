package com.example.tasktelegrambot.listener;

import com.example.tasktelegrambot.model.NotificationTask;
import com.example.tasktelegrambot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    final String WELCOME_MESSAGE_TEXT = "Добро пожаловать в чат бот! Чем могу помочь?";
    final String ERROR_MESSAGE_TEXT = "Извините, я работаю только с текстовыми сообщениями!";
    final String REGEX_PATTERN = "([0-9.:\\s]{16})(\\s)([\\W0-9+]+)";
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile(REGEX_PATTERN);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        logger.info("Processing update: {}", update);
                        Message msg = update.message();
                        Long chatId = msg.chat().id();
                        String text = msg.text();
                        if ("/start".equals(msg.text())) {
                            sendMessage(chatId, WELCOME_MESSAGE_TEXT);
                        } else if (msg.photo() != null || msg.audio() != null || msg.video() != null || msg.sticker() != null) {
                            sendMessage(chatId, ERROR_MESSAGE_TEXT);
                        } else if (text != null) {
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                LocalDateTime dateTime = parse(matcher.group(1));
                                if (Objects.isNull(dateTime)) {
                                    logger.warn("Incorrect format data/time");
                                    sendMessage(chatId, "Введен не корректный формат даты/времени");
                                } else {
                                    String task = matcher.group(3);
                                    NotificationTask notificationTask = new NotificationTask();
                                    notificationTask.setChat_id(chatId);
                                    notificationTask.setDate(dateTime);
                                    notificationTask.setNotification(task);
                                    notificationTaskService.save(notificationTask);
                                    logger.info("Task: {} successfully saved", task);
                                    sendMessage(chatId, "Задача успешно запланирована");
                                }
                            } else {
                                logger.warn("Incorrect format message");
                                sendMessage(chatId, "Некорректный формат сообщения");
                            }
                        }
                        //                    logger.info("Create pattern {}", REGEX_PATTERN);
                        //                    String date;
                        //                    String item;
                        //                    if (matcher.matches()) {
                        //                        date = matcher.group(1);
                        //                        item = matcher.group(3);
                        //                        logger.info("Parse message {} to {} and {}", msg.text(), date, item);
                        //                        NotificationTask task = new NotificationTask();
                        //                        logger.info("Create object task");
                        //                        task.setChat_id(chatId);
                        //                        task.setNotification(item);
                        //                        task.setDate(parse(Objects.requireNonNull(date),
                        //                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                        //                        logger.info("Set data into task");
                        //                        notificationTaskService.save(task);
                        //                        logger.info("Task: {} successfully saved", task);
                        //                    }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String text) {
        logger.info("Chat chatId : {}", chatId);
        SendMessage sendMessage = new SendMessage(chatId, text);
        logger.info("t-bot is sending sendMessage: {}", text);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error: {}", sendResponse.description());
        }
    }

    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}

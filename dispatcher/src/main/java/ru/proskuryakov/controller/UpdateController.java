package ru.proskuryakov.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.proskuryakov.service.UpdateProducer;
import ru.proskuryakov.utils.MessageUtils;

import static ru.proskuryakov.model.RabbitQueue.*;


@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    @Autowired
    private UpdateProducer updateProducer;
    @Autowired
    private MessageUtils messageUtils;


    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }
        if(update.getMessage() != null) {
            distributeMessagesByType(update);
        } else {
            log.error("Received unsupported type " + update);
        }
    }

    private void distributeMessagesByType(Update update) {
        var message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        } else if (message.getDocument() != null) {
            processDocMessage(update);
        } else if (message.getPhoto() != null) {
            processPhotoMessage(update);
        } else {
            setUnsupportedTypeMessage(update);
        }
    }
    private void setFileReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен! Обрабатывается... ");
        setView(sendMessage);
    }
    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void setUnsupportedTypeMessage(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Unsupported type of message");
        setView(sendMessage);
    }



    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE,update);
        setFileReceivedView(update);
    }



    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE,update);
        setFileReceivedView(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE,update);
    }
}

package ru.proskuryakov.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.proskuryakov.controller.UpdateController;

public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);

}

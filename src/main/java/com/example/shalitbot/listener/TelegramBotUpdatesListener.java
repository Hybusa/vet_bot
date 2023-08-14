package com.example.shalitbot.listener;

import com.example.shalitbot.enums.BotState;
import com.example.shalitbot.model.Drug;
import com.example.shalitbot.model.User;
import com.example.shalitbot.service.DrugsService;
import com.example.shalitbot.service.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeDefault;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final DrugsService drugsService;
    private final UserService userService;
    private InlineKeyboardMarkup TYPES_INLINE_MARKUP;
    private InlineKeyboardMarkup MAIN_CHOICE_MARKUP;
    private Map<Long, BotState> stateByChatIdMap;


    public TelegramBotUpdatesListener(TelegramBot telegramBot, DrugsService drugsService, UserService userService) {
        this.telegramBot = telegramBot;
        this.drugsService = drugsService;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        stateByChatIdMap = userService.getMapOfStates();
        MAIN_CHOICE_MARKUP = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                new InlineKeyboardButton("Search by name")
                        .callbackData("search_by_name")},
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Search by type of medicine")
                                .callbackData("search_by_type")});
        TYPES_INLINE_MARKUP = createVerticalInlineKeyboardMarkupFromStringList(drugsService.getTypeGroupsAsList());
        telegramBot.setUpdatesListener(this);

        BotCommand[] commandsArr = new BotCommand[]{
                new BotCommand("/start", "Restart the bot")
        };
        SetMyCommands commands = new SetMyCommands(commandsArr);
        commands.scope(new BotCommandScopeDefault());
        telegramBot.execute(commands);
    }

    private InlineKeyboardMarkup createVerticalInlineKeyboardMarkupFromStringList(List<String> typeGroupsAsList) {
        List<InlineKeyboardButton[]> inlineKeyboardButtonsList = new ArrayList<>();
        for (String s : typeGroupsAsList) {
            inlineKeyboardButtonsList.add(
                    new InlineKeyboardButton[]{
                            new InlineKeyboardButton(StringUtils.capitalize(s))
                                    .callbackData("typeGroup_" + s)});
        }
        InlineKeyboardButton[][] inlineKeyboardButtonsArr =
                new InlineKeyboardButton[inlineKeyboardButtonsList.size()][1];
        inlineKeyboardButtonsList.toArray(inlineKeyboardButtonsArr);

        return new InlineKeyboardMarkup(inlineKeyboardButtonsArr);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update {}", update);
            Long chatId;
            if (update.callbackQuery() != null) {
                chatId = update.callbackQuery().from().id();
                String data = update.callbackQuery().data();
                if (data.startsWith("search")) {
                    switch (data) {
                        case "search_by_name":
                            searchByNameReply(update);
                            break;
                        case "search_by_type":
                            searchByTypeMessage(update);
                            break;
                        default:
                            sendMessage(chatId, "Something went wrong");
                    }
                } else if (data.startsWith("typeGroup")) {
                    typeGroupReply(update);
                } else if (data.startsWith("drug")) {
                    drugChoiceMessage(update);
                }
            } else if (update.message().text() != null) {
                chatId = update.message().chat().id();
                if (update.message().text().equals("/start")) {
                    startBot(update);
                    logger.info("bot started");
                } else if (stateByChatIdMap.get(chatId) == BotState.NAME) {
                    searchByName(update);
                } else {
                    SendResponse response = sendMessage(chatId, "Something went wrong try again");
                    if (response.isOk()) {
                        logger.info("Message: {} sent", response.message());
                    } else {
                        logger.error("Error sending. Code: " + response.errorCode());
                    }
                }
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void typeGroupReply(Update update) {
        String data = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();
        int messageId = update.callbackQuery().message().messageId();
        String drugTypeGroup = data.substring(data.indexOf('_') + 1);

        List<Drug> drugList = drugsService.findAllByTypeGroup(drugTypeGroup);
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupFromDrugList(drugList);
        telegramBot.execute(new EditMessageText(chatId, messageId, drugTypeGroup).replyMarkup(inlineKeyboardMarkup));
    }

    private void drugChoiceMessage(Update update) {
        String data = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();
        int messageId = update.callbackQuery().message().messageId();

        String drugName = data.substring(data.indexOf('_') + 1);
        Optional<Drug> optDrug = drugsService.findByName(drugName);
        if (optDrug.isPresent()) {
            Drug drug = optDrug.get();
            String message = getMessageStringFromDrug(drug);
            telegramBot.execute(new EditMessageText(chatId, messageId, message));
            SendResponse response = sendMainChoiceMessage(chatId);

            if (response.isOk()) {
                logger.info("Message: {} sent", response.message());
                stateByChatIdMap.put(chatId, BotState.IDLE);
            } else {
                logger.error("Error sending. Code: " + response.errorCode());
            }
        }
    }

    private SendResponse sendMainChoiceMessage(Long chatId) {
        return sendChoiceMessage(chatId, "Choose option:", MAIN_CHOICE_MARKUP);
    }

    private void searchByName(Update update) {
        Long chatId = update.message().chat().id();
        List<Drug> drugs = drugsService.findByNameLike(update.message().text());
        if (drugs.isEmpty()) {
            sendMessage(chatId, "Nothing found try again");
        } else if (drugs.size() == 1) {
            Drug drug = drugs.get(0);
            String message = getMessageStringFromDrug(drug);
            sendMessage(chatId, message);
            sendMainChoiceMessage(chatId);
        } else {
            InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupFromDrugList(drugs);

            sendChoiceMessage(chatId, "Choose the drug you were looking for", inlineKeyboardMarkup);
        }
    }

    @NotNull
    private static InlineKeyboardMarkup getInlineKeyboardMarkupFromDrugList(List<Drug> drugs) {
        List<InlineKeyboardButton[]> inlineKeyboardButtonsList = new ArrayList<>();
        for (Drug drug : drugs) {
            inlineKeyboardButtonsList.add(
                    new InlineKeyboardButton[]{
                            new InlineKeyboardButton(drug.getNameLat() + "(" + drug.getNameRus() + ")")
                                    .callbackData("drug_" + drug.getNameLat())});
        }
        InlineKeyboardButton[][] inlineKeyboardButtonsArr =
                new InlineKeyboardButton[inlineKeyboardButtonsList.size()][1];
        inlineKeyboardButtonsList.toArray(inlineKeyboardButtonsArr);

        return new InlineKeyboardMarkup(inlineKeyboardButtonsArr);
    }


    private String getMessageStringFromDrug(Drug drug) {
        return String.format("%s (%s) \n" +
                        "For cats:\n %s\n" +
                        "For Dogs:\n %s\n",
                StringUtils.capitalize(drug.getNameLat()),
                StringUtils.capitalize(drug.getNameRus()),
                drug.getDosageForCats(),
                drug.getDosageForDogs());
    }

    private void searchByTypeMessage(Update update) {
        String message = "Choose type group of the medicine";
        Long chatId = update.callbackQuery().from().id();
        int messageId = update.callbackQuery().message().messageId();

        stateByChatIdMap.put(chatId, BotState.TYPE);


        telegramBot.execute(new EditMessageText(chatId, messageId, message).replyMarkup(TYPES_INLINE_MARKUP));

    }

    private SendResponse sendChoiceMessage(Long chatId, String message, InlineKeyboardMarkup inlineKeyboardMarkup) {
        return telegramBot.execute(new SendMessage(chatId, message).replyMarkup(inlineKeyboardMarkup));
    }

    private SendResponse sendMessage(Long chatId, String message) {
        return telegramBot.execute(new SendMessage(chatId, message));
    }

    private void searchByNameReply(Update update) {
        Long chatId = update.callbackQuery().from().id();
        int messageId = update.callbackQuery().message().messageId();

        stateByChatIdMap.put(chatId, BotState.NAME);

        telegramBot.execute(new EditMessageText(chatId, messageId, "Send the name of the medicine"));
    }

    private void startBot(Update update) {
        Long chatId = update.message().chat().id();
        User user = new User(chatId);
        user.setTypeChoice(BotState.IDLE.name());
        user.setName(update.message().chat().firstName());
        if (!stateByChatIdMap.containsKey(chatId)) {
            userService.saveUser(user);
        }

        SendResponse response = telegramBot.execute(new SendMessage(chatId, "Choose type of search:")
                .replyMarkup(MAIN_CHOICE_MARKUP));
        if (response.isOk()) {
            logger.info("Message: {} sent", response.message());
            stateByChatIdMap.put(chatId, BotState.IDLE);
        } else {
            logger.error("Error sending. Code: " + response.errorCode());
        }
    }

    @Scheduled(cron = "0 5 * * * *")
    private void updateDB() {
        userService.updateUserDbFromMap(stateByChatIdMap);
        TYPES_INLINE_MARKUP = createVerticalInlineKeyboardMarkupFromStringList(drugsService.getTypeGroupsAsList());
    }
}

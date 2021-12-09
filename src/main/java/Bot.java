import org.json.simple.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot{

    private static final String START_MESSAGE = "Что умеет этот бот:\n" +
            "1. Искать книги по названию и ключевым словам\n" +
            "2. Искать документы по названию\n" +
            "Отправлять нужные вам файлы";

    private static final String WARNING_MESSAGE = "Выберите категорию поиска";

    private HashMap<Long, Boolean> Chatid_nextstep = new HashMap<Long, Boolean>();
    public static final Boolean BOOK_NEXT_STEP = true;
    public static final Boolean DOCUMENT_NEXT_STEP = false;

    public static void main(String[] args) {
        // Инициализируем апи
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            botapi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "RTU_TechLib_Bot";
    }

    @Override
    // При получении сообщения
    public void onUpdateReceived(Update upd) {
        if (upd.hasMessage()) {
            Message msg = upd.getMessage(); // Это нам понадобится
            System.out.println(msg.getText());
            String txt = msg.getText();
            if (txt.equals("/start")) {
                sendMsg(msg, START_MESSAGE);
            }
            else if (txt.equals("Книги")) {
                Chatid_nextstep.put(msg.getChatId(), BOOK_NEXT_STEP);
            }
            else if(txt.equals("Документы")) {
                Chatid_nextstep.put(msg.getChatId(), DOCUMENT_NEXT_STEP);
            }
            else {
//                Searcher searcher = new Searcher();
                if (Chatid_nextstep.containsKey(msg.getChatId())) {
                    if (Chatid_nextstep.get(msg.getChatId()).equals(BOOK_NEXT_STEP)){
                        System.out.println("Получен поисковый запрос на КНИГИ: " + txt);
                        JSONObject jsonObj = Searcher.searchItems(txt, Searcher.BOOKS);
                        if ( ! jsonObj.isEmpty() ){
                            ArrayList books = Searcher.parseBooks(jsonObj);
                            sendMsg(msg, String.format("Результаты по запросу: %s", txt), books);
                        }
                        else {
                            sendMsg(msg, String.format("По запросу %s ничего не найдено", txt));
                        }
                    }
                    else if (Chatid_nextstep.get(msg.getChatId()).equals(DOCUMENT_NEXT_STEP)){
                        System.out.println("Получен поисковый запрос на ДОКУМЕНТЫ: " + txt);
                        JSONObject jsonObj = Searcher.searchItems(txt, Searcher.DOCS);
                        if ( ! jsonObj.isEmpty() ){
                            ArrayList docs = Searcher.parseDocuments(jsonObj);
//                            System.out.println(docs.toString());
                            sendMsg(msg, String.format("Результаты по запросу: %s", txt), docs);
                        }
                        else {
                            sendMsg(msg, String.format("По запросу %s ничего не найдено", txt));
                        }
                    }
                    else {
                        sendMsg(msg, WARNING_MESSAGE);
                    }
                }


            }
        }
        if(upd.hasCallbackQuery()) {
            CallbackQuery callback = upd.getCallbackQuery();
            System.out.println("Запрос на скачивание: " + callback.getData());
//            answerCallbackQuery(callback.getId(), callback.getData());
            Long chatId = callback.getMessage().getChatId();
            JSONObject jsonObj = Searcher.getItemById(callback.getData());
            String name = jsonObj.keySet().toArray()[0].toString();
            String path = jsonObj.get(name).toString();
            URL fullURL = null;
            try {
                fullURL = path.startsWith("/media") ? new URL("https://tacoburrito.pythonanywhere.com" + path) : new URL(path);
                if (path.startsWith("/media")){
                    fullURL = new URL("https://tacoburrito.pythonanywhere.com" + path);
                    InputStream fileToSendIS = fullURL.openStream();
                    sendFile(chatId, fileToSendIS, name);
                }
                else {
                    fullURL = new URL(path);
                    sendMsg(callback.getMessage(), fullURL.toString());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public String getBotToken() {
        return ""; // TOKEN HERE
    }

    // метод отправки простого текстового мообщения
    private void sendMsg(Message msg, String text, ArrayList...items) {
        SendMessage outMess = new SendMessage();
        outMess.setChatId(msg.getChatId());
        outMess.setText(text);
        setButtons(outMess);
        if (items.length > 0){
            outMess.setReplyMarkup(createInline(items[0]));
        }
        try {
            execute(outMess);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    private void sendChoises(Message msg, String text, ArrayList items){
        SendMessage outMess = new SendMessage();
        outMess.setChatId(msg.getChatId());
        outMess.setText(text);
        outMess.setReplyMarkup(createInline(items));
        try{
            execute(outMess);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    public synchronized void answerCallbackQuery(String callbackId, String message, File file) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(false);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendFile(Long chatId, InputStream is, String name){
        SendDocument outMess = new SendDocument();
        outMess.setChatId(chatId);
        outMess.setNewDocument(name, is);
        try{
            sendDocument(outMess);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    public synchronized void setButtons(SendMessage sendMessage) {
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Книги"));
        keyboardFirstRow.add(new KeyboardButton("Документы"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        // устанваливаем список клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private InlineKeyboardMarkup createInline(ArrayList<Item> items) {
        int count = 1;
        List<List<InlineKeyboardButton>> keyboard= new ArrayList<List<InlineKeyboardButton>>();
        for (Item item : items){
            List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
            buttons.add(new InlineKeyboardButton().setText(count + ") " + item.getName()).setCallbackData(item.getId()));
            keyboard.add(buttons);
            count++;
        }
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(keyboard);
        return markupKeyboard;
    }


}

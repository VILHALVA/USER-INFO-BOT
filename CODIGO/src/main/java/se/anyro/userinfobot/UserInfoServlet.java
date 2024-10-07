package se.anyro.userinfobot;

import static se.anyro.userinfobot.BuildVars.OWNER;
import static se.anyro.userinfobot.BuildVars.TOKEN;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.anyro.tgbotapi.TgBotApi;
import se.anyro.tgbotapi.types.Chat;
import se.anyro.tgbotapi.types.Message;
import se.anyro.tgbotapi.types.Update;
import se.anyro.tgbotapi.types.User;

@SuppressWarnings("serial")
public class UserInfoServlet extends HttpServlet {

    private TgBotApi api;
    private long lastFrom;
    private long lastId;


    public UserInfoServlet() {
        super();
        api = new TgBotApi(TOKEN, OWNER);
        api.debug("BOT EM EXECUÇÃO!");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setStatus(200);

        try {
            Update update = api.parseFromWebhook(req.getReader());
            Message message = update.message;
            if (message == null) {
                return;
            }
            User user = message.forward_from;
            if (user == null) {
                if (!message.isForwardedFromChannel()) {
                    user = message.from;
                }
            }

            StringBuilder builder = new StringBuilder();
            if (user != null) {
                if (lastFrom == message.from.id && lastId == user.id) {
                    return; 
                }
                if (user.username != null) {
                    builder.append("@").append(user.username).append('\n');
                }
                builder.append("SEU ID: ").append(user.id).append('\n');
                builder.append("SEU NOME: ").append(user.first_name).append('\n');
                if (user.last_name != null) {
                    builder.append("SEU SOBRENOME: ").append(user.last_name).append('\n');
                }
                if (user.language_code != null) {
                    builder.append("LINGUAGEM: ").append(user.language_code).append('\n');
                }
                lastFrom = message.from.id;
                lastId = user.id;
            } 
            else if (message.isForwardedFromChannel()) {
                Chat channel = message.forward_from_chat;
                if (channel.username != null) {
                    builder.append("@").append(channel.username).append('\n');
                }
                builder.append("ID: ").append(channel.id).append('\n');
                builder.append("TITULO: ").append(channel.title).append('\n');

                if (message.forward_from_message_id != 0 && message.forward_from_chat.username != null) {
                    builder.append("https://t.me/" + message.forward_from_chat.username + "/"
                            + message.forward_from_message_id);
                }
            }
            api.sendMessage(message.from.id, builder.toString(), null, true, 0, null);
        } catch (Exception e) {
            api.debug(e);
        }
    }
}

package de.presti.ree6.webinterface.utils;

import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.*;
import de.presti.ree6.webinterface.bot.version.BotVersion;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getToken() {
        if (BotWorker.getVersion() == BotVersion.DEVELOPMENT_BUILD) {
            return Server.getInstance().getConfig().getConfiguration().getString("discord.bot.tokens.dev");
        } else if (BotWorker.getVersion() == BotVersion.RELEASE || BotWorker.getVersion() == BotVersion.PRE_RELEASE) {
            return Server.getInstance().getConfig().getConfiguration().getString("discord.bot.tokens.rel");
        } else {
            return "error";
        }
    }

}
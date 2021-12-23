package de.presti.ree6.webinterface.utils;

import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.*;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getToken() {
        if (BotInfo.version == BotVersion.DEV) {
            return Server.getInstance().getConfig().getConfig().getString("discord.bot.tokens.dev");
        } else if (BotInfo.version == BotVersion.PUBLIC || BotInfo.version == BotVersion.PRERELASE) {
            return Server.getInstance().getConfig().getConfig().getString("discord.bot.tokens.rel");
        } else {
            return "error";
        }
    }

}
package moe.xetanai.chattr;

import moe.xetanai.chattr.commands.*;
import moe.xetanai.chattr.listeners.CommandListener;
import moe.xetanai.chattr.listeners.PMRelay;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class Chattr {
	private static final Logger logger = LoggerFactory.getLogger("Chattr");

	public static final long DEVID = 155490847494897664L;
	public static JDA API;

	public static void main(String[] args) {
		logger.info("Starting Chattr.");

		JSONObject config = null;

		try {
			String configfile = FileUtils.readFileToString(new File("config.json"), "UTF-8");

			config = new JSONObject(configfile);

			Chattr.API = new JDABuilder(AccountType.BOT).setToken(config.getString("token"))
					.addEventListener(new CommandListener())
					.addEventListener(new PMRelay())
					.buildAsync();
		} catch (IOException | JSONException err) {
			logger.error("Failed to load config.", err);
			System.exit(1);
		} catch (LoginException | RateLimitedException err) {
			logger.error("Failed to login.", err);
			System.exit(2);
		}

		registerCommands();
	}

	private static void registerCommands() {
		new CmdSearch().registerCommand();
		new Help().registerCommand();
		new Report().registerCommand();
		new Reveal().registerCommand();
		new StopSearch().registerCommand();
	}
}

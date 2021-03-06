/*
 *     Copyright 2017-2018 Julia L Rogers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package moe.xetanai.chattr;

import moe.xetanai.chattr.commands.*;
import moe.xetanai.chattr.listeners.CommandListener;
import moe.xetanai.chattr.listeners.PMRelay;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
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
	public static JSONObject RAWCFG = null;

	public static void main(String[] args) {
		logger.info("Starting Chattr.");
		if (ChattrInfo.isDevVersion()) {
			logger.info("DEVELOPMENT MODE");
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		}

		try {
			String configfile = FileUtils.readFileToString(new File("config.json"), "UTF-8");

			RAWCFG = new JSONObject(configfile);

			Chattr.API = new JDABuilder(AccountType.BOT).setToken(RAWCFG.getString("token"))
					.addEventListener(new CommandListener())
					.addEventListener(new PMRelay())
					.setStatus(OnlineStatus.IDLE)
					.buildAsync();
		} catch (IOException | JSONException err) {
			logger.error("Failed to load config.", err);
			System.exit(1);
		} catch (LoginException | RateLimitedException err) {
			logger.error("Failed to login.", err);
			System.exit(2);
		}

		registerCommands();
		Matchmaker.start();
		Chattr.API.getPresence().setStatus(OnlineStatus.ONLINE);
	}

	private static void registerCommands() {
		new CmdSearch().registerCommand();
		new Help().registerCommand();
		new Report().registerCommand();
		new Reveal().registerCommand();
		new StopSearch().registerCommand();
		new Info().registerCommand();
		new Invite().registerCommand();
	}
}

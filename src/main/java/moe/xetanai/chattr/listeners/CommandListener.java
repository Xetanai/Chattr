package moe.xetanai.chattr.listeners;

import ch.qos.logback.classic.Logger;
import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.entities.Command;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class CommandListener extends ListenerAdapter {
	private static final Logger log = (Logger) LoggerFactory.getLogger(CommandListener.class);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String contents = event.getMessage().getContentRaw();
		User user = event.getAuthor();
		String[] args = contents.split(" ");

		if (user.isBot()) {return;} // Ignore bots
		if (args.length == 1) {return;} // Ignore commandless mentions
		if (!args[0].equals(Chattr.API.getSelfUser().getAsMention())) {return;} // Ignore non mention prefixes
		String keyword = args[1];

		for (Command c : Command.REGISTRY) {
			if (c.getKeyword().equals(keyword)) { // Find the command with this keyword, if any.
				c.invoke(event);
			}
		}
	}
}

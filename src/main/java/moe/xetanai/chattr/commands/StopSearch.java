package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Command;
import moe.xetanai.chattr.entities.Conversation;
import moe.xetanai.chattr.entities.Search;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StopSearch extends Command {
	public StopSearch() {
		super("stop", "Cancel a search or end a conversation.", CmdFlag.CONV_AGNOSTIC, CmdFlag.FREE_USE);
	}

	@Override
	public MessageBuilder run(MessageReceivedEvent e, MessageBuilder res) {
		User author = e.getAuthor();
		Conversation c = Matchmaker.getConversationForUser(author);
		Search s = Matchmaker.getSearchForUser(author);

		if (s == null && c == null) {
			return res.append("You weren't searching anyways.");
		}

		if (c != null) {c.stop(author);}
		if (s != null) {
			s.stop();
			return res.append("Stopped your search.");
		}
		return null;
	}
}

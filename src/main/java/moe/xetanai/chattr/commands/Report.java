package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Command;
import moe.xetanai.chattr.entities.Conversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class Report extends Command {
	public Report() {
		super("report", "Report your partner and end the conversation.", CmdFlag.IN_CONVERSATION, CmdFlag.FREE_USE);
	}

	@Override
	public MessageBuilder run(@Nonnull MessageReceivedEvent e, @Nonnull MessageBuilder res) {
		User author = e.getAuthor();
		Conversation c = Matchmaker.getConversationForUser(author);

		c.togglePendingCase(author);
		c.stop(author);

		return res.append("User has been reported.");
	}
}

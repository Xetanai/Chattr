package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Command;
import moe.xetanai.chattr.entities.Conversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class Reveal extends Command {
	public Reveal() {
		super("reveal", "Vote to share usernames with your partner.", CmdFlag.IN_CONVERSATION, CmdFlag.FREE_USE);
	}

	@Override
	public MessageBuilder run(@Nonnull MessageReceivedEvent e, @Nonnull MessageBuilder res) {
		User author = e.getAuthor();
		Conversation c = Matchmaker.getConversationForUser(author);

		if (c.isRevealed()) {
			return res.append("This conversation has already been revealed. No going back!");
		}

		c.toggleReveal(author);

		if (c.isUserRevealed(author)) {
			c.sendSystemMessage("%USER" + c.getUserNum(author) + "% voted to reveal!", null);
		} else {
			c.sendSystemMessage("%USER" + c.getUserNum(author) + "% cancelled the reveal vote.", null);
		}

		if (c.isRevealed()) {
			c.sendPartnerInfo(null); // Send both eath others' info.
		}

		return null;
	}
}

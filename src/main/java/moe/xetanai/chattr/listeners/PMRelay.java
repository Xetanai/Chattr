package moe.xetanai.chattr.listeners;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Conversation;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserTypingEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PMRelay extends ListenerAdapter {

	@Override
	public void onUserTyping(UserTypingEvent event) {
		User author = event.getUser();
		Conversation c = Matchmaker.getConversationForUser(author);

		if (c == null) {return;}

		int num = c.getUserNum(author);
		User partner = num == 1 ? c.getUser2() : c.getUser1();
		partner.openPrivateChannel().complete().sendTyping().queue();
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		String content = event.getMessage().getContentRaw();
		User author = event.getAuthor();
		if (content.startsWith(Chattr.API.getSelfUser().getAsMention())) {return;} // Command, disregard.
		if (author.isBot()) {return;}

		Conversation conv = Matchmaker.getConversationForUser(event.getAuthor());
		if (conv == null) {
			event.getAuthor().openPrivateChannel().complete().sendMessage("You're not in a conversation right now. Try starting one with `@Chattr search`.").queue();
			return;
		}

		conv.addMessageToLog(event.getMessage());

		// Make minor edits to their message.

		if (!event.getMessage().getAttachments().isEmpty()) {
			conv.sendSystemMessage("Files cannot be sent. If you like this person, try revealing and adding them to your friends list!", author);
		}

		if (content.trim().isEmpty()) {
			return; // Do nothing more, it's empty.
		}

		if (!conv.isRevealed()) {
			content = content.replaceAll(author.getName() + "#" + author.getDiscriminator(), "Chattr");
			content = content.replaceAll(author.getName(), "Chattr"); // Don't allow them to reveal themself informally.

			content = "Partner: " + content;
		} else {
			content = author.getName() + ": " + content;
		}

		if (conv.getUser1().equals(author)) {
			conv.getUser2().openPrivateChannel().complete().sendMessage(content).queue();
		} else {
			conv.getUser1().openPrivateChannel().complete().sendMessage(content).queue();
		}
	}
}

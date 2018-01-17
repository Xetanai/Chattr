package moe.xetanai.chattr.listeners;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Conversation;
import moe.xetanai.chattr.entities.Search;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String content = event.getMessage().getContentRaw();
		if(!content.startsWith(Chattr.API.getSelfUser().getAsMention())) {return;} // Not a command.
		if(event.getAuthor().isBot()) {return;}

		String[] args = content.split(" ");

		if(args.length == 1 || args[1].equals("help")) {
			event.getChannel().sendMessage("You can use any of these commands:\n" +
					"\thelp - Show this list.\n" +
					"\tsearch <Interests..> - Search for a chat with similar interests.\n" +
					"\tstop - Stop searching.\n" +
					"\treveal - (In conversation) Reveal your username and discriminator/tag to your partner.\n" +
					"\treport - (In conversation) Report your partner.").queue();
		} else if(args[1].equals("search")) {
			Search s = null;
			s = Matchmaker.getSearchForUser(event.getAuthor());
			if(s != null) {
				event.getChannel().sendMessage("You're already searching for a conversation.").queue();
				return;
			}

			String[] interests = new String[args.length-2];
			System.arraycopy(args,2,interests,0,interests.length);

			s = new Search(event.getAuthor(), interests);
			s.start();
			event.getChannel().sendMessage("Starting your search with "+ interests.length +" interests.").queue();
		} else if(args[1].equals("stop")) {
			Conversation c = Matchmaker.getConversationForUser(event.getAuthor());
			if(c != null) {
				c.stop(event.getAuthor());
				return;
			}

			Search s = null;
			s = Matchmaker.getSearchForUser(event.getAuthor());
			if(s == null) {
				event.getChannel().sendMessage("You weren't searching anyways.").queue();
				return;
			}

			s.stop();
			event.getChannel().sendMessage("Stopped your search.").queue();
		} else if(args[1].equals("reveal")) {
			User author = event.getAuthor();
			Conversation c = Matchmaker.getConversationForUser(author);
			if(c == null) return;
			if(c.isRevealed()) {
				c.sendSystemMessage("This conversation has already been revealed. No going back!", author);
				return;
			}

			c.toggleReveal(author);

			if(c.isUserRevealed(author)) {
				c.sendSystemMessage("%USER"+ c.getUserNum(author) +"% voted to reveal!", null);
			} else {
				c.sendSystemMessage("%USER"+ c.getUserNum(author) +"% cancelled the reveal vote.", null);
			}

			if(c.isRevealed()) {
				c.sendPartnerInfo(null);
			}
		} else if(args[1].equals("report")) {
			User author = event.getAuthor();
			Conversation c = Matchmaker.getConversationForUser(author);
			if(c == null) return;

			c.togglePendingCase(author);
			c.stop(author);

			event.getChannel().sendMessage("User has been reported.").queue();
		}
	}
}

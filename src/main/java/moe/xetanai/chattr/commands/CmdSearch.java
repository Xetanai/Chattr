package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Matchmaker;
import moe.xetanai.chattr.entities.Command;
import moe.xetanai.chattr.entities.Search;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSearch extends Command {
	public CmdSearch() {
		super("search", "Find a conversation. Add interests after to find people who like similar things.", Command.CmdFlag.NO_CONVERSATION, Command.CmdFlag.FREE_USE);
	}

	@Override
	public MessageBuilder run(MessageReceivedEvent e, MessageBuilder res) {
		User author = e.getAuthor();
		Search s = Matchmaker.getSearchForUser(author);
		String[] args = e.getMessage().getContentRaw().split(" ");

		if (s != null) {
			return res.append("You're already searching. Stop your current search first.");
		}

		String[] interests = new String[args.length - 2];
		System.arraycopy(args, 2, interests, 0, interests.length);
		s = new Search(author, interests);
		s.start();
		return res.append("Started your search with " + interests.length + " interests.");
	}
}

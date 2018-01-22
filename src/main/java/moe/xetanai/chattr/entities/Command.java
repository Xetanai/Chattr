package moe.xetanai.chattr.entities;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.Matchmaker;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
	public static List<Command> REGISTRY = new ArrayList<>();

	public final Logger log = LoggerFactory.getLogger(this.getClass());
	private final String keyword;
	private final String helpLine;
	private int inConv = -1;
	private boolean developerOnly = false;

	public Command(String keyword, String helpLine, CmdFlag... flags) {
		this.keyword = keyword;
		this.helpLine = helpLine;

		for (CmdFlag f : flags) {
			switch (f) {
				case DEVELOPER_ONLY:
					this.developerOnly = true;
					break;
				case FREE_USE:
					this.developerOnly = false;
					break;
				case IN_CONVERSATION:
					this.inConv = 1;
					break;
				case NO_CONVERSATION:
					this.inConv = 0;
					break;
				case CONV_AGNOSTIC:
					this.inConv = -1;
			}
		}
	}

	public String getKeyword() { return this.keyword;}

	public String getHelpLine() {return this.helpLine;}

	public boolean isDeveloperOnly() {return this.developerOnly;}

	public int getConvReq() {return this.inConv;}

	public abstract MessageBuilder run(MessageReceivedEvent e, MessageBuilder mb);

	public void invoke(MessageReceivedEvent e) {
		MessageBuilder res = new MessageBuilder();
		User author = e.getAuthor();
		Conversation conv = Matchmaker.getConversationForUser(author);

		this.log.info("Command " + this.keyword + " called by " + author.getName() + " (" + author.getId() + ")");

		if (this.inConv == 1 && conv == null) {
			res.append("You can only use this command while in a conversation.\n");
		}
		if (this.developerOnly && Chattr.DEVID != author.getIdLong()) {
			res.append("Only the developer of Chattr may use this command.\n");
		}

		if (res.isEmpty()) // There were no permissions warnings; The user is allowed.
			this.run(e, res);

		if (res.isEmpty()) {return;}
		res.sendTo(e.getChannel()).queue(); // Reply.
	}

	public void registerCommand() {
		if (REGISTRY.contains(this)) {
			this.log.warn("Cannot register command " + this.keyword + ", already registered.");
		} else {
			REGISTRY.add(this);
			this.log.info("Command " + this.keyword + " registered.");
		}
	}

	public boolean equals(Command c) {
		return this.keyword.equals(c.keyword);
	}

	// INIT FLAGS

	public enum CmdFlag {
		CONV_AGNOSTIC, NO_CONVERSATION, IN_CONVERSATION,
		DEVELOPER_ONLY, FREE_USE;
	}
}

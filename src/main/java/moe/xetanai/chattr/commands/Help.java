package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.entities.Command;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class Help extends Command {
	public Help() {
		super("help", "List commands.", CmdFlag.CONV_AGNOSTIC, CmdFlag.FREE_USE);
	}

	@Override
	public MessageBuilder run(@Nonnull MessageReceivedEvent e, @Nonnull MessageBuilder res) {
		boolean authorIsDev = e.getAuthor().getIdLong() == Chattr.DEVID;

		for (Command c : Command.REGISTRY) {
			if (c.isDeveloperOnly()) {
				if (!authorIsDev) {
					continue;
				} else {
					res.append("***(DEV)*** ");
				}
			}

			res.append(c.getKeyword() + " - " + c.getHelpLine());

			if (c.getConvReq() == 1) {
				res.append(" (In conversation)");
			} else if (c.getConvReq() == 0) {
				res.append(" (Not in conversation)");
			}

			res.append("\n");
		}

		return res;
	}
}

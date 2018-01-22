package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.entities.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Invite extends Command {
	public Invite() {super("invite", "Get invites for Chattr and the support server.", CmdFlag.CONV_AGNOSTIC, CmdFlag.FREE_USE);}

	@Override
	public MessageBuilder run(MessageReceivedEvent e, MessageBuilder mb) {
		EmbedBuilder eb = new EmbedBuilder()
				.setDescription("[Invite Chattr to your server](https://discordapp.com/oauth2/authorize?client_id=393113542670286848&scope=bot&permissions=0)\n" +
						"[Join Chattr's support server](https://discord.gg/x4fPTjt)");
		mb.setEmbed(eb.build()).sendTo(e.getAuthor().openPrivateChannel().complete()).queue();
		mb.clear();
		return null;
	}
}

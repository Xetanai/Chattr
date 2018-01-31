package moe.xetanai.chattr.commands;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.ChattrInfo;
import moe.xetanai.chattr.entities.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Info extends Command {
	public Info() {super("info", "Responds with basic bot info.", CmdFlag.FREE_USE, CmdFlag.CONV_AGNOSTIC);}

	@Override
	public MessageBuilder run(MessageReceivedEvent e, MessageBuilder mb) {
		String devsField = "";
		List<Long> devsById = ChattrInfo.getDeveloperIds();

		for (Long d : devsById) {
			User dev = e.getJDA().getUserById(d);
			devsField += dev.getName() + "#" + dev.getDiscriminator();
		}

		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Chattr v" + ChattrInfo.VERSION + (ChattrInfo.isDevVersion() ? "( DEV )" : ""))
				.setThumbnail(Chattr.API.getSelfUser().getEffectiveAvatarUrl())
				.setColor(new Color(114, 137, 218))
				.addField("Developers", devsField, true)
				.addField("Library", "JDA v" + JDAInfo.VERSION, true)
				.addField("Github", "[Chattr](https://www.github.com/Xetanai/Chattr)\n[JDA](https://www.github.com/DV8FromTheWorld/JDA)", true)
				.setDescription("Only responds to metions.\nChat with strangers! Say `@Chattr search` to try it out or `@Chattr help` to see what else you can do.");

		return mb.setEmbed(eb.build());
	}
}

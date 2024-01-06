package com.finni.discordmodbot.command.discord.slashcommand;

import com.finni.discordmodbot.DiscordModBot;
import com.finni.discordmodbot.command.CustomConsoleSender;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;

import java.util.List;


public class SlashTPS implements InteractionCommand {

	private final DiscordModBot plugin;

	public SlashTPS(){
		this.plugin = DiscordModBot.getInstance();
	}

	@Override
	public void onCommand(InteractionEvent event) {
		CustomConsoleSender wrapped = new CustomConsoleSender(Bukkit.getServer().getConsoleSender());
		Bukkit.dispatchCommand(wrapped, "tps");
		Bukkit.getScheduler().runTaskLater(plugin, () -> event.reply(parseStringFromTPS(wrapped.getMessage())), 20L);

	}
	private String parseStringFromTPS(String message){
		String finalNewmsg = "";
		message = message.replace("§8[§e§l⚡§8]§7  ","§8[§e§l⚡§8]§7 ").replace("§8[§e§l⚡§8]§7 ","")
				//.replace("\n\n","\n")
				.replace("95%ile","`95th %ile`")
				.replace("*","")
				.replace("5s","`5s`")
				.replace("10s","`10s`")
				.replace("1m","`1m`")
				.replace("5m","`5m`")
				.replace("15m","`15m`")
				.replace("min","`min`")
				.replace("med","`med`")
				//.replace("95th %ile","`95th %ile`")
				.replace("max","`max`")
				.replace("CPU","__**CPU**__")
				.replace("Tick","__**Tick**__")
				.replace("TPS","__**TPS**__");


		for (String line : message.split("\n")) {
			// Bukkit.getLogger().info("DCMB: Line is '" + line + "'");
			if(line.startsWith("§a")) {
				line = line.replace("§a","`")
						.replace("§7","`")
						.replace("§c","`")
						.replace("§8","`")
						.replace("§e","`");
				if (!line.strip().endsWith(")") || !line.strip().endsWith(":")) {
					line = line + "`";
				}
			}
			finalNewmsg += line + "\n";
		}
		return finalNewmsg.replace(")`",")");
	}
	@Override
	public String getName() {
		return "tps";
	}

	@Override
	public String getDescription() {
		return "Get the server's current TPS.";
	}

	@Override
	public List<InteractionCommandArgument> getArguments() {
		return null;
	}

	@Override
	public boolean isEphemeral() {
		return true;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}
}
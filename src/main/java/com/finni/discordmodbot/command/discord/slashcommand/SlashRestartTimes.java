package com.finni.discordmodbot.command.discord.slashcommand;

import com.finni.discordmodbot.DiscordModBot;
import com.finni.discordmodbot.command.CustomConsoleSender;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;


public class SlashRestartTimes implements InteractionCommand {

	private final DiscordModBot plugin;

	public SlashRestartTimes(){
		this.plugin = DiscordModBot.getInstance();
	}

	private boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished")) {
			if (meta.asBoolean()) return true;
		}
		return false;
	}

	@Override
	public void onCommand( InteractionEvent event) {
		CustomConsoleSender wrapped = new CustomConsoleSender(Bukkit.getServer().getConsoleSender(), event);
		Bukkit.dispatchCommand(wrapped, "uar time");
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			event.reply(wrapped.getMessage());
		}, 20L);
	}

	@Override
	public String getName() {
		return "restarttime";
	}

	@Override
	public String getDescription() {
		return "See when the server will restart next.";
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
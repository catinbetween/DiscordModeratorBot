package com.finni.discordmodbot.command.discord;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.finni.discordmodbot.DiscordModBot;

import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgumentType;
import net.essentialsx.api.v2.services.discord.InteractionEvent;


public class SlashPlayerList implements InteractionCommand {

	//private final DiscordModBot plugin;

	@Override
	public void onCommand( InteractionEvent event) {
		// The name of the argument here has to be the same you used in getArguments()
		final String playerName = event.getStringArgument("player");
		final Player player = Bukkit.getPlayerExact(playerName);
		if (player == null) {
			event.reply("A player by that name could not be found!");
			return;
		}

		//final int balance = plugin.getBalance(player);

		// It is important you reply to the InteractionEvent at least once as discord
		// will show your bot is 'thinking' until you do so.
		event.reply(player.getName());
	}

	@Override
	public String getName() {
		// This should return the name of the command as you want it to appear in discord.
		// This method should never return different values.
		return "balance";
	}

	@Override
	public String getDescription() {
		// This should return the description of the command as you want it
		// to appear in discord.
		// This method should never return different values.
		return "Checks the balance of the given player";
	}

	@Override
	public List<InteractionCommandArgument> getArguments() {
		// Should return a list of arguments that will be used in your command.
		// If you don't want any arguments, you can return null here.
		return List.of(
				new InteractionCommandArgument(
						// This should be the name of the command argument.
						// Keep it a single world, all lower case.
						"player",
						// This is the description of the argument.
						"name of the player",
						// This is the type of the argument you'd like to receive from
						// discord.
						InteractionCommandArgumentType.STRING,
						// Should be set to true if the argument is required to send
						// the command from discord.
						true));
	}

	@Override
	public boolean isEphemeral() {
		// Whether the command and response should be hidden to other users on discord.
		// Return true here in order to hide command/responses from other discord users.
		return false;
	}

	@Override
	public boolean isDisabled() {
		// Whether the command should be prevented from being registered/executed.
		// Return true here in order to mark the command as disabled.
		return false;
	}
}
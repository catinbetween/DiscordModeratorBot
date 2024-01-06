package com.finni.discordmodbot.command;

import com.finni.discordmodbot.DiscordModBot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//Credit:  NobreHD for the base idea
public class ForceRespawnCommand extends BukkitCommand
{

	DiscordModBot plugin;
	public BukkitCommand command;

	public ForceRespawnCommand(@NotNull String permission, @NotNull String name, @NotNull String description, @NotNull String usage, @NotNull List<String> aliases) {
		super(name, description, usage, aliases);
		this.setName(name);
		this.setDescription(description);
		this.setUsage(usage);
		this.setAliases(aliases);
		this.setPermission(permission);
		try {
			Field f;
			f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);
			CommandMap commandMap = (CommandMap) f.get(Bukkit.getServer());
			commandMap.register(name, this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setPlugin(DiscordModBot plugin) {
		this.plugin  = plugin;
	}


	@Override
	public boolean execute(@NotNull CommandSender commandSender, @NotNull String name, @NotNull String[] args) {



		if(args.length == 1) {
			Player player = Bukkit.getPlayerExact(args[0]);
			if(player != null) {
				if(!player.isDead()){
					commandSender.sendRichMessage("<yellow>" + player.getName() + " is not dead.</yellow>");
					return true;
				}
				commandSender.sendRichMessage("<yellow> Respawned " + player.getName() + "!</yellow>");
				plugin.getLogger().info("Respawned " + player.getName());
				player.spigot().respawn();
            } else {
				commandSender.sendRichMessage("<red>Player " + args[0] + " couldn't be found! :(</red>");
            }
        } else if(args.length > 1){
				commandSender.sendRichMessage(this.getUsage());
		} else {
			commandSender.sendRichMessage("<yellow>Forcing respawns for all dead players!</yellow>");
			plugin.getLogger().info("Forcing respawns for all dead players!");

			Integer respawnedPLayers = 0;
			for (Player player : Bukkit.getOnlinePlayers()){
				if (player.isDead()){
					player.spigot().respawn();
					commandSender.sendRichMessage("<yellow> Respawned " + player.getName() + "!</yellow>");
					plugin.getLogger().info("Respawned " + player.getName());
				}
			}
			commandSender.sendRichMessage("<yellow>Successfully respawned " + respawnedPLayers + " players!</yellow>");
			plugin.getLogger().info("Successfully respawned " + respawnedPLayers + " players");
		}
		return true;
	}


	public static BukkitCommand getNewInstance() {
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("forcePlayerRespawn"); // Our main command name, this can be from a config or somewhere else
		aliases.add("fr"); // Our main command name, this can be from a config or somewhere else

		String usage = "Usage: /forcePlayerRespawn <player>  or /forcePlayerRespawn";
		String description = "forcing player respawn (WARNING: only intended to be used before restart!!)";
		String permission = "discordmodbot.admin";

		return new ForceRespawnCommand(permission, aliases.get(0), description, usage, aliases);
	}

	public void registerCommand() {
		if( command != null) command.unregister(getCommandMap());
		command = ForceRespawnCommand.getNewInstance();

	}

	public CommandMap getCommandMap() {
		CommandMap commandMap = null;

		try {
			Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);

			commandMap = (CommandMap) f.get( Bukkit.getPluginManager());
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
		}

		return commandMap;
	}
}

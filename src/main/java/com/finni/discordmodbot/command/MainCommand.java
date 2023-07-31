package com.finni.discordmodbot.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.finni.discordmodbot.DiscordModBot;


public class MainCommand extends BukkitCommand
{

	DiscordModBot plugin;
	public BukkitCommand command;

	public MainCommand(@NotNull String permission, @NotNull String name, @NotNull String description, @NotNull String usage, @NotNull List<String> aliases) {
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

		//commandSender.sendMessage(name + " " + (( args.length != 0 && (!args[0].equals("")) ? args[0]: "")));

		if(args.length < 1) {
			commandSender.sendMessage("please provide an argument");
			return true;
		}

		switch( args[0] ) {
			case "reload" -> {
				return reloadCommand( commandSender, args );
			} case "whatami" -> {
				if( commandSender instanceof Player ) {
					commandSender.sendMessage( "You're a player" );
				} else if( commandSender instanceof ConsoleCommandSender ) {
					commandSender.sendMessage( "You're a Console" );
				}
			} default -> {
				commandSender.sendMessage( "unrecognized command" );
				return true;
			}
		}

		return true;
	}


	public static BukkitCommand getNewInstance() {
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("discordmodbot"); // Our main command name, this can be from a config or somewhere else
		aliases.add("dmb"); // Our main command name, this can be from a config or somewhere else

		String usage = "/<command>";
		String description = "discordmodbot plugin";
		String permission = "discordmodbot.admin";

		return new MainCommand(permission, aliases.get(0), description, usage, aliases);
	}

	public void registerMainCommand() {
		if( command != null) command.unregister(getCommandMap());
		command = MainCommand.getNewInstance();

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

	public boolean reloadCommand(@NotNull CommandSender commandSender, @NotNull String[] args) {

		if(args.length > 1) {
			commandSender.sendMessage("too many arguments for reload");
			return true;
		}

		if( this.plugin != null ){
			commandSender.sendMessage("reloading....");
			plugin.reloadBot();
		}

		return true;
	}


}

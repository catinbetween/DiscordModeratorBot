/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.finni.discordmodbot;

/** @author Finn Teichmann */

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

import com.finni.discordmodbot.command.discord.MainCommand;
import com.finni.discordmodbot.command.discord.McUserLookup;
import com.finni.discordmodbot.command.discord.SlashPlayerList;
import com.finni.discordmodbot.listener.EssentialsDiscordModlogs;

import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionException;


public class DiscordModBot extends JavaPlugin
{
	private DiscordApi api;

	private DiscordService discordService;

	private String botToken = "";
	private YamlConfiguration config;

	private MainCommand mainDMBCommand;


	private void loadConf(){
		File dir = this.getDataFolder(); //Your plugin folder
		dir.mkdirs(); //Make sure your plugin folder exists

		File conf = new File(this.getDataFolder() + "/conf.yml"); //This is your external file
		this.config = YamlConfiguration.loadConfiguration(conf); //Get the configuration of your external File

		if(!conf.exists()) { //Check if your external file exists
			try {
				conf.createNewFile(); //if not so, create a new one
				this.config.save(conf); //save the configuration of config1 or config2 to your new file
			} catch (IOException e) {
				System.out.println("[PluginName] couldn't create some files!"); //if something goes wrong this is what will be done then
			}
		} else {
			this.botToken = this.config.getString("bot-token");
		}
	}

	@Override
	public void onEnable() {
		this.loadConf();
		this.mainDMBCommand = (MainCommand)MainCommand.getNewInstance();
		this.mainDMBCommand.setPlugin( this );
		this.mainDMBCommand.registerMainCommand();

		loginToDiscord();
		registerListeners();
		this.discordService = Bukkit.getServicesManager().load(DiscordService.class);
		getLogger().info(this.discordService != null ? "discordservice is UP" : "NO DISCORD SERVICE");
		if( discordService != null ){
			try
			{
				discordService.getInteractionController().registerCommand(new SlashPlayerList());
			}
			catch( InteractionException e )
			{
				getLogger().severe("no discord service! can't add slash command.");
			}
		}
	}

	@Override
	public void onDisable() {

		logOutFromDiscord();
	}

	private void loginToDiscord(){
		new DiscordApiBuilder()
				.setToken(botToken) // Set the token of the bot here
				.setIntents( Intent.GUILD_PRESENCES, Intent.GUILD_MESSAGES, Intent.GUILD_MEMBERS, Intent.MESSAGE_CONTENT)
				.login().thenAccept( api -> {this.api = api;} )
				.exceptionally(error -> {
					// Log a warning when the login to Discord failed (wrong token?)
					getLogger().severe("Failed to connect to Discord! Disabling plugin! Warning: "+error.getMessage());
					getPluginLoader().disablePlugin(this);
					return null;
				}).join();
		McUserLookup.createNewInstance(this.api, this.config);
		EssentialsDiscordModlogs.createNewInstance( this.api, this.config );


		// Log a message that the connection was successful and log the url that is needed to invite the bot
		getLogger().info("Connected to Discord as " + api.getYourself().getDiscriminatedName());
		//getLogger().info("Open the following url to invite the bot: " + api.createBotInvite());
	}

	private void logOutFromDiscord(){
		if (api != null)
		{
			// Make sure to disconnect the bot when the plugin gets disabled
			api.disconnect().join();
		}
	}

	public void reloadConf() {

		if (this.api != null)
			logOutFromDiscord();
		loadConf();
		loginToDiscord();
		registerListeners();
		System.gc();
	}

	private void registerListeners() {
		if(McUserLookup.getInstance() != null && EssentialsDiscordModlogs.getInstance() != null ) {
			api.addListener(McUserLookup.getInstance());
			this.getServer().getPluginManager().registerEvents( EssentialsDiscordModlogs.getInstance(), this);
		} else {
			getLogger().severe("no discord API! can't add listeners.");
		}
	}
}
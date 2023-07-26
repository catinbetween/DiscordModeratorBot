/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.finni.discordmodbot.command.discord;

//import github.scarsz.discordsrv.DiscordSRV;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;


/**
 * @author Finn Teichmann
 */
public class McUserLookup implements MessageCreateListener
{

	static McUserLookup instance;
	DiscordApi discordapi;

	YamlConfiguration config;

	String commandPrefix;
	//DiscordSRV discordsrv;

	DiscordLinkService linkApi;

	List<Long> allowedRoles;

	List<Long> allowedChannels;


	public McUserLookup( DiscordApi api, YamlConfiguration config )
	{
		this.discordapi = api;
		this.config = config;
		this.linkApi = Bukkit.getServicesManager().load( DiscordLinkService.class );

		this.allowedRoles = this.config.getStringList( "allowed-roles" )
				.stream()
				.map( Long::parseLong )
				.collect( Collectors.toList() );

		this.allowedChannels = this.config.getStringList( "allowed-channels" )
				.stream()
				.map( Long::parseLong )
				.collect( Collectors.toList() );

		this.commandPrefix = this.config.getString( "prefix" );
	}


	@Override
	public void onMessageCreate( MessageCreateEvent event )
	{

		boolean isAllowedCommandChannel = this.allowedChannels.stream()
				.anyMatch( channelID -> Objects.equals( channelID, event.getChannel().getId() ) );

		if( event.getMessageAuthor().isBotUser() || !isAllowedCommandChannel || !event.getMessageContent()
				.startsWith( this.commandPrefix ) )
		{
			return;
		}

		User commanduser = null;

		try
		{
			CompletableFuture<User> userF = discordapi.getUserById( event.getMessageAuthor().getId() );
			userF.join();
			commanduser = userF.get();

			List<Role> dcuserRoles = event.getServer().get().getRoles( commanduser );

			boolean isAuthorized = allowedRoles.stream()
					.anyMatch(
							role -> dcuserRoles.stream().anyMatch( dcrole -> Objects.equals( dcrole.getId(), role ) ) );
			if( !isAuthorized )
			{
				Bukkit.getLogger()
						.warning( "User " + commanduser.getDiscriminatedName()
								+ " was not authorized to use this command" );
				event.getChannel().sendMessage( "Sorry, You need to have a Moderator Role to do this." );
				return;
			}

		}
		catch( Exception e )
		{
			Bukkit.getLogger().warning( "Error checking out command sender (0)" );
			Bukkit.getLogger().warning( e.toString() );
			event.getChannel().sendMessage( "Sorry, i couldn't check who you are. (0)" );
			return;
		}

		String[] message = event.getMessageContent().split( " " );

		if( 2 > message.length )
		{
			event.getChannel().sendMessage( "you need to provide a MC username or a discord id" );
			return;
		}

		String userstring = message[1];

		boolean discordIDProvided = false;
		User dcuser = null;

		if( userstring.matches( "\\d*" ) )
		{
			try
			{
				CompletableFuture<User> userF = discordapi.getUserById( userstring );
				userF.join();
				dcuser = userF.get();

				discordIDProvided = true;

			}
			catch( Exception e )
			{
				Bukkit.getLogger().info( "tried to interpret param as DC id and found no one." );
				Bukkit.getLogger().info( e.toString() );
			}
		}

		if( discordIDProvided )
		{

			try
			{
				UUID uuid = this.linkApi.getUUID( dcuser.getIdAsString() );
				OfflinePlayer mcuser = Bukkit.getOfflinePlayer( uuid );

				getMessage( dcuser, mcuser ).send( event.getChannel() );

			}
			catch( Exception e )
			{
				Bukkit.getLogger().warning( "Something went wrong! (1)" );
				Bukkit.getLogger().warning( e.toString() );
				event.getChannel().sendMessage( "Something went wrong! (1)" );
			}
		}
		else
		{
			try
			{
				OfflinePlayer mcuser = Bukkit.getOfflinePlayer( userstring );
				String discordId = this.linkApi.getDiscordId( mcuser.getUniqueId() );
				if( discordId.matches( "\\d*" ) )
				{
					CompletableFuture<User> userF = discordapi.getUserById( discordId );
					userF.join();
					dcuser = userF.get();
					getMessage( dcuser, mcuser ).send( event.getChannel() );
				}

			}
			catch( NullPointerException e )
			{
				Bukkit.getLogger().info( "discord id was null. " );
				Bukkit.getLogger().info( e.toString() );
				event.getChannel().sendMessage( "No player found! (2)" );
			}
			catch( CompletionException e )
			{
				Bukkit.getLogger().info( "Player Could not be found (2)" );
				Bukkit.getLogger().info( e.toString() );
				event.getChannel().sendMessage( "No player found! (2)" );
			}
			catch( Exception e )
			{
				Bukkit.getLogger().warning( "Something went wrong! (2)" );
				Bukkit.getLogger().warning( e.toString() );
				event.getChannel().sendMessage( "Something went wrong! (2)" );
			}
		}
	}


	private MessageBuilder getMessage( User user, OfflinePlayer mcuser )
	{

		AllowedMentions allowedMentions = new AllowedMentionsBuilder().addUser( user.getId() ).build();

		return new MessageBuilder().setAllowedMentions( allowedMentions )
				.append( user.getIdAsString() )
				.setEmbed( new EmbedBuilder().setTitle( "User Lookup" )
						.setColor( Color.blue )
						.addField( "MC User", mcuser.getName() )
						.addField( "Discord User", user.getMentionTag(), false )
						.addField( "Discord ID", user.getIdAsString(), false ) );

	}

	public static void createNewInstance(DiscordApi api, YamlConfiguration config){
		McUserLookup.instance = new McUserLookup(api, config);
	}

	public static McUserLookup getInstance() {
		return instance;
	}
}

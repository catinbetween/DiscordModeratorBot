/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.finni.discordmodbot.command.discord;

//import github.scarsz.discordsrv.DiscordSRV;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.finni.discordmodbot.DiscordModBot;
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
import org.javacord.api.interaction.*;
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

	DiscordLinkService discordLinkService;

	List<Long> allowedRoles;

	List<Long> allowedChannels;


	public McUserLookup()
	{
		this.discordapi = DiscordModBot.getInstance().getDiscordAPI();
		this.config = DiscordModBot.getInstance().getMcModBotconfig();
		this.discordLinkService = DiscordModBot.getInstance().getDiscordLinkService();

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

		try {
			if( userstring.matches( "\\d*" ) ) {
				User dcuser = findDiscordUserByID(userstring);
				OfflinePlayer mcuser = lookUpMCPlayerByDiscordUser(dcuser);
				getMessage( dcuser, mcuser ).send( event.getChannel() );
			} else {
				OfflinePlayer mcuser = findMCUserByMCName(userstring);
				User dcuser = lookUpDiscordUserByMCUUID(mcuser.getUniqueId());
				getMessage( dcuser, mcuser ).send( event.getChannel() );
			}
		}
		catch( Exception e )
		{
			Bukkit.getLogger().warning("Something went wrong!" + e.getMessage() );
			event.getChannel().sendMessage( "Something went wrong!" + e.getMessage());
		}
	}

	private User findDiscordUserByID(String discordID) throws Exception {
		CompletableFuture<User> userF = discordapi.getUserById( discordID );
		userF.join();
		User dcuser = userF.get();
		if(dcuser == null)
			throw new Exception("Discord-User not found!");

		return dcuser;
	}

	private OfflinePlayer findMCUserByMCName(String mcname) throws Exception {

		OfflinePlayer mcuser  = Bukkit.getOfflinePlayer( mcname );

		if(mcuser == null)
			throw new Exception("MC-User with given MC-name not found!");

		return mcuser;
	}

	private OfflinePlayer findMCUserByMCUUID(UUID uuid) throws Exception {

		OfflinePlayer mcuser  = Bukkit.getOfflinePlayer( uuid );

		if(mcuser == null)
			throw new Exception("MC-User with given UUID not found!");
		return mcuser;
	}

	private OfflinePlayer lookUpMCPlayerByDiscordUser(User dcuser) throws Exception {

		User dcuser2 = findDiscordUserByID(dcuser.getIdAsString());
		UUID uuid = this.discordLinkService.getUUID( dcuser.getIdAsString() );

        return Bukkit.getOfflinePlayer( uuid );
	}

	private User lookUpDiscordUserByMCUUID(UUID uuid) throws Exception {

		String discordId = this.discordLinkService.getDiscordId( uuid );
		if(discordId == null) {
			throw new Exception("Player with given UUID has no Discord Account Linked!");
		}

        return findDiscordUserByID(discordId);
	}

	private EmbedBuilder getEmbed(OfflinePlayer mcuser, User dcuser){
        return new EmbedBuilder().setTitle( "User Lookup" )
				.setColor( Color.blue )
				.addField( "MC User", mcuser.getName() )
				.addField( "Minecraft-UUID", mcuser.getUniqueId().toString(), false )
				.addField( "Discord User", dcuser.getMentionTag(), false )
				.addField( "Discord ID", dcuser.getIdAsString(), false );
	}

	private MessageBuilder getMessage( User dcuser, OfflinePlayer mcuser )
	{

		AllowedMentions allowedMentions = new AllowedMentionsBuilder().addUser( dcuser.getId() ).build();

		return new MessageBuilder().setAllowedMentions( allowedMentions )
				.append( dcuser.getIdAsString() )
				.append("\n")
				.append( mcuser.getUniqueId().toString() )
				.setEmbed( getEmbed(mcuser, dcuser) );

	}

	public void registerSlashCommand(){
		SlashCommand command =
			SlashCommand.with("mcuser", "lookup a linked user.",
				Arrays.asList(
					SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "by-discord-id", "Look up user by their discord ID.",
						Arrays.asList(
							SlashCommandOption.create(SlashCommandOptionType.STRING, "Discord-ID", "Example: 123456781234567890", true)
						)
					),
					SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "by-discord-username", "Look up user by their Discord username.",
						Arrays.asList(
							SlashCommandOption.create(SlashCommandOptionType.USER, "Discord-Username", "Example: @emotionalsupportdemon", true)
						)
					),
					SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "by-minecraft-username", "Loop up user by their Minecraft username.",
						Arrays.asList(
							SlashCommandOption.create(SlashCommandOptionType.STRING, "Minecraft-Username", "Example: MangoMc", true)
						)
					),
					SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "by-minecraft-UUID", "Look up user by their Minecraft UUID.",
						Arrays.asList(
							SlashCommandOption.create(SlashCommandOptionType.STRING, "Minecraft-UUID", "Example: a1111bbb-feeb-1234-0000-1abc42069def", true)
						)
					)
				)
			).setEnabledInDms(false)
			.createGlobal(discordapi)
			.join();

		discordapi.addSlashCommandCreateListener(event -> {
			SlashCommandInteraction interaction = event.getSlashCommandInteraction();

			if (interaction.getFullCommandName().equals("mcuser by-discord-id")) {
				Optional<String> discordID = interaction.getArgumentStringValueByName("Discord-ID");
				if(discordID.isPresent()){
					try {
						User dcuser = findDiscordUserByID(discordID.get());
						OfflinePlayer mcuser = lookUpMCPlayerByDiscordUser(dcuser);
						interaction.createImmediateResponder()
								.addEmbed(getEmbed(mcuser, dcuser))
								.append(dcuser.getIdAsString() )
								.respond();
					} catch (Exception e) {
						interaction.createImmediateResponder().append("Something went wrong! " + e.getMessage()).respond();
					}
				} else {
					interaction.createImmediateResponder().append("Something went wrong! Please provide a Discord ID." ).respond();
				}
			} else if(interaction.getFullCommandName().equals("mcuser by-discord-username")) {
				Optional<User> discorduser = interaction.getArgumentUserValueByName("Discord-Username");
				if(discorduser.isPresent()){
					try {
						User dcuser = discorduser.get();
						OfflinePlayer mcuser = lookUpMCPlayerByDiscordUser(dcuser);
						interaction.createImmediateResponder()
								.addEmbed(getEmbed(mcuser, dcuser))
								.append(dcuser.getIdAsString() )
								.respond();
					} catch (Exception e) {
						interaction.createImmediateResponder().append("Something went wrong! " + e.getMessage()).respond();
					}
				} else {
					interaction.createImmediateResponder().append("Something went wrong! Please provide a discord User." ).respond();
				}
			} else if(interaction.getFullCommandName().equals("mcuser by-minecraft-username")) {
				Optional<String> mcusername = interaction.getArgumentStringValueByName("Minecraft-Username");
				if(mcusername.isPresent()){
					try {
						OfflinePlayer mcuser = findMCUserByMCName(mcusername.get());
						User dcuser = lookUpDiscordUserByMCUUID(mcuser.getUniqueId());
						interaction.createImmediateResponder()
								.addEmbed(getEmbed(mcuser, dcuser))
								.append(dcuser.getIdAsString() )
								.respond();
					} catch (Exception e) {
						interaction.createImmediateResponder().append("Something went wrong! " + e.getMessage()).respond();
					}
				} else {
					interaction.createImmediateResponder().append("Something went wrong! Please provide a MC-Username." ).respond();
				}
			} else if(interaction.getFullCommandName().equals("mcuser by-minecraft-uuid")) {
				Optional<String> mcuuid = interaction.getArgumentStringValueByName("Minecraft-UUID");
				if(mcuuid.isPresent()){
					try {
						OfflinePlayer mcuser = findMCUserByMCUUID(UUID.fromString(mcuuid.get()));
						User dcuser = lookUpDiscordUserByMCUUID(mcuser.getUniqueId());
						interaction.createImmediateResponder()
								.addEmbed(getEmbed(mcuser, dcuser))
								.append(dcuser.getIdAsString() )
								.respond();
					} catch (Exception e) {
						interaction.createImmediateResponder().append("Something went wrong! " + e.getMessage()).respond();
					}
				} else {
					interaction.createImmediateResponder().append("Something went wrong! Please provide a UUID." ).respond();
				}
			}
		});
	}

	public static void createNewInstance(){
		McUserLookup.instance = new McUserLookup();
	}

	public static McUserLookup getInstance() {
		return instance;
	}
}

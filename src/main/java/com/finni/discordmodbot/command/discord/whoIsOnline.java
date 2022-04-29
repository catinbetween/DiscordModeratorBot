/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.finni.discordmodbot.command.discord;

import github.scarsz.discordsrv.DiscordSRV;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.bukkit.Bukkit;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

/** @author Finn Teichmann */
public class whoIsOnline implements MessageCreateListener {

	DiscordApi discordapi;
	YamlConfiguration config;
	String commandPrefix = "!mcuser";
	DiscordSRV discordsrv;
	List<Long> allowedRoles;
	
	public whoIsOnline(DiscordApi api, YamlConfiguration config) {
		this.discordapi = api;
		this.config = config;
		this.discordsrv = (DiscordSRV)Bukkit.getServer().getPluginManager().getPlugin("DiscordSRV");

    this.allowedRoles =
        this.config.getStringList("allowed-roles").stream()
            .map(role -> Long.parseLong(role))
            .collect(Collectors.toList());
	}

	
	
	@Override
    public void onMessageCreate(MessageCreateEvent event) {

    if (event.getMessageAuthor().isBotUser()
        || !event
            .getChannel()
            .getIdAsString()
            .equalsIgnoreCase(this.config.getString("command-channel-id"))
        || !event.getMessageContent().startsWith(this.commandPrefix)) {
					return;
				}
				
				User commanduser = null;
				
				try { 
					CompletableFuture<User> userF = discordapi.getUserById(event.getMessageAuthor().getId());
					userF.join();
					commanduser = userF.get();

					
					List<Role> dcuserRoles = event.getServer().get().getRoles(commanduser);

					Boolean isAuthorized = allowedRoles.stream().anyMatch(
						role -> {
							return dcuserRoles.stream()
									.anyMatch(
											dcrole -> {
												Boolean yes = Objects.equals(dcrole.getId(), role);
												return yes;
											});
                  });
					if (!isAuthorized){
					Bukkit.getLogger().warning("User " + commanduser.getDiscriminatedName() + " was not authorized to use this command");
					event.getChannel().sendMessage("Sorry, You need to have a Moderator Role to do this.");
					return;
				}

				} catch (Exception e) {
					Bukkit.getLogger().warning("Error checking out command sender (0)");
					Bukkit.getLogger().warning(e.toString());
					event.getChannel().sendMessage("Sorry, i couldn't check who you are. (0)");
					return;
				}
        // Collect the names of all online players
        //String onlinePlayers = Bukkit.getOnlinePlayers()
        //        .stream()
        //        .map(Player::getName)
        //        .collect(Collectors.joining(", "));
				
				String[] message = event.getMessageContent().split(" ");
				
				if (2 > message.length){
					event.getChannel().sendMessage("you need to provide a MC username or a discord id");
					return;
				}
				

				String userstring = message[1];
				
				Boolean discordIDProvided = false;
				User dcuser = null;
				
				if(userstring.matches("\\d*")){
					try { 
						CompletableFuture<User> userF = discordapi.getUserById(userstring);
						userF.join();
						dcuser = userF.get();

						discordIDProvided = true;

					} catch (Exception e) {
							Bukkit.getLogger().info("tried to interpret param as DC id and found no one.");
							Bukkit.getLogger().info(e.toString());
					}
				}
				
				
				
				if(discordIDProvided) {
					
					try {
						UUID uuid = this.discordsrv.getAccountLinkManager().getLinkedAccounts().get(dcuser.getIdAsString());
						OfflinePlayer mcuser = Bukkit.getOfflinePlayer(uuid);
						
						getMessage(dcuser, mcuser).send(event.getChannel());
						
						
					} catch (Exception e) {
						Bukkit.getLogger().warning("Something went wrong! (1)");
						Bukkit.getLogger().warning(e.toString());
						event.getChannel().sendMessage("Something went wrong! (1)");
					}
				} else {
						try {
							OfflinePlayer mcuser = Bukkit.getOfflinePlayer(userstring);
							String discordId = this.discordsrv.getAccountLinkManager().getDiscordId(mcuser.getUniqueId());

							if(discordId.matches("\\d*")){
								CompletableFuture<User> userF = discordapi.getUserById(discordId);
								userF.join();
								dcuser = userF.get();
								getMessage(dcuser, mcuser).send(event.getChannel());
							}
							
						} catch (NullPointerException e) {
							Bukkit.getLogger().info("discord id was null. ");
							Bukkit.getLogger().info(e.toString());
							event.getChannel().sendMessage("No player found! (2)");
						}
							catch (CompletionException e) {
							Bukkit.getLogger().info("Player Could not be found (2)");
							Bukkit.getLogger().info(e.toString());
							event.getChannel().sendMessage("No player found! (2)");
						} catch (Exception e){
							Bukkit.getLogger().warning("Something went wrong! (2)");
							Bukkit.getLogger().warning(e.toString());
							event.getChannel().sendMessage("Something went wrong! (2)");
				}
			}
				
//        // Check if there are any online players
//        if (onlinePlayers.isEmpty()) {
//            event.getChannel().sendMessage("There are no players online!");
//            return;
//        }
//
//        // Display the names of all online players
//        new MessageBuilder()
//                .append("The following players are currently online:")
//                .appendCode("", onlinePlayers)
//                .send(event.getChannel());
				
				
    }
		
		private MessageBuilder getMessage(User user, OfflinePlayer mcuser) {
			
			AllowedMentions allowedMentions =
                    new AllowedMentionsBuilder().addUser(user.getId()).build();

			return new MessageBuilder()
					.setAllowedMentions(allowedMentions)
					.append(user.getIdAsString())
					.setEmbed(
							new EmbedBuilder()
									.setTitle("User Lookup")
									.setColor(Color.blue)
									.addField("MC User", mcuser.getName())
									.addField("Discord User", user.getNicknameMentionTag(), false)
									.addField("Discord ID", user.getIdAsString(), false));
					              
		}

}

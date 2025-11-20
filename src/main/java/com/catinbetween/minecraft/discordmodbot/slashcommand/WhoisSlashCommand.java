package com.catinbetween.minecraft.discordmodbot.slashcommand;

import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.EmbedBuilder;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.Member;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.interactions.commands.OptionType;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.interactions.commands.build.OptionData;
import com.hypherionmc.sdlink.shaded.jagrosh.jdautilities.command.SlashCommandEvent;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Log4j2
public class WhoisSlashCommand extends SDLinkSlashCommand {
    public WhoisSlashCommand() {
        super(true);

        this.name = "whois";
        this.help = "add option \"mcusername\" or \"mcuuid\" or \"discorduser\" or \"discorduserid\" to see the linked accounts";

        this.options = List.of(new OptionData(OptionType.STRING, "mcusername", "Minecraft Username of player").setRequired(false),
                new OptionData(OptionType.STRING, "mcuuid", "Minecraft UUID of player").setRequired(false),
                new OptionData(OptionType.USER, "discorduser", "The Discord User to look up").setRequired(false),
                new OptionData(OptionType.STRING, "discorduserid", "The Discord User ID to look up").setRequired(false)
        );

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(false).queue();
        try {

            List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

            if (accounts.isEmpty()) {
                event.getHook().sendMessage("No one has been verified yet!").setEphemeral(false).queue();
                return;
            }
            Optional<SDLinkAccount> accountOptional = Optional.empty();

            String mcusername = event.getOption("mcusername") != null ? event.getOption("mcusername").getAsString() : null;
            String mcuuid = event.getOption("mcuuid") != null ? event.getOption("mcuuid").getAsString() : null;
            String discorduserid = event.getOption("discorduserid") != null ? event.getOption("discorduserid").getAsString() : null;
            Member discorduser = event.getOption("discorduser") != null ? event.getOption("discorduser").getAsMember() : null;

            int optioncount = 0;
            if (mcusername != null) optioncount++;
            if (mcuuid != null) optioncount++;
            if (discorduserid != null) optioncount++;
            if (discorduser != null) optioncount++;

            if(optioncount != 1) {
                event.getHook().sendMessage("Set exactly ONE of the four options").setEphemeral(false).queue();
            }

            if (mcusername != null) accountOptional = findAccountByMCUsername(mcusername, accounts);
            if (mcuuid != null) accountOptional = findAccountByMCUUID(mcuuid, accounts);
            if (discorduserid != null) accountOptional = findAccountByDiscordUserId(discorduserid, accounts);
            if (discorduser != null) accountOptional = findAccountByDiscordUser(discorduser, accounts);

            if (accountOptional.isEmpty()) {
                event.getHook().sendMessage("Couldn't find any account based on this option!").setEphemeral(false).queue();
                return;
            }
            SDLinkAccount account = accountOptional.get();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("MC User lookup");
            builder.setColor(Color.GREEN);
            StringBuilder sBuilder = new StringBuilder();
            Member member = null;

            try {
                if (account.getDiscordID() != null && !account.getDiscordID().isEmpty()) {
                    member = event.getGuild().getMemberById(account.getDiscordID());
                }
            } catch (Exception ignored) {
            }

            sBuilder.append(account.getUsername()).append(!account.getInGameName().equalsIgnoreCase(account.getUsername()) ? " (" + account.getInGameName() + " )" : "").append(" -> ").append(member == null ? "Unlinked" : member.getAsMention()).append(account.getUuid() != null ? " (" + account.getUuid() + ")": " (no uuid)").append("\r\n");
            builder.setDescription(sBuilder);


            event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(false).queue();
        } catch (Exception e) {
            log.error(e.toString());
            event.getHook().sendMessage("Something went wrong while trying to find the account").setEphemeral(false).queue();
        }
    }

    private Optional<SDLinkAccount> findAccountByMCUsername(String mcusername, List<SDLinkAccount> accounts) {
        return accounts.stream().filter(sdLinkAccount -> {
                    try {
                        return sdLinkAccount.getUsername().equalsIgnoreCase(mcusername);
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).findFirst();
    }

    private Optional<SDLinkAccount> findAccountByMCUUID(String mcuuid, List<SDLinkAccount> accounts) {
        return accounts.stream().filter(
                sdLinkAccount -> {
                    try {
                        return sdLinkAccount.getUuid().equals(mcuuid);
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).findFirst();
    }

    private Optional<SDLinkAccount> findAccountByDiscordUser(Member discorduser, List<SDLinkAccount> accounts) {
        return accounts.stream().filter(sdLinkAccount -> {
                    try {
                        return sdLinkAccount.getDiscordID().equals(discorduser.getId());
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).findFirst();
    }

    private Optional<SDLinkAccount> findAccountByDiscordUserId(String discorduserid, List<SDLinkAccount> accounts) {
        return accounts.stream().filter(sdLinkAccount -> {
                    try {
                        return sdLinkAccount.getDiscordID().equals(discorduserid);
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).findFirst();
    }
}

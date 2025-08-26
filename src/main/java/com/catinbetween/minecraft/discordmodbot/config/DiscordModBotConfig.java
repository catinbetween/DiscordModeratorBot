package com.catinbetween.minecraft.discordmodbot.config;

import com.catinbetween.minecraft.discordmodbot.DiscordModBot;
import com.catinbetween.minecraft.discordmodbot.slashcommand.WhoisSlashCommand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.JDA;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.JDABuilder;
import com.hypherionmc.sdlink.shaded.jagrosh.jdautilities.command.CommandClient;
import com.hypherionmc.sdlink.shaded.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mojang.brigadier.context.CommandContext;

import lombok.extern.log4j.Log4j2;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Log4j2
public class DiscordModBotConfig {
    private static final File configDir = new File("config/discordmodbot");
    private static final File configFile = new File("config/discordmodbot/" + DiscordModBot.MOD_ID + "_config.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();
    public static DiscordModBotConfig INSTANCE = new DiscordModBotConfig();

    public String botToken = "";
    public String guildID = "";
    public String ownerID = "";
    public String commandChannelId = "";
    public String logChannelID = "";
    public String[] allowedRoles = new String[]{};
    public String[] allowedChannels = new String[]{};
    public DiscordModerationSettings discordModerationSettings = new DiscordModerationSettings(false, false, false, false, false, false, false, false, false, false, false, false);

    public String logLevel = "INFO";
    public transient Level transientLogLevel;

    public static void loadConfig() {
        try {
            configDir.mkdirs();
            if (configFile.createNewFile()) {
                FileWriter fw = new FileWriter(configFile);
                fw.append(gson.toJson(INSTANCE));
                fw.close();
                log.info("Default config generated.");
            } else {
                FileReader fr = new FileReader(configFile);
                INSTANCE = gson.fromJson(fr, DiscordModBotConfig.class);
                fr.close();
                log.info("DiscordModBotConfig loaded. {} {}", DiscordModBotConfig.INSTANCE.guildID, DiscordModBotConfig.INSTANCE.ownerID);
            }
            CommandClientBuilder builder = new CommandClientBuilder();
            builder.addSlashCommand(new WhoisSlashCommand());

            // Build the CommandClient instance
            CommandClient commandClient = builder.forceGuildOnly(DiscordModBotConfig.INSTANCE.guildID).setOwnerId(DiscordModBotConfig.INSTANCE.ownerID).build();

            // Add it as an event listener to JDA
            JDA jda = JDABuilder.createDefault(DiscordModBotConfig.INSTANCE.botToken)
                    .addEventListeners(
                            commandClient
                            // Any other events you have
                    ).build();
            jda.awaitReady();

            log.info("(Re-)Registered slash command.");
        } catch (Exception e) {
            log.warn("Error loading config, using default values.");
        }



    }

    public static void saveConfig() {
        try {
            configDir.mkdirs();
            FileWriter fw = new FileWriter(configFile);
            fw.append(gson.toJson(INSTANCE));
            fw.close();
            log.info("DiscordModBotConfig saved.");
        } catch (Exception e) {
            log.error("Error saving config");
        }
    }

    public static int commandReload(CommandContext<ServerCommandSource> context) {

        context.getSource().sendFeedback(() -> Text.literal("DiscordModBot: Reloading config..."), false);
        log.info("Reloading config...");
        loadConfig();
        context.getSource().sendFeedback(() -> Text.literal("DiscordModBot: Config reloaded."), false);
        log.info("Config reloaded.");
        return 1;
    }
}
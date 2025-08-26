package com.catinbetween.minecraft.discordmodbot;

import com.catinbetween.minecraft.discordmodbot.command.MeowCommand;
import com.catinbetween.minecraft.discordmodbot.config.DiscordModBotConfig;
import com.catinbetween.minecraft.discordmodbot.slashcommand.WhoisSlashCommand;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.JDA;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.JDABuilder;
import com.hypherionmc.sdlink.shaded.jagrosh.jdautilities.command.CommandClient;
import com.hypherionmc.sdlink.shaded.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lombok.extern.java.Log;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Log
public class DiscordModBot implements ModInitializer {
    public static final String  MAIN_COMMAND_PERMISSION = "discordmodbot.admin";
    public static final String MOD_ID = "DiscordModBot";
    public static final String MOD_NAME = "DiscordModeratorBot";
    public static final String MOD_VER = "3.0.0";

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        log.info("version " + MOD_VER);
        DiscordModBotConfig.loadConfig();


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("dmb")
                    .then(literal("meow").executes(MeowCommand::run))
                    .then(literal("reload").executes(DiscordModBotConfig::commandReload)).requires(source -> hasPermission(source.getPlayer(),MAIN_COMMAND_PERMISSION,4))

            );
        });

        log.info("Initialized successfully");
    }

    private boolean hasPermission(ServerPlayerEntity player, String permission) {
        LuckPerms luckPerms = null;
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (Exception e) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUuid());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission( MAIN_COMMAND_PERMISSION).asBoolean();
    }

    private boolean hasPermission(ServerPlayerEntity player, String permission, Integer opLevel) {
        return player.getPermissionLevel() == opLevel || hasPermission(player, permission);
    }
}

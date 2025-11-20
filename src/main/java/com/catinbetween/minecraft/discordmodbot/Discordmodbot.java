package com.catinbetween.minecraft.discordmodbot;

import com.catinbetween.minecraft.discordmodbot.command.MeowCommand;
import com.catinbetween.minecraft.discordmodbot.config.DiscordModBotConfig;
import net.fabricmc.api.ModInitializer;

import lombok.extern.java.Log;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.literal;

@Log
public class Discordmodbot implements ModInitializer {
    public static final String MOD_ID = "DiscordModBot";
    public static final String MOD_NAME = "DiscordModeratorBot";
    public static final String MAIN_COMMAND_PERMISSION = "discordmodbot.admin";
    public static final String MOD_VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        log.info("version " + MOD_VERSION);
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

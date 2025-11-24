package com.catinbetween.minecraft.discordmodbot;

import com.catinbetween.minecraft.discordmodbot.config.DiscordModBotConfig;
import com.catinbetween.minecraft.discordmodbot.utils.IdentityDatabaseManager;
import com.catinbetween.minecraft.discordmodbot.utils.SDLinkEventHandler;
import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;


public class DiscordModBot implements ModInitializer {
    public static final String MOD_ID = "DiscordModBot";
    public static final String MAIN_COMMAND_PERMISSION = "discordmodbot.admin";
    public static IdentityDatabaseManager IDENTITY_DATABASE;
    private static SDLinkEventHandler eventListener;

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordModBot.class);

    @Override
    public void onInitialize() {
        DiscordModBotConfig.loadConfig();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("dmb")
                    .then(literal("reload").executes(DiscordModBotConfig::commandReload)).requires(source -> hasPermission(source.getPlayer(),MAIN_COMMAND_PERMISSION,4))

            );
        });
        LOGGER.info("Initialized McModBotConfig successfully");

        IDENTITY_DATABASE = new IdentityDatabaseManager();
        IDENTITY_DATABASE.initialize();
        eventListener = new SDLinkEventHandler(DatabaseManager.INSTANCE, IDENTITY_DATABASE);

        CraterEventBus.INSTANCE.registerEventListener(eventListener);

        ServerPlayConnectionEvents.JOIN.register( (a, b, c) -> {
            eventListener.onPlayerJoin(a.getPlayer());
        });
        LOGGER.info("Initialized Database and SDLink integration successfully");
    }

    /*private boolean hasPermission(ServerPlayerEntity player, String permission) {
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
    }*/

    private boolean hasPermission(ServerPlayerEntity player, String permission, Integer opLevel) {
        return player.getPermissionLevel() == opLevel /*|| hasPermission(player, permission)*/;
    }
}

package com.catinbetween.minecraft.discordmodbot.identity.model;

import java.sql.Timestamp;
import java.util.List;

public record IdentityProfile (
    Integer identityProfileId,
    int uniqueMinecraftAccounts,
    int uniqueDiscordAccounts,
    List<String> allMinecraftNames,
    List<String> allDiscordIds,
    Timestamp firstEverSeen,
    Timestamp lastSeen,
    String notes
) {}
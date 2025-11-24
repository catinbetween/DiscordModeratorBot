package com.catinbetween.minecraft.discordmodbot.command.slashcommand;

import com.catinbetween.minecraft.discordmodbot.DiscordModBot;
import com.catinbetween.minecraft.discordmodbot.identity.model.IdentityProfile;
import com.catinbetween.minecraft.discordmodbot.identity.model.Snapshot;
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
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
public class WhoisSlashCommand extends SDLinkSlashCommand {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public WhoisSlashCommand() {
        super(true);

        this.name = "whois";
        this.help = "Look up player identity history by Minecraft username, UUID, Discord user, or Discord user ID";

        this.options = List.of(
                new OptionData(OptionType.STRING, "mcusername", "Minecraft Username of player").setRequired(false),
                new OptionData(OptionType.STRING, "mcuuid", "Minecraft UUID of player").setRequired(false),
                new OptionData(OptionType.USER, "discorduser", "The Discord User to look up").setRequired(false),
                new OptionData(OptionType.STRING, "discorduserid", "The Discord User ID to look up").setRequired(false)
        );

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(false).queue();
        try {
            // Extract and validate options
            SearchParameters params = extractAndValidateOptions(event);
            if (params == null) {
                return; // Validation failed, error already sent
            }

            // Search for account in identity database
            SearchResult identityResult = searchIdentityDatabase(params);

            // If not found in identity database, try SDLink fallback
            if (identityResult.snapshot.isEmpty()) {
                handleSDLinkFallback(event, params);
                return;
            }

            // Build and send identity profile response
            sendIdentityProfileResponse(event, identityResult, params);

        } catch (Exception e) {
            log.error("Error executing whois command", e);
            event.getHook().sendMessage("❌ Something went wrong while trying to find the account: " + e.getMessage())
                    .setEphemeral(false).queue();
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(DATE_FORMATTER);
    }

    private Optional<Snapshot> findCurrentLinkByMcUserName(String mcusername) {
        if (mcusername != null) {
            try {
                return DiscordModBot.IDENTITY_DATABASE.getCurrentLinkByMinecraftName(mcusername);
            } catch (Exception e) {
                log.error("Error finding by MC username", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<Snapshot> findCurrentLinkByDiscordUserId(String discorduserid) {
        if (discorduserid != null) {
            try {
                return DiscordModBot.IDENTITY_DATABASE.getCurrentLinkByDiscordId(discorduserid);
            } catch (Exception e) {
                log.error("Error finding by Discord user ID", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<Snapshot> findCurrentLinkByMcUUID(UUID mcuuid) {
        if (mcuuid != null) {
            try {
                return DiscordModBot.IDENTITY_DATABASE.getCurrentLinkByUuid(mcuuid);
            } catch (Exception e) {
                log.error("Error finding by MC UUID", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<Snapshot> findCurrentLinkByDiscordUser(Member discorduser) {
        if (discorduser != null) {
            try {
                return DiscordModBot.IDENTITY_DATABASE.getCurrentLinkByDiscordId(discorduser.getId());
            } catch (Exception e) {
                log.error("Error finding by Discord user", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Search for an account in the SDLink database
     */
    private Optional<SDLinkAccount> findSDLinkAccount(String mcusername, String mcuuid, String discorduserid, Member discorduser) {
        try {
            // Search by Minecraft UUID first (most reliable)
            if (mcuuid != null) {
                try {
                    SDLinkAccount account = DatabaseManager.INSTANCE.findById(mcuuid, SDLinkAccount.class);
                    if (account != null) {
                        return Optional.of(account);
                    }
                } catch (Exception e) {
                    log.debug("No SDLink account found for UUID: {}", mcuuid);
                }
            }

            // Search by Discord User ID
            if (discorduserid != null) {
                SDLinkAccount account = findSDLinkAccountByDiscordId(discorduserid);
                if (account != null) {
                    return Optional.of(account);
                }
            }

            // Search by Discord User
            if (discorduser != null) {
                SDLinkAccount account = findSDLinkAccountByDiscordId(discorduser.getId());
                if (account != null) {
                    return Optional.of(account);
                }
            }

            // Search by Minecraft username (less reliable due to name changes)
            if (mcusername != null) {
                SDLinkAccount account = findSDLinkAccountByUsername(mcusername);
                if (account != null) {
                    return Optional.of(account);
                }
            }

        } catch (Exception e) {
            log.error("Error searching SDLink database", e);
        }

        return Optional.empty();
    }

    /**
     * Find SDLink account by Discord ID
     */
    private SDLinkAccount findSDLinkAccountByDiscordId(String discordId) {
        try {
            List<SDLinkAccount> allAccounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);
            return allAccounts.stream()
                    .filter(account -> discordId.equals(account.getDiscordID()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Error searching SDLink by Discord ID: {}", discordId);
            return null;
        }
    }

    /**
     * Find SDLink account by Minecraft username
     */
    private SDLinkAccount findSDLinkAccountByUsername(String username) {
        try {
            List<SDLinkAccount> allAccounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);
            return allAccounts.stream()
                    .filter(account -> username.equalsIgnoreCase(account.getUsername()) ||
                                     username.equalsIgnoreCase(account.getInGameName()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Error searching SDLink by username: {}", username);
            return null;
        }
    }

    /**
     * Send response showing SDLink account information (for accounts not yet synced to identity database)
     */
    private void sendSDLinkAccountResponse(SlashCommandEvent event, SDLinkAccount account, String searchedFor) {
        EmbedBuilder builder = new EmbedBuilder();

        // Determine status based on verification state
        boolean isFullyLinked = account.getDiscordID() != null && !account.getDiscordID().isEmpty();
        boolean isPendingVerification = account.getVerifyCode() != null && !account.getVerifyCode().isEmpty();

        if (isFullyLinked) {
            builder.setTitle("🔗 SDLink Account (Not Synced)");
            builder.setDescription("⚠️ This account is linked but hasn't been synced to the identity system yet.");
            builder.setColor(Color.ORANGE);
        } else if (isPendingVerification) {
            builder.setTitle("⏳ SDLink Account (Pending Verification)");
            builder.setDescription("This account is in the process of linking. Verification code: `" + account.getVerifyCode() + "`");
            builder.setColor(Color.YELLOW);
        } else {
            builder.setTitle("❓ SDLink Account (Unknown State)");
            builder.setDescription("Account found but in an unexpected state.");
            builder.setColor(Color.GRAY);
        }

        // Minecraft information
        builder.addField("**Minecraft Account**", "", false);
        builder.addField("Username", account.getUsername() != null ? account.getUsername() : "Unknown", true);
        builder.addField("In-Game Name", account.getInGameName() != null ? account.getInGameName() : "Unknown", true);
        builder.addField("UUID", account.getUuid() != null ? account.getUuid() : "Unknown", true);

        // Discord information
        builder.addField("**Discord Account**", "", false);
        if (isFullyLinked) {
            // Try to get Discord member for mention
            Member discordMember = null;
            boolean userLeftServer = false;
            try {
                discordMember = event.getGuild().retrieveMemberById(account.getDiscordID()).complete();
            } catch (Exception e) {
                // Member not in guild or error retrieving
                userLeftServer = true;
            }

            // If user left server, update the title and description
            if (userLeftServer) {
                builder.setTitle("🔗 SDLink Account (User Left Server)");
                builder.setDescription(builder.getDescriptionBuilder() + " ⚠️ User is no longer in the Discord server.");
            }

            String discordUserDisplay;
            if (discordMember != null) {
                discordUserDisplay = discordMember.getAsMention();
            } else if (userLeftServer) {
                discordUserDisplay = "~~User Left Server~~";
            } else {
                discordUserDisplay = "Unknown User";
            }

            builder.addField("Discord User", discordUserDisplay, true);
            builder.addField("Discord ID", account.getDiscordID(), true);
            builder.addBlankField(true); // For spacing
        } else if (isPendingVerification) {
            builder.addField("Status", "Awaiting Discord verification", true);
            builder.addField("Verification Code", "`" + account.getVerifyCode() + "`", true);
            builder.addBlankField(true); // For spacing
        } else {
            builder.addField("Status", "No Discord link found", true);
            builder.addBlankField(true);
            builder.addBlankField(true);
        }

        // Offline status
        if (account.isOffline()) {
            builder.addField("Account Type", "Offline Mode", true);
        } else {
            builder.addField("Account Type", "Online Mode", true);
        }

        // Footer
        builder.setFooter("SDLink Account • Searched for: " + searchedFor);

        event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(false).queue();
    }

    /**
     * Helper class to hold search parameters
     */
    private static class SearchParameters {
        final String mcusername;
        final String mcuuid;
        final String discorduserid;
        final Member discorduser;
        final String searchedFor;

        SearchParameters(String mcusername, String mcuuid, String discorduserid, Member discorduser, String searchedFor) {
            this.mcusername = mcusername;
            this.mcuuid = mcuuid;
            this.discorduserid = discorduserid;
            this.discorduser = discorduser;
            this.searchedFor = searchedFor;
        }
    }

    /**
     * Helper class to hold search results
     */
    private static class SearchResult {
        final Optional<Snapshot> snapshot;
        final List<Snapshot> allSnapshots;
        final boolean foundCurrent;

        SearchResult(Optional<Snapshot> snapshot, List<Snapshot> allSnapshots, boolean foundCurrent) {
            this.snapshot = snapshot;
            this.allSnapshots = allSnapshots;
            this.foundCurrent = foundCurrent;
        }
    }

    /**
     * Extract and validate command options
     */
    private SearchParameters extractAndValidateOptions(SlashCommandEvent event) {
        String mcusername = event.getOption("mcusername") != null ? event.getOption("mcusername").getAsString() : null;
        String mcuuid = event.getOption("mcuuid") != null ? event.getOption("mcuuid").getAsString() : null;
        String discorduserid = event.getOption("discorduserid") != null ? event.getOption("discorduserid").getAsString() : null;
        Member discorduser = event.getOption("discorduser") != null ? event.getOption("discorduser").getAsMember() : null;

        // Validate that exactly one option is provided
        int optionCount = 0;
        if (mcusername != null) optionCount++;
        if (mcuuid != null) optionCount++;
        if (discorduserid != null) optionCount++;
        if (discorduser != null) optionCount++;

        if (optionCount == 0) {
            event.getHook().sendMessage("❌ Please provide at least one search parameter (mcusername, mcuuid, discorduser, or discorduserid)").setEphemeral(true).queue();
            return null;
        }

        if (optionCount > 1) {
            event.getHook().sendMessage("❌ Please provide only 1 search parameter").setEphemeral(true).queue();
            return null;
        }

        // Determine search description
        String searchedFor;
        if (mcusername != null) {
            searchedFor = "Minecraft Username: " + mcusername;
        } else if (mcuuid != null) {
            searchedFor = "Minecraft UUID: " + mcuuid;
        } else if (discorduserid != null) {
            searchedFor = "Discord User ID: " + discorduserid;
        } else {
            searchedFor = "Discord User: " + discorduser.getEffectiveName();
        }

        return new SearchParameters(mcusername, mcuuid, discorduserid, discorduser, searchedFor);
    }

    /**
     * Search for account in the identity database
     */
    private SearchResult searchIdentityDatabase(SearchParameters params) {
        Optional<Snapshot> snapshot = Optional.empty();
        List<Snapshot> allSnapshots = null;
        boolean foundCurrent = false;

        try {
            if (params.mcusername != null) {
                // Try current first
                snapshot = findCurrentLinkByMcUserName(params.mcusername);
                foundCurrent = snapshot.isPresent();

                // Fall back to historical
                if (snapshot.isEmpty()) {
                    allSnapshots = DiscordModBot.IDENTITY_DATABASE.getAllSnapshotsByMinecraftName(params.mcusername);
                    if (!allSnapshots.isEmpty()) {
                        snapshot = allSnapshots.stream()
                                .max(Comparator.comparing(Snapshot::lastSeen));
                    }
                }
            } else if (params.mcuuid != null) {
                try {
                    UUID uuid = UUID.fromString(params.mcuuid);

                    // Try current first
                    snapshot = findCurrentLinkByMcUUID(uuid);
                    foundCurrent = snapshot.isPresent();

                    // Fall back to historical
                    if (snapshot.isEmpty()) {
                        allSnapshots = DiscordModBot.IDENTITY_DATABASE.getAllSnapshotsByUuid(uuid);
                        if (!allSnapshots.isEmpty()) {
                            snapshot = allSnapshots.stream()
                                    .max(Comparator.comparing(Snapshot::lastSeen));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format - will be handled by caller
                    return new SearchResult(Optional.empty(), null, false);
                }
            } else if (params.discorduserid != null) {
                // Try current first
                snapshot = findCurrentLinkByDiscordUserId(params.discorduserid);
                foundCurrent = snapshot.isPresent();

                // Fall back to historical
                if (snapshot.isEmpty()) {
                    allSnapshots = DiscordModBot.IDENTITY_DATABASE.getAllSnapshotsByDiscord(params.discorduserid);
                    if (!allSnapshots.isEmpty()) {
                        snapshot = allSnapshots.stream()
                                .max(Comparator.comparing(Snapshot::lastSeen));
                    }
                }
            } else if (params.discorduser != null) {
                // Try current first
                snapshot = findCurrentLinkByDiscordUser(params.discorduser);
                foundCurrent = snapshot.isPresent();

                // Fall back to historical
                if (snapshot.isEmpty()) {
                    allSnapshots = DiscordModBot.IDENTITY_DATABASE.getAllSnapshotsByDiscord(params.discorduser.getId());
                    if (!allSnapshots.isEmpty()) {
                        snapshot = allSnapshots.stream()
                                .max(Comparator.comparing(Snapshot::lastSeen));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error searching identity database", e);
            return new SearchResult(Optional.empty(), null, false);
        }

        return new SearchResult(snapshot, allSnapshots, foundCurrent);
    }

    /**
     * Handle SDLink database fallback when not found in identity database
     */
    private void handleSDLinkFallback(SlashCommandEvent event, SearchParameters params) {
        // Handle invalid UUID format
        if (params.mcuuid != null) {
            try {
                UUID.fromString(params.mcuuid);
            } catch (IllegalArgumentException e) {
                event.getHook().sendMessage("❌ Invalid UUID format: " + params.mcuuid).setEphemeral(true).queue();
                return;
            }
        }

        // Try to find in SDLink database as fallback
        Optional<SDLinkAccount> sdlinkAccount = findSDLinkAccount(params.mcusername, params.mcuuid, params.discorduserid, params.discorduser);

        if (sdlinkAccount.isPresent()) {
            // Found in SDLink but not in identity database - user hasn't been synced yet
            sendSDLinkAccountResponse(event, sdlinkAccount.get(), params.searchedFor);
        } else {
            // Not found anywhere
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("❌ No Account Found");
            builder.setDescription("No linked or pending account found for: " + params.searchedFor);
            builder.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(false).queue();
        }
    }

    /**
     * Build and send the identity profile response
     */
    private void sendIdentityProfileResponse(SlashCommandEvent event, SearchResult result, SearchParameters params) {
        try {
            // Get the full identity profile
            Snapshot current = result.snapshot.orElse(null);
            if (current == null) {
                event.getHook().sendMessage("❌ No snapshot found").setEphemeral(true).queue();
                return;
            }

            Optional<IdentityProfile> profileOpt = DiscordModBot.IDENTITY_DATABASE.getIdentityProfileBySnapshot(current.snapshotId());

            if (profileOpt.isEmpty()) {
                event.getHook().sendMessage("❌ Failed to retrieve identity profile").setEphemeral(true).queue();
                return;
            }

            IdentityProfile profile = profileOpt.get();

            // Get all snapshots for this identity if it wasn't already retrieved
            List<Snapshot> allSnapshots = result.allSnapshots;
            if (allSnapshots == null) {
                allSnapshots = DiscordModBot.IDENTITY_DATABASE.getAllSnapshotsForProfile(profile.identityProfileId());
            }

            // Build the response embed
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);

            // Set title and description based on status
            setIdentityProfileTitleAndDescription(builder, result.foundCurrent, event, current);

            // Add current status section
            addCurrentStatusSection(builder, current, result.foundCurrent, event);

            // Add account history section
            addAccountHistorySection(builder, allSnapshots, event);

            // Add footer
            builder.setFooter(String.format(
                    "Identity created: %s • Total accounts: %d MC, %d Discord • Searched for: %s",
                    formatTimestamp(profile.firstEverSeen()),
                    profile.uniqueMinecraftAccounts(),
                    profile.uniqueDiscordAccounts(),
                    params.searchedFor
            ));

            event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(false).queue();
        } catch (Exception e) {
            log.error("Error building identity profile response", e);
            event.getHook().sendMessage("❌ Error retrieving identity profile information").setEphemeral(true).queue();
        }
    }

    /**
     * Set the title and description for identity profile embed
     */
    private void setIdentityProfileTitleAndDescription(EmbedBuilder builder, boolean foundCurrent, SlashCommandEvent event, Snapshot current) {
        if (foundCurrent) {
            builder.setTitle("🔍 Identity Profile");
        } else {
            builder.setTitle("🔍 Identity Profile (Historical)");
            builder.setDescription("⚠️ This account is no longer linked. Showing last known information.");
        }

        // Check if user left server and update title/description
        try {
            Member discordMember = event.getGuild().retrieveMemberById(current.discordId()).complete();
            if (discordMember == null && foundCurrent) {
                builder.setTitle("🔍 Identity Profile (User Left Server)");
                if (builder.getDescriptionBuilder().isEmpty()) {
                    builder.setDescription("⚠️ This user is no longer in the Discord server.");
                } else {
                    builder.setDescription(builder.getDescriptionBuilder() + " ⚠️ User is no longer in the Discord server.");
                }
            }
        } catch (Exception e) {
            if (foundCurrent) {
                builder.setTitle("🔍 Identity Profile (User Left Server)");
                if (builder.getDescriptionBuilder().isEmpty()) {
                    builder.setDescription("⚠️ This user is no longer in the Discord server.");
                } else {
                    builder.setDescription(builder.getDescriptionBuilder() + " ⚠️ User is no longer in the Discord server.");
                }
            }
        }
    }

    /**
     * Add the current status section to the embed
     */
    private void addCurrentStatusSection(EmbedBuilder builder, Snapshot current, boolean foundCurrent, SlashCommandEvent event) {
        builder.addField(foundCurrent ? "**Current Status**" : "**Last Known Status**", "", false);
        builder.addField("Minecraft Name", current.minecraftName(), true);
        builder.addField("Minecraft UUID", current.minecraftUuid().toString(), true);
        builder.addBlankField(true); // For spacing

        // Discord user information with server status check
        String discordUserDisplay = getDiscordUserDisplay(current.discordId(), event);
        builder.addField("Discord User", discordUserDisplay, true);
        builder.addField("Discord ID", current.discordId(), true);
        builder.addBlankField(true); // For spacing
    }

    /**
     * Get Discord user display string with server status
     */
    private String getDiscordUserDisplay(String discordId, SlashCommandEvent event) {
        try {
            Member discordMember = event.getGuild().retrieveMemberById(discordId).complete();
            if (discordMember != null) {
                return discordMember.getAsMention();
            } else {
                return "~~User Left Server~~";
            }
        } catch (Exception e) {
            return "~~User Left Server~~";
        }
    }

    /**
     * Add the account history section to the embed
     */
    private void addAccountHistorySection(EmbedBuilder builder, List<Snapshot> allSnapshots, SlashCommandEvent event) {
        builder.addField("", "**Account History**", false);

        // Get unique Minecraft accounts
        List<String> minecraftAccounts = allSnapshots.stream()
                .collect(Collectors.groupingBy(Snapshot::minecraftUuid))
                .entrySet().stream()
                .map(entry -> {
                    UUID uuid = entry.getKey();
                    List<Snapshot> snapshots = entry.getValue();

                    // Get all names used with this UUID
                    String names = snapshots.stream()
                            .map(Snapshot::minecraftName)
                            .distinct()
                            .collect(Collectors.joining(" → "));

                    Optional<Snapshot> first = snapshots.stream()
                            .min(Comparator.comparing(Snapshot::firstSeen));
                    Optional<Snapshot> last = snapshots.stream()
                            .max(Comparator.comparing(Snapshot::lastSeen));

                    boolean isCurrent = snapshots.stream().anyMatch(Snapshot::isCurrent);
                    String currentMarker = isCurrent ? " ✅" : "";

                    return String.format("**%s**%s\n`%s`\nUsed: %s - %s",
                            names,
                            currentMarker,
                            uuid.toString(),
                            (first.map(s -> formatTimestamp(s.firstSeen())).orElse("-")),
                            isCurrent ? "Present" : (last.map(s -> formatTimestamp(s.lastSeen())).orElse("-")));
                })
                .collect(Collectors.toList());

        if (!minecraftAccounts.isEmpty()) {
            builder.addField("Minecraft Accounts (" + minecraftAccounts.size() + ")",
                    String.join("\n\n", minecraftAccounts),
                    false);
        }

        // Get unique Discord accounts with server status
        List<String> discordAccounts = allSnapshots.stream()
                .collect(Collectors.groupingBy(Snapshot::discordId))
                .entrySet().stream()
                .map(entry -> {
                    String discordId = entry.getKey();
                    List<Snapshot> snapshots = entry.getValue();

                    Optional<Snapshot> first = snapshots.stream()
                            .min(Comparator.comparing(Snapshot::firstSeen));
                    Optional<Snapshot> last = snapshots.stream()
                            .max(Comparator.comparing(Snapshot::lastSeen));

                    boolean isCurrent = snapshots.stream().anyMatch(Snapshot::isCurrent);
                    String currentMarker = isCurrent ? " ✅" : "";

                    // Check if user is still in the server
                    boolean userInServer = true;
                    String userDisplayStatus = "";
                    try {
                        Member historyDiscordMember = event.getGuild().retrieveMemberById(discordId).complete();
                        if (historyDiscordMember == null) {
                            userInServer = false;
                        }
                    } catch (Exception e) {
                        userInServer = false;
                    }

                    if (!userInServer) {
                        userDisplayStatus = " 🚪"; // Door emoji to indicate left server
                    }

                    String timeRange = (first.map(s -> formatTimestamp(s.firstSeen())).orElse("-")) + " - " +
                            (isCurrent ? "Present" : (last.map(s -> formatTimestamp(s.lastSeen())).orElse("-")));

                    return String.format("%s%s `%s`%s\nLinked: %s\n",
                            currentMarker,
                            userInServer ? "" : "~~",  // Start strikethrough if user left
                            discordId,
                            userInServer ? "" : "~~",  // End strikethrough if user left
                            timeRange) +
                            (userInServer ? "" : "*(User left server)*") + userDisplayStatus;
                })
                .collect(Collectors.toList());

        if (!discordAccounts.isEmpty()) {
            builder.addField("Discord Accounts (" + discordAccounts.size() + ")",
                    String.join("\n\n", discordAccounts),
                    false);
        }
    }
}

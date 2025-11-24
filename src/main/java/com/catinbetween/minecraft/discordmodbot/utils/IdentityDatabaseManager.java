package com.catinbetween.minecraft.discordmodbot.utils;

import com.catinbetween.minecraft.discordmodbot.identity.model.IdentityProfile;
import com.catinbetween.minecraft.discordmodbot.identity.model.Snapshot;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.*;

public class IdentityDatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityDatabaseManager.class);
    private static final File DB_FILE = FabricLoader.getInstance().getConfigDir().resolve("discordmodbot/mcmodbot.db").toFile();
    private Connection connection;

    public void initialize() {
        try {
            File parentDir = DB_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                LOGGER.info("Created database directory {}", parentDir.getAbsolutePath());
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            LOGGER.info("Connected to SQLite database: {}", DB_FILE);

            createTables();
        } catch (SecurityException e) {
            LOGGER.error("Failed to create database directory", e);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialise database", e);
        }
        LOGGER.info("Initialized Database successfully");
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.warn("Database connection was closed, reopening...");
            String url = "jdbc:sqlite:" + DB_FILE;
            connection = DriverManager.getConnection(url);

            // Reconfigure connection
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
            stmt.close();

            LOGGER.info("Database connection re-established");
        }
    }

    // Snapshots of link states over time
    private void createTables() throws SQLException {
        ensureConnection();
        String createLinkSnapshotsTableSQL = """
                    CREATE TABLE IF NOT EXISTS link_snapshots (
                        snapshot_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        minecraft_uuid TEXT NOT NULL,
                        minecraft_name TEXT NOT NULL,
                        discord_id TEXT NOT NULL,
                        first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        is_current BOOLEAN DEFAULT 1
                    )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createLinkSnapshotsTableSQL);
            LOGGER.debug("link_snapshots table created or already exists");
        }

        // Quick lookup table for "who is this person across all their accounts?"
        String createIdentityProfilesTableSQL = """
                CREATE TABLE IF NOT EXISTS identity_profiles (
                    identity_profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createIdentityProfilesTableSQL);
            LOGGER.debug("identity_profiles table created or already exists");
        }


        // Links snapshots to identity profiles (one person, many accounts over time)
        String createIdentityProfileMembersTableSQL = """
                CREATE TABLE IF NOT EXISTS identity_profile_members (
                     member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                     identity_profile_id INTEGER NOT NULL,
                     snapshot_id INTEGER NOT NULL,
                     added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     FOREIGN KEY (identity_profile_id) REFERENCES identity_profiles(identity_profile_id) ON DELETE CASCADE,
                     FOREIGN KEY (snapshot_id) REFERENCES link_snapshots(snapshot_id) ON DELETE CASCADE,
                     UNIQUE(snapshot_id)  -- Each snapshot belongs to only one identityProfile
                 )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createIdentityProfileMembersTableSQL);
            LOGGER.debug("identity_profile_members table created or already exists");
        }


        // Create an index for faster lookups
        String createSnapshotsUuidIndexSQL = "CREATE INDEX IF NOT EXISTS idx_snapshots_uuid ON link_snapshots(minecraft_uuid)";
        String createSnapshotsDiscordIndexSQL = "CREATE INDEX IF NOT EXISTS idx_snapshots_discord ON link_snapshots(discord_id)";
        String createSnapshotsMcNameIndexSQL = "CREATE INDEX IF NOT EXISTS idx_snapshots_mc_name ON link_snapshots(minecraft_name)";
        String createSnapshotsCurrentIndexSQL = "CREATE INDEX IF NOT EXISTS idx_snapshots_current ON link_snapshots(is_current)";
        String createIdentityProfileMembersIndexSQL = "CREATE INDEX IF NOT EXISTS idx_identity_profile_members ON identity_profile_members(identity_profile_id)";
        String createIdentityProfileMembersSnapshotIndexSQL = "CREATE INDEX IF NOT EXISTS idx_identity_profile_members_snapshot ON identity_profile_members(snapshot_id)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSnapshotsUuidIndexSQL);
            LOGGER.debug("idx_snapshots_uuid index created or already exists");
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSnapshotsDiscordIndexSQL);
            LOGGER.debug("idx_snapshots_discord index created or already exists");
        }try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSnapshotsMcNameIndexSQL);
            LOGGER.debug("idx_snapshots_mc_name index created or already exists");
        }try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSnapshotsCurrentIndexSQL);
            LOGGER.debug("idx_snapshots_current index created or already exists");
        }try (Statement stmt = connection.createStatement()) {
            stmt.execute(createIdentityProfileMembersIndexSQL);
            LOGGER.debug("idx_identity_profile_members index created or already exists");
        } try (Statement stmt = connection.createStatement()) {
            stmt.execute(createIdentityProfileMembersSnapshotIndexSQL);
            LOGGER.debug("idx_identity_profile_members_snapshot index created or already exists");
        }
    }

    /**
     * Check if exact link state already exists and is current
     */
    public Optional<Integer> getCurrentSnapshot(UUID minecraftUuid, String discordId) throws SQLException {
        String sql = "SELECT snapshot_id FROM link_snapshots " +
                "WHERE minecraft_uuid = ? AND discord_id = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());
            stmt.setString(2, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("snapshot_id"));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find current snapshot for a Minecraft UUID
     */
    public Optional<Snapshot> getCurrentSnapshotByUuid(UUID minecraftUuid) throws SQLException {
        String sql = "SELECT snapshot_id, discord_id FROM link_snapshots " +
                "WHERE minecraft_uuid = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find current snapshot for a Discord ID
     */
    public Optional<Snapshot> getCurrentSnapshotByDiscord(String discordId) throws SQLException {
        String sql = "SELECT snapshot_id, minecraft_uuid FROM link_snapshots " +
                "WHERE discord_id = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Mark snapshot as no longer current
     */
    public void markSnapshotAsOld(Integer snapshotId) throws SQLException {
        String sql = "UPDATE link_snapshots " +
                "SET is_current = 0, last_seen = CURRENT_TIMESTAMP " +
                "WHERE snapshot_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, snapshotId);
            stmt.executeUpdate();
        }
    }

    /**
     * Mark all current snapshots for a UUID as no longer current
     */
    public void markSnapshotsAsOldByUuid(UUID minecraftUuid) throws SQLException {
        String sql = "UPDATE link_snapshots " +
                "SET is_current = 0, last_seen = CURRENT_TIMESTAMP " +
                "WHERE minecraft_uuid = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Mark all current snapshots for a Discord ID as no longer current
     */
    public void markSnapshotsAsOldByDiscord(String discordId) throws SQLException {
        String sql = "UPDATE link_snapshots " +
                "SET is_current = 0, last_seen = CURRENT_TIMESTAMP " +
                "WHERE discord_id = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            stmt.executeUpdate();
        }
    }

    /**
     * Create new link snapshot
     */
    public Optional<Integer> createSnapshot(UUID minecraftUuid, String minecraftName,
                                  String discordId) throws SQLException {
        String sql = "INSERT INTO link_snapshots " +
                "(minecraft_uuid, minecraft_name, discord_id, is_current) " +
                "VALUES (?, ?, ?, 1)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, minecraftUuid.toString());
            stmt.setString(2, minecraftName);
            stmt.setString(3, discordId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt(1));
                }
                throw new SQLException("Failed to get generated snapshot_id");
            }
        }
    }

    /**
     * Update last_seen timestamp for existing snapshot
     */
    public void updateLastSeen(UUID minecraftUuid, String discordId) throws SQLException {
        String sql = "UPDATE link_snapshots " +
                "SET last_seen = CURRENT_TIMESTAMP " +
                "WHERE minecraft_uuid = ? AND discord_id = ? AND is_current = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());
            stmt.setString(2, discordId);
            stmt.executeUpdate();
        }
    }

    /**
     * Find identity profile ID by Minecraft UUID
     */
    public Optional<Integer> findIdentityProfileByUuid(UUID minecraftUuid) throws SQLException {
        String sql = "SELECT DISTINCT ip.identity_profile_id " +
                "FROM identity_profile_members ip " +
                "JOIN link_snapshots ls ON ip.snapshot_id = ls.snapshot_id " +
                "WHERE ls.minecraft_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("identity_profile_id"));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Find identity profile ID by Discord ID
     */
    public Optional<Integer> findIdentityProfileByDiscordId(String discordId) throws SQLException {
        String sql = "SELECT DISTINCT ip.identity_profile_id " +
                "FROM identity_profile_members ip " +
                "JOIN link_snapshots ls ON ip.snapshot_id = ls.snapshot_id " +
                "WHERE ls.discord_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("identity_profile_id"));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Create new identity profile
     */
    public Optional<Integer> createIdentityProfile() throws SQLException {
        String sql = "INSERT INTO identity_profiles DEFAULT VALUES";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt(1));
                }
                throw new SQLException("Failed to get generated identity_profile_id");
            }
        }
    }

    /**
     * Create new identity profile with notes
     */
    public Optional<Integer> createIdentityProfile(String notes) throws SQLException {
        String sql = "INSERT INTO identity_profiles (notes) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, notes);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt(1));
                }
                throw new SQLException("Failed to get generated identity_profile_id");
            }
        }
    }

    /**
     * Add snapshot to existing identity profile
     */
    public void addSnapshotToIdentityProfile(Integer identityProfileId, Integer snapshotId) throws SQLException {
        String sql = "INSERT INTO identity_profile_members (identity_profile_id, snapshot_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);
            stmt.setInt(2, snapshotId);
            stmt.executeUpdate();
        }
    }

    /**
     * Merge two identity profiles
     */
    public void mergeIdentityProfiles(Integer targetIdentityProfileId, Integer sourceIdentityProfileId) throws SQLException {
        String sql = "UPDATE identity_profile_members SET identity_profile_id = ? WHERE identity_profile_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, targetIdentityProfileId);
            stmt.setInt(2, sourceIdentityProfileId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get all snapshots by Minecraft UUID
     */
    public List<Snapshot> getAllSnapshotsByUuid(UUID minecraftUuid) throws SQLException {
        String sql = "SELECT DISTINCT ls.* " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ( " +
                "    SELECT ip2.identity_profile_id " +
                "    FROM identity_profile_members ip2 " +
                "    JOIN link_snapshots ls2 ON ip2.snapshot_id = ls2.snapshot_id " +
                "    WHERE ls2.minecraft_uuid = ? " +
                "    LIMIT 1 " +
                ") " +
                "ORDER BY ls.last_seen DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> snapshots = new ArrayList<>();
                while (rs.next()) {
                    snapshots.add(mapResultSetToSnapshot(rs));
                }
                return snapshots;
            }
        }
    }

    /**
     * Get all snapshots for an identity profile
     */
    public List<Snapshot> getAllSnapshotsForProfile(Integer identityProfileId) throws SQLException {
        // Get all snapshots belonging to this profile
        String sql = "SELECT ls.* " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ? " +
                "ORDER BY ls.last_seen DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> snapshots = new ArrayList<>();
                while (rs.next()) {
                    snapshots.add(mapResultSetToSnapshot(rs));
                }
                return snapshots;
            }
        }
    }

    /**
     * Get all snapshots by Discord ID
     */
    public List<Snapshot> getAllSnapshotsByDiscord(String discordId) throws SQLException {
        String sql = "SELECT DISTINCT ls.* " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ( " +
                "    SELECT ip2.identity_profile_id " +
                "    FROM identity_profile_members ip2 " +
                "    JOIN link_snapshots ls2 ON ip2.snapshot_id = ls2.snapshot_id " +
                "    WHERE ls2.discord_id = ? " +
                "    LIMIT 1 " +
                ") " +
                "ORDER BY ls.last_seen DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> snapshots = new ArrayList<>();
                while (rs.next()) {
                    snapshots.add(mapResultSetToSnapshot(rs));
                }
                return snapshots;
            }
        }
    }

    /**
     * Get all snapshots by Minecraft name
     */
    public List<Snapshot> getAllSnapshotsByMinecraftName(String minecraftName) throws SQLException {
        String sql = "SELECT DISTINCT ls.* " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ( " +
                "    SELECT ip2.identity_profile_id " +
                "    FROM identity_profile_members ip2 " +
                "    JOIN link_snapshots ls2 ON ip2.snapshot_id = ls2.snapshot_id " +
                "    WHERE ls2.minecraft_name = ? " +
                "    LIMIT 1 " +
                ") " +
                "ORDER BY ls.last_seen DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftName);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> snapshots = new ArrayList<>();
                while (rs.next()) {
                    snapshots.add(mapResultSetToSnapshot(rs));
                }
                return snapshots;
            }
        }
    }

    /**
     * Get current link by UUID
     */
    public Optional<Snapshot> getCurrentLinkByUuid(UUID minecraftUuid) throws SQLException {
        String sql = "SELECT * FROM link_snapshots " +
                "WHERE minecraft_uuid = ? AND is_current = 1 LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Get current link by Discord ID
     */
    public Optional<Snapshot> getCurrentLinkByDiscordId(String discordId) throws SQLException {
        String sql = "SELECT * FROM link_snapshots " +
                "WHERE discord_id = ? AND is_current = 1 LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Get current link by Minecraft name
     */
    public Optional<Snapshot> getCurrentLinkByMinecraftName(String minecraftName) throws SQLException {
        String sql = "SELECT * FROM link_snapshots " +
                "WHERE minecraft_name = ? AND is_current = 1 LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Get complete identity profile for an IdentityProfile id
     */
    public Optional<IdentityProfile> getIdentityProfile(Integer identityProfileId) throws SQLException {
        String sql = "SELECT " +
                "    ic.identity_profile_id, " +
                "    ic.notes, " +
                "    COUNT(DISTINCT ls.minecraft_uuid) as unique_minecraft_accounts, " +
                "    COUNT(DISTINCT ls.discord_id) as unique_discord_accounts, " +
                "    GROUP_CONCAT(DISTINCT ls.minecraft_name) as all_minecraft_names, " +
                "    GROUP_CONCAT(DISTINCT ls.discord_id) as all_discord_ids, " +
                "    MIN(ls.first_seen) as first_ever_seen, " +
                "    MAX(ls.last_seen) as last_seen " +
                "FROM identity_profiles ic " +
                "JOIN identity_profile_members ip ON ic.identity_profile_id = ip.identity_profile_id " +
                "JOIN link_snapshots ls ON ip.snapshot_id = ls.snapshot_id " +
                "WHERE ic.identity_profile_id = ? " +
                "GROUP BY ic.identity_profile_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIdentityProfile(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Get complete identity profile for a snapshot ID
     */
    public Optional<IdentityProfile> getIdentityProfileBySnapshot(Integer snapshotId) throws SQLException {
        // Get the profile ID from this snapshot, then get the full profile
        String sql = "SELECT ic.identity_profile_id, " +
                "    ic.notes, " +
                "    COUNT(DISTINCT ls.minecraft_uuid) as unique_minecraft_accounts, " +
                "    COUNT(DISTINCT ls.discord_id) as unique_discord_accounts, " +
                "    GROUP_CONCAT(DISTINCT ls.minecraft_name) as all_minecraft_names, " +
                "    GROUP_CONCAT(DISTINCT ls.discord_id) as all_discord_ids, " +
                "    MIN(ls.first_seen) as first_ever_seen, " +
                "    MAX(ls.last_seen) as last_seen " +
                "FROM identity_profiles ic " +
                "JOIN identity_profile_members ip ON ic.identity_profile_id = ip.identity_profile_id " +
                "JOIN link_snapshots ls ON ip.snapshot_id = ls.snapshot_id " +
                "WHERE ic.identity_profile_id = ( " +
                "    SELECT identity_profile_id FROM identity_profile_members WHERE snapshot_id = ? LIMIT 1" +
                ") " +
                "GROUP BY ic.identity_profile_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, snapshotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIdentityProfile(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Get all Minecraft UUIDs for an identity profile
     */
    public List<UUID> getAllMinecraftUuids(Integer identityProfileId) throws SQLException {
        String sql = "SELECT DISTINCT ls.minecraft_uuid " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<UUID> uuids = new ArrayList<>();
                while (rs.next()) {
                    uuids.add(UUID.fromString(rs.getString("minecraft_uuid")));
                }
                return uuids;
            }
        }
    }

    /**
     * Get all Discord IDs for an identity profile
     */
    public List<String> getAllDiscordIds(Integer identityProfileId) throws SQLException {
        String sql = "SELECT DISTINCT ls.discord_id " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> discordIds = new ArrayList<>();
                while (rs.next()) {
                    discordIds.add(rs.getString("discord_id"));
                }
                return discordIds;
            }
        }
    }

    /**
     * Get all Minecraft names for an identity profile
     */
    public List<String> getAllMinecraftNames(Integer identityProfileId) throws SQLException {
        String sql = "SELECT DISTINCT ls.minecraft_name " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString("minecraft_name"));
                }
                return names;
            }
        }
    }

    /**
     * Find stale current flags
     */
    public List<Snapshot> findStaleCurrentFlags(int daysOld) throws SQLException {
        String sql = "SELECT * FROM link_snapshots " +
                "WHERE is_current = 1 AND last_seen < datetime('now', '-' || ? || ' days')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, daysOld);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> snapshots = new ArrayList<>();
                while (rs.next()) {
                    snapshots.add(mapResultSetToSnapshot(rs));
                }
                return snapshots;
            }
        }
    }

    /**
     * Get chronological timeline for an identity profile
     */
    public List<Snapshot> getIdentityProfileTimeline(Integer identityProfileId) throws SQLException {
        String sql = "SELECT ls.* " +
                "FROM link_snapshots ls " +
                "JOIN identity_profile_members ip ON ls.snapshot_id = ip.snapshot_id " +
                "WHERE ip.identity_profile_id = ? " +
                "ORDER BY ls.first_seen ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, identityProfileId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Snapshot> timeline = new ArrayList<>();
                while (rs.next()) {
                    timeline.add(mapResultSetToSnapshot(rs));
                }
                return timeline;
            }
        }
    }


    private Snapshot mapResultSetToSnapshot(ResultSet rs) throws SQLException {
        return new Snapshot(
            rs.getInt("snapshot_id"),
            UUID.fromString(rs.getString("minecraft_uuid")),
            rs.getString("minecraft_name"),
            rs.getString("discord_id"),
            rs.getTimestamp("first_seen"),
            rs.getTimestamp("last_seen"),
            rs.getBoolean("is_current")
        );
    }

    private IdentityProfile mapResultSetToIdentityProfile(ResultSet rs) throws SQLException {
        return new IdentityProfile(
            rs.getInt("identity_profile_id"),
            rs.getInt("unique_minecraft_accounts"),
            rs.getInt("unique_discord_accounts"),
            List.of(rs.getString("all_minecraft_names").split(",")),
            List.of(rs.getString("all_discord_ids").split(",")),
            rs.getTimestamp("first_ever_seen"),
            rs.getTimestamp("last_seen"),
            rs.getString("notes")
        );
    }

}

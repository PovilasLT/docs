// ========================================
// REFACTORING EXAMPLES: Before & After
// ========================================

// ============================================================
// EXAMPLE 1: SQL.java - Complete Refactoring
// ============================================================

// ❌ BEFORE (Your Current Code):
package lt.povilasc.funkcijos.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lt.povilasc.funkcijos.Main;

public class SQL {
    private static Connection con;

    public static void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection(
                    "jdbc:mysql://" + 
                    Main.getMainPlugin().getConfig().getString("SQL.hostname") + ":" + 
                    Main.getMainPlugin().getConfig().getString("SQL.port") + "/" + 
                    Main.getMainPlugin().getConfig().getString("SQL.database") + 
                    "?autoReconnect=true", 
                    Main.getMainPlugin().getConfig().getString("SQL.username"), 
                    Main.getMainPlugin().getConfig().getString("SQL.password")
                );
                Util.logger("Sekmingai prisijungta prie SQL");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                Util.logger("Sekmingai atsijungta nuo SQL");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnected() {
        return (con == null ? false : true);
    }

    public static Connection getConnection() {
        return con;
    }
}

// ✅ AFTER (Improved Version):
package lt.povilasc.funkcijos.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lt.povilasc.funkcijos.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager using HikariCP connection pooling.
 * Provides thread-safe database connections for the plugin.
 * 
 * @author YourName
 * @version 2.0
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger("MCPrison-DB");
    private static DatabaseManager instance;
    
    private final HikariDataSource dataSource;
    private final Main plugin;

    /**
     * Private constructor for singleton pattern.
     * 
     * @param plugin The main plugin instance
     */
    private DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.dataSource = createDataSource();
    }

    /**
     * Initializes the database manager.
     * 
     * @param plugin The main plugin instance
     * @return The DatabaseManager instance
     * @throws SQLException if connection fails
     */
    public static DatabaseManager initialize(Main plugin) throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager(plugin);
            instance.testConnection();
        }
        return instance;
    }

    /**
     * Gets the singleton instance.
     * 
     * @return The DatabaseManager instance
     * @throws IllegalStateException if not initialized
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager not initialized!");
        }
        return instance;
    }

    /**
     * Creates and configures the HikariCP data source.
     * 
     * @return Configured HikariDataSource
     */
    private HikariDataSource createDataSource() {
        FileConfiguration config = plugin.getConfig();
        
        HikariConfig hikariConfig = new HikariConfig();
        
        // Build JDBC URL
        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false&characterEncoding=utf8",
            config.getString("SQL.hostname", "localhost"),
            config.getString("SQL.port", "3306"),
            config.getString("SQL.database", "mcprison")
        );
        
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getString("SQL.username", "root"));
        hikariConfig.setPassword(config.getString("SQL.password", ""));
        
        // Connection pool settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        
        // Performance settings
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        hikariConfig.setPoolName("MCPrison-Pool");
        
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Gets a connection from the pool.
     * 
     * @return A database connection
     * @throws SQLException if unable to get connection
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is closed or not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Tests the database connection.
     * 
     * @throws SQLException if connection test fails
     */
    private void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Connection validation failed");
            }
            LOGGER.info("Successfully connected to database");
        }
    }

    /**
     * Checks if the database is connected.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Closes the database connection pool.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database connection pool closed");
        }
    }

    /**
     * Executes a database query safely with automatic resource management.
     * 
     * @param query The SQL query
     * @param handler The result set handler
     * @param params Query parameters
     */
    public void executeQuery(String query, ResultSetHandler handler, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                handler.handle(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Query execution failed: " + query, e);
        }
    }

    /**
     * Executes a database update safely.
     * 
     * @param query The SQL update/insert/delete query
     * @param params Query parameters
     * @return Number of affected rows
     */
    public int executeUpdate(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            return ps.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Update execution failed: " + query, e);
            return 0;
        }
    }

    /**
     * Functional interface for handling result sets.
     */
    @FunctionalInterface
    public interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }
}


// ============================================================
// EXAMPLE 2: Player Data Access - Repository Pattern
// ============================================================

// ❌ BEFORE (Scattered SQL queries):
// In various classes, you probably have code like:
Connection con = SQL.getConnection();
String query = "SELECT * FROM players WHERE name = '" + playerName + "'"; // SQL Injection!
Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(query);
// No resource cleanup!


// ✅ AFTER (Clean Repository Pattern):
package lt.povilasc.funkcijos.database;

import lt.povilasc.funkcijos.objects.Zaidejas;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for player data access.
 * Handles all database operations for player entities.
 */
public class PlayerRepository {
    
    private final DatabaseManager db;

    public PlayerRepository(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Finds a player by UUID.
     * 
     * @param uuid Player's UUID
     * @return Optional containing the player if found
     */
    public Optional<Zaidejas> findByUuid(UUID uuid) {
        final Zaidejas[] result = {null};
        
        String query = "SELECT * FROM players WHERE uuid = ?";
        
        db.executeQuery(query, rs -> {
            if (rs.next()) {
                result[0] = mapResultSetToZaidejas(rs);
            }
        }, uuid.toString());
        
        return Optional.ofNullable(result[0]);
    }

    /**
     * Finds a player by name.
     * 
     * @param name Player's name
     * @return Optional containing the player if found
     */
    public Optional<Zaidejas> findByName(String name) {
        final Zaidejas[] result = {null};
        
        String query = "SELECT * FROM players WHERE name = ? LIMIT 1";
        
        db.executeQuery(query, rs -> {
            if (rs.next()) {
                result[0] = mapResultSetToZaidejas(rs);
            }
        }, name);
        
        return Optional.ofNullable(result[0]);
    }

    /**
     * Saves or updates a player.
     * 
     * @param zaidejas The player to save
     * @return true if successful
     */
    public boolean save(Zaidejas zaidejas) {
        String query = 
            "INSERT INTO players (uuid, name, balance, rank, prison_xp) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "name = VALUES(name), " +
            "balance = VALUES(balance), " +
            "rank = VALUES(rank), " +
            "prison_xp = VALUES(prison_xp)";
        
        int affected = db.executeUpdate(query,
            zaidejas.getUuid().toString(),
            zaidejas.getName(),
            zaidejas.getBalance(),
            zaidejas.getRank(),
            zaidejas.getPrisonXp()
        );
        
        return affected > 0;
    }

    /**
     * Deletes a player by UUID.
     * 
     * @param uuid Player's UUID
     * @return true if deleted
     */
    public boolean delete(UUID uuid) {
        String query = "DELETE FROM players WHERE uuid = ?";
        return db.executeUpdate(query, uuid.toString()) > 0;
    }

    /**
     * Updates player's balance.
     * 
     * @param uuid Player's UUID
     * @param newBalance The new balance
     * @return true if successful
     */
    public boolean updateBalance(UUID uuid, double newBalance) {
        String query = "UPDATE players SET balance = ? WHERE uuid = ?";
        return db.executeUpdate(query, newBalance, uuid.toString()) > 0;
    }

    /**
     * Maps a ResultSet row to a Zaidejas object.
     * 
     * @param rs The ResultSet
     * @return The mapped Zaidejas object
     * @throws SQLException if mapping fails
     */
    private Zaidejas mapResultSetToZaidejas(ResultSet rs) throws SQLException {
        Zaidejas zaidejas = new Zaidejas();
        zaidejas.setUuid(UUID.fromString(rs.getString("uuid")));
        zaidejas.setName(rs.getString("name"));
        zaidejas.setBalance(rs.getDouble("balance"));
        zaidejas.setRank(rs.getString("rank"));
        zaidejas.setPrisonXp(rs.getInt("prison_xp"));
        return zaidejas;
    }
}


// ============================================================
// EXAMPLE 3: Main.java onEnable - Before & After
// ============================================================

// ❌ BEFORE (240 lines in one method):
@Override
public void onEnable() {
    plugin = this;
    new DefaultConfig(this);
    plugin.getConfig().options().copyDefaults(true);
    SQL.connect();
    if (!SQL.isConnected()) {
        Bukkit.getServer().setWhitelist(true);
        Util.logger("SQL neveikia, serveris isjungiamas!");
        Bukkit.getServer().shutdown();
    }
    // ... 230 more lines ...
}


// ✅ AFTER (Clean, organized):
@Override
public void onEnable() {
    plugin = this;
    
    try {
        initializePlugin();
        getLogger().info("MCPrison plugin enabled successfully!");
    } catch (PluginInitializationException e) {
        getLogger().severe("Failed to initialize plugin: " + e.getMessage());
        shutdownServer(e.getMessage());
    }
}

/**
 * Initializes all plugin components.
 * 
 * @throws PluginInitializationException if initialization fails
 */
private void initializePlugin() throws PluginInitializationException {
    // Load configurations
    loadConfigurations();
    
    // Initialize dependencies
    initializeDependencies();
    
    // Setup systems
    setupDatabase();
    setupEventListeners();
    setupCommands();
    setupScheduledTasks();
    
    // Initialize modules
    initializeModules();
}

/**
 * Loads all configuration files.
 */
private void loadConfigurations() {
    new DefaultConfig(this);
    getConfig().options().copyDefaults(true);
    saveConfig();
    
    CMCFG = new CMConfig(this);
    MinesCFG = new MinesConfig(this);
    MotdCFG = new MotdConfig(this);
    // ... other configs
}

/**
 * Initializes required plugin dependencies.
 * 
 * @throws PluginInitializationException if a dependency is missing
 */
private void initializeDependencies() throws PluginInitializationException {
    // LuckPerms
    try {
        lp = LuckPermsProvider.get();
        getLogger().info("✓ Connected to LuckPerms");
    } catch (IllegalStateException e) {
        throw new PluginInitializationException("LuckPerms not found");
    }
    
    // WorldGuard
    wgs = WorldGuard.getInstance();
    if (wgs == null) {
        throw new PluginInitializationException("WorldGuard not found");
    }
    getLogger().info("✓ Connected to WorldGuard");
    
    // Vault Economy
    if (!setupEconomy()) {
        throw new PluginInitializationException("Vault Economy not found");
    }
    getLogger().info("✓ Connected to Vault");
    
    // PlaceholderAPI
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
        throw new PluginInitializationException("PlaceholderAPI not found");
    }
    new PAPI(this).register();
    getLogger().info("✓ Connected to PlaceholderAPI");
}

/**
 * Sets up the database connection.
 * 
 * @throws PluginInitializationException if database setup fails
 */
private void setupDatabase() throws PluginInitializationException {
    try {
        DatabaseManager.initialize(this);
        getLogger().info("✓ Database connected");
    } catch (SQLException e) {
        throw new PluginInitializationException("Database connection failed: " + e.getMessage());
    }
}

/**
 * Registers all event listeners.
 */
private void setupEventListeners() {
    PluginManager pm = Bukkit.getPluginManager();
    
    // Core events
    pm.registerEvents(new OnJoin(this), this);
    pm.registerEvents(new InputsHandler(this), this);
    pm.registerEvents(new Fixes(this), this);
    
    // Custom job events
    pm.registerEvents(new Bamboo(), this);
    pm.registerEvents(new Kanalizacija(), this);
    pm.registerEvents(new Kasyklos(), this);
    
    getLogger().info("✓ Event listeners registered");
}

/**
 * Registers all commands.
 */
private void setupCommands() {
    // Use a command manager for better organization
    CommandManager cmdManager = new CommandManager(this);
    
    cmdManager.register("skelbti", new SkelbtiCommand(this));
    cmdManager.register("randomtp", new RandomPlayer(this));
    cmdManager.register("coinflip", new CoinFlip(this));
    
    getLogger().info("✓ Commands registered");
}

/**
 * Sets up scheduled tasks.
 */
private void setupScheduledTasks() {
    // SQL keepalive
    scheduleAsyncTask(this::keepDatabaseAlive, 0L, 1200L);
    
    // Player data saving
    scheduleSyncTask(this::saveModifiedPlayers, 1200L, 400L);
    
    // Mute sync
    scheduleAsyncTask(this::syncMutes, 1200L, 1200L);
    
    getLogger().info("✓ Scheduled tasks started");
}

/**
 * Shuts down the server with a message.
 */
private void shutdownServer(String reason) {
    getLogger().severe("Shutting down server: " + reason);
    Bukkit.getServer().setWhitelist(true);
    Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 20L);
}

/**
 * Custom exception for plugin initialization failures.
 */
private static class PluginInitializationException extends Exception {
    public PluginInitializationException(String message) {
        super(message);
    }
}


// ============================================================
// EXAMPLE 4: Null Safety with Optional
// ============================================================

// ❌ BEFORE:
public static Zaidejas getZaidejas(String nick) {
    if(Main.zaidejai == null) {
        OnJoin.getPlayer(Bukkit.getPlayer(nick).getUniqueId(), nick); // NPE risk!
    }
    if(Main.zaidejai.get(nick) == null) {
        if(Bukkit.getPlayer(nick) != null) {
            OnJoin.getPlayer(Bukkit.getPlayer(nick).getUniqueId(), nick);
        } else {
            return null;
        }
    }
    return Main.zaidejai.get(nick);
}

// Usage:
Zaidejas z = Main.getZaidejas("PlayerName");
z.addMoney(100); // Might crash with NPE!


// ✅ AFTER:
/**
 * Gets a Zaidejas (player) by name.
 * 
 * @param nick Player's nickname
 * @return Optional containing the Zaidejas if found
 */
public static Optional<Zaidejas> getZaidejas(String nick) {
    if (nick == null || nick.isEmpty()) {
        return Optional.empty();
    }
    
    // Initialize map if needed
    if (zaidejai == null) {
        zaidejai = new ConcurrentHashMap<>();
    }
    
    // Try to get from cache
    Zaidejas cached = zaidejai.get(nick);
    if (cached != null) {
        return Optional.of(cached);
    }
    
    // Try to load from online player
    Player player = Bukkit.getPlayerExact(nick);
    if (player != null) {
        Zaidejas loaded = OnJoin.getPlayer(player.getUniqueId(), nick);
        return Optional.ofNullable(loaded);
    }
    
    // Not found
    return Optional.empty();
}

// Usage (safe!):
Main.getZaidejas("PlayerName")
    .ifPresent(zaidejas -> zaidejas.addMoney(100));

// Or with fallback:
Zaidejas zaidejas = Main.getZaidejas("PlayerName")
    .orElseThrow(() -> new PlayerNotFoundException("Player not found"));


// ============================================================
// EXAMPLE 5: Enum Constants Instead of Magic Strings/Numbers
// ============================================================

// ❌ BEFORE (scattered throughout code):
if (player.hasPermission("funkcijos.admin")) { ... }
if (material == Material.COAL_ORE) { ... }
economy.depositPlayer(player, 100.0);


// ✅ AFTER:
public enum Permission {
    ADMIN("funkcijos.admin"),
    MODERATOR("funkcijos.moderator"),
    VIP("funkcijos.vip"),
    SKELBTI_USE("funkcijos.skelbti.use"),
    SKELBTI_REMOVE("funkcijos.skelbti.atimti");
    
    private final String node;
    
    Permission(String node) {
        this.node = node;
    }
    
    public String getNode() {
        return node;
    }
    
    public boolean has(Player player) {
        return player.hasPermission(node);
    }
}

public enum MineReward {
    COAL(Material.COAL_ORE, 10.0, "Mine A"),
    IRON(Material.IRON_ORE, 25.0, "Mine B"),
    GOLD(Material.GOLD_ORE, 50.0, "Mine C"),
    DIAMOND(Material.DIAMOND_ORE, 100.0, "Mine D");
    
    private final Material ore;
    private final double reward;
    private final String mineName;
    
    MineReward(Material ore, double reward, String mineName) {
        this.ore = ore;
        this.reward = reward;
        this.mineName = mineName;
    }
    
    public Material getOre() { return ore; }
    public double getReward() { return reward; }
    public String getMineName() { return mineName; }
    
    public static Optional<MineReward> fromMaterial(Material material) {
        for (MineReward reward : values()) {
            if (reward.ore == material) {
                return Optional.of(reward);
            }
        }
        return Optional.empty();
    }
}

// Usage:
if (Permission.ADMIN.has(player)) {
    // Do admin stuff
}

MineReward.fromMaterial(block.getType())
    .ifPresent(reward -> {
        economy.depositPlayer(player, reward.getReward());
        player.sendMessage("You earned $" + reward.getReward());
    });


// ============================================================
// EXAMPLE 6: Command Pattern for Better Command Organization
// ============================================================

// ❌ BEFORE:
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 1 && args[0].equalsIgnoreCase("atimti") && sender.hasPermission("funkcijos.skelbti.atimti")) {
        // 50 lines of code...
    }
    if (args.length > 1 && args[0].equalsIgnoreCase("grazinti") && sender.hasPermission("funkcijos.skelbti.grazinti")) {
        // 50 more lines...
    }
    // etc...
}


// ✅ AFTER:
public abstract class SubCommand {
    protected final Main plugin;
    
    public SubCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    public abstract String getName();
    public abstract String getPermission();
    public abstract String getUsage();
    public abstract boolean execute(CommandSender sender, String[] args);
}

public class SkelbtiRemoveCommand extends SubCommand {
    
    public SkelbtiRemoveCommand(Main plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "atimti";
    }
    
    @Override
    public String getPermission() {
        return "funkcijos.skelbti.atimti";
    }
    
    @Override
    public String getUsage() {
        return "/skelbti atimti <player> [reason]";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Util.prefix("Usage: " + getUsage()));
            return false;
        }
        
        OfflinePlayer target = Util.offlinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Util.prefix("Player not found!"));
            return false;
        }
        
        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : "No reason provided";
        
        // Business logic
        revokeSkelbtiPermission(target, reason);
        
        sender.sendMessage(Util.prefix("§2Successfully revoked skelbti permission."));
        return true;
    }
    
    private void revokeSkelbtiPermission(OfflinePlayer player, String reason) {
        plugin.getConfig().set("Skelbti.blacklist." + player.getName(), reason);
        plugin.saveConfig();
    }
}

public class SkelbtiCommand implements CommandExecutor {
    
    private final Map<String, SubCommand> subCommands;
    
    public SkelbtiCommand(Main plugin) {
        this.subCommands = new HashMap<>();
        registerSubCommand(new SkelbtiRemoveCommand(plugin));
        registerSubCommand(new SkelbtiRestoreCommand(plugin));
        registerSubCommand(new SkelbtiListCommand(plugin));
    }
    
    private void registerSubCommand(SubCommand cmd) {
        subCommands.put(cmd.getName().toLowerCase(), cmd);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        SubCommand subCmd = subCommands.get(args[0].toLowerCase());
        if (subCmd == null) {
            sender.sendMessage(Util.prefix("Unknown subcommand!"));
            showHelp(sender);
            return false;
        }
        
        if (!sender.hasPermission(subCmd.getPermission())) {
            sender.sendMessage(Util.prefix("§cNo permission!"));
            return false;
        }
        
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCmd.execute(sender, subArgs);
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§7§m          §r §e§lSkelbti Commands §7§m          ");
        for (SubCommand cmd : subCommands.values()) {
            if (sender.hasPermission(cmd.getPermission())) {
                sender.sendMessage("§e" + cmd.getUsage());
            }
        }
    }
}


// ============================================================
// KEY TAKEAWAYS
// ============================================================

/*
1. ✅ Use try-with-resources for all database operations
2. ✅ Use PreparedStatements to prevent SQL injection
3. ✅ Use Optional<T> instead of returning null
4. ✅ Extract methods - keep them under 30 lines
5. ✅ Use enums for constants and type safety
6. ✅ Follow naming conventions (PascalCase for classes)
7. ✅ Use dependency injection instead of static abuse
8. ✅ Add proper JavaDoc documentation
9. ✅ Use connection pooling (HikariCP)
10. ✅ Separate concerns (Repository pattern, Service layer)
*/

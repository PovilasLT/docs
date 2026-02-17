# Java Code Quality Review: funkcijos Plugin

## 📋 Overview
This is a comprehensive Minecraft server plugin (~16,600 lines) with multiple features including mines, cells, guards, custom jobs, and more.

---

## 🚨 Critical Issues (Fix First)

### 1. **SQL Injection Vulnerability**
**Location**: Throughout the codebase (SQL class doesn't use PreparedStatements properly)

**Problem**: Direct string concatenation in SQL queries opens you to SQL injection attacks.

**Current Pattern** (Found in many places):
```java
String query = "SELECT * FROM players WHERE name = '" + playerName + "'";
```

**Fix**: Always use PreparedStatements:
```java
String query = "SELECT * FROM players WHERE name = ?";
PreparedStatement ps = connection.prepareStatement(query);
ps.setString(1, playerName);
ResultSet rs = ps.executeQuery();
```

---

### 2. **Resource Leaks**
**Location**: SQL.java and database operations throughout

**Problem**: Connections, PreparedStatements, and ResultSets are not properly closed, causing memory leaks.

**Fix**: Use try-with-resources:
```java
// Bad (your current code):
Connection con = SQL.getConnection();
PreparedStatement ps = con.prepareStatement(query);
ResultSet rs = ps.executeQuery();

// Good:
try (Connection con = SQL.getConnection();
     PreparedStatement ps = con.prepareStatement(query);
     ResultSet rs = ps.executeQuery()) {
    // Use the resources
} catch (SQLException e) {
    // Handle exception
}
```

---

### 3. **Null Pointer Exceptions**
**Location**: Main.java line 410, and many other places

**Problem**:
```java
public static Zaidejas getZaidejas(String nick) {
    if(Main.zaidejai == null) OnJoin.getPlayer(Bukkit.getPlayer(nick).getUniqueId(), nick);
    // Bukkit.getPlayer(nick) can return null!
}
```

**Fix**:
```java
public static Zaidejas getZaidejas(String nick) {
    if (Main.zaidejai == null) {
        Player player = Bukkit.getPlayer(nick);
        if (player == null) {
            return null;
        }
        OnJoin.getPlayer(player.getUniqueId(), nick);
    }
    // ... rest of code
}
```

---

## ⚠️ High Priority Issues

### 4. **Naming Conventions Violations**
**Location**: Multiple files

**Problems**:
- ❌ `skelbti.java` - class names should be PascalCase
- ❌ `chestblock.java` - same issue
- ❌ `tazeris.java` - same issue
- ❌ `stars.java` - same issue
- ❌ `sapling.java` - same issue

**Fix**: Rename classes:
```java
// Bad:
public class skelbti implements CommandExecutor

// Good:
public class SkelbtiCommand implements CommandExecutor
```

---

### 5. **Static Abuse**
**Location**: Main.java

**Problem**: Too many static fields make testing difficult and create tight coupling.

```java
public static Economy econ = null;
public static LuckPerms lp;
public static Team team;
// ... 10+ more static fields
```

**Fix**: Use dependency injection or singleton pattern properly:
```java
public class Main extends JavaPlugin {
    private static Main instance;
    private Economy economy;
    private LuckPerms luckPerms;
    
    @Override
    public void onEnable() {
        instance = this;
        // Initialize fields
    }
    
    public static Main getInstance() {
        return instance;
    }
    
    public Economy getEconomy() {
        return economy;
    }
}

// Usage in other classes:
Main.getInstance().getEconomy().depositPlayer(player, amount);
```

---

### 6. **Error Handling**
**Location**: Throughout codebase

**Problems**:
1. Empty catch blocks
2. Just printing stack traces
3. Not logging context

**Example from SQL.java**:
```java
// Bad:
} catch (SQLException e) {
    e.printStackTrace();
}

// Good:
} catch (SQLException e) {
    plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
    plugin.getLogger().severe("Host: " + config.getString("SQL.hostname"));
    // Consider reconnection logic or fallback
}
```

---

## 📝 Medium Priority Issues

### 7. **Magic Numbers**
**Location**: Main.java lines 284, 299, 300, 310, etc.

**Problem**:
```java
(new JailTask()).runTaskTimer(this, 0L, 50L);
Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getMainPlugin(), () -> {
    // ...
}, 0L, 1200L);
```

**Fix**: Use constants:
```java
// At the top of your class:
private static final long SQL_KEEPALIVE_INTERVAL = 1200L; // 60 seconds
private static final long PLAYER_SAVE_INTERVAL = 400L;    // 20 seconds
private static final long JAIL_TASK_INTERVAL = 50L;       // 2.5 seconds

// Usage:
(new JailTask()).runTaskTimer(this, 0L, JAIL_TASK_INTERVAL);
```

---

### 8. **Inefficient Boolean Checks**
**Location**: SQL.java line 39, Util.java and others

**Problem**:
```java
public static boolean isConnected() {
    return (con == null ? false : true);
}
```

**Fix**:
```java
public static boolean isConnected() {
    return con != null;
}
```

---

### 9. **Overly Long Methods**
**Location**: Main.java `onEnable()` method (240+ lines)

**Problem**: The onEnable method does too much, making it hard to read and maintain.

**Fix**: Extract methods:
```java
@Override
public void onEnable() {
    plugin = this;
    
    if (!initializeDependencies()) {
        shutdownServer("Failed to initialize required dependencies");
        return;
    }
    
    loadConfigurations();
    registerEvents();
    registerCommands();
    setupScheduledTasks();
    initializeRecipes();
    
    Util.logger("Plugin successfully enabled.");
}

private boolean initializeDependencies() {
    if (!connectToDatabase()) return false;
    if (!setupLuckPerms()) return false;
    if (!setupWorldGuard()) return false;
    if (!setupEconomy()) return false;
    return true;
}

private void registerEvents() {
    PluginManager pm = Bukkit.getPluginManager();
    pm.registerEvents(new OnJoin(this), this);
    pm.registerEvents(new InputsHandler(this), this);
    // ... rest
}
```

---

### 10. **Anonymous Inner Classes in Loops**
**Location**: Main.java line 362-367

**Problem**:
```java
Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
    @Override
    public void run() {
        Kanalizacija.iterate();
    }
}, 200L, 200L);
```

**Fix**: Use lambda expressions (Java 8+):
```java
Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, 
    Kanalizacija::iterate, 
    200L, 200L);
```

---

## 💡 Code Style Improvements

### 11. **Inconsistent Formatting**
**Location**: Throughout

**Problems**:
- Inconsistent spacing
- Mixed use of `this.` prefix
- Inconsistent bracing style

**Fix**: Use a consistent style guide (Google Java Style or Oracle's conventions)

**Example**:
```java
// Bad:
public  void    method(  ){
if(condition){
doSomething();
}
}

// Good:
public void method() {
    if (condition) {
        doSomething();
    }
}
```

---

### 12. **Comments and Documentation**
**Location**: Most classes lack JavaDoc

**Add JavaDoc**:
```java
/**
 * Manages player cells in the prison system.
 * 
 * @author YourName
 * @version 2.0
 */
public class CellsManager {
    
    /**
     * Assigns a cell to a player.
     * 
     * @param player The player to assign the cell to
     * @param cellId The ID of the cell
     * @return true if assignment was successful, false otherwise
     * @throws IllegalArgumentException if player is null
     */
    public boolean assignCell(Player player, int cellId) {
        // Implementation
    }
}
```

---

### 13. **Package Naming**
**Location**: All packages

**Current**: `lt.povilasc.funkcijos`

**Better**:
- Use meaningful names: `lt.povilasc.prisonplugin` or `lt.povilasc.mcprison`
- Avoid non-English package names if possible for international collaboration

---

### 14. **Configuration Management**
**Location**: Multiple config classes

**Problem**: Each config creates its own file access pattern.

**Fix**: Create a unified config manager:
```java
public class ConfigManager {
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    
    public void loadConfig(String name) {
        // Centralized config loading
    }
    
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }
}
```

---

## 🎯 Best Practices to Adopt

### 15. **Use Enums for Constants**
**Example**: Instead of scattered string/int constants:

```java
public enum MineType {
    A("Mine A", Material.COAL_ORE, 1.0),
    B("Mine B", Material.IRON_ORE, 1.5),
    C("Mine C", Material.GOLD_ORE, 2.0);
    
    private final String displayName;
    private final Material ore;
    private final double multiplier;
    
    MineType(String displayName, Material ore, double multiplier) {
        this.displayName = displayName;
        this.ore = ore;
        this.multiplier = multiplier;
    }
    
    public String getDisplayName() { return displayName; }
    public Material getOre() { return ore; }
    public double getMultiplier() { return multiplier; }
}
```

---

### 16. **Implement Proper Logging**
**Replace**: `Util.logger()` with proper Java logging

```java
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    private static final Logger log = Logger.getLogger("MCPrison");
    
    public void onEnable() {
        log.info("Enabling MCPrison plugin...");
        log.warning("Configuration file missing, using defaults");
        log.severe("Failed to connect to database!");
    }
}
```

---

### 17. **Use Optional for Null Safety** (Java 8+)
```java
// Instead of:
public static Zaidejas getZaidejas(String nick) {
    // ... null checks everywhere
    return zaidejas; // might be null
}

// Use:
public static Optional<Zaidejas> getZaidejas(String nick) {
    if (nick == null || Main.zaidejai == null) {
        return Optional.empty();
    }
    return Optional.ofNullable(Main.zaidejai.get(nick));
}

// Usage:
Main.getZaidejas(playerName)
    .ifPresent(zaidejas -> {
        // Safe to use zaidejas here
    });
```

---

### 18. **Separate Business Logic from Framework Code**
**Problem**: Bukkit/Spigot code mixed with business logic

**Fix**: Create service classes:
```java
// Service layer (no Bukkit dependencies)
public class PlayerDataService {
    public PlayerData getPlayerData(String name) {
        // Pure business logic
    }
}

// Bukkit command executor (thin layer)
public class PlayerCommand implements CommandExecutor {
    private final PlayerDataService service;
    
    @Override
    public boolean onCommand(...) {
        PlayerData data = service.getPlayerData(sender.getName());
        // Convert to Bukkit messages
    }
}
```

---

## 📊 Code Metrics

| Metric | Current | Target | Priority |
|--------|---------|--------|----------|
| SQL Injection Risk | High | None | 🔴 Critical |
| Resource Leaks | Many | None | 🔴 Critical |
| NullPointerException Risk | High | Low | 🔴 Critical |
| Naming Convention Violations | ~10 classes | 0 | 🟡 High |
| Lines per Method (avg) | ~50 | <30 | 🟡 High |
| JavaDoc Coverage | <5% | >60% | 🟢 Medium |
| Magic Numbers | ~50+ | <10 | 🟢 Medium |

---

## 🎓 Learning Resources

1. **Effective Java** by Joshua Bloch - Essential Java best practices
2. **Clean Code** by Robert Martin - Code readability principles
3. **Spigot Plugin Development** - Official docs: https://www.spigotmc.org/wiki/
4. **OWASP Top 10** - Security vulnerabilities: https://owasp.org/www-project-top-ten/

---

## 🔄 Next Steps

1. **Week 1**: Fix critical SQL injection vulnerabilities
2. **Week 2**: Implement try-with-resources for all database operations
3. **Week 3**: Add null checks in high-risk methods
4. **Week 4**: Rename classes to follow naming conventions
5. **Ongoing**: Refactor one large method per week

---

## 💬 Questions?

Feel free to ask about:
- How to implement any of these suggestions
- Why a particular pattern is better
- How to refactor specific code sections
- Testing strategies for your plugin

Great job on building a large, feature-rich plugin! With these improvements, your code will be more maintainable, secure, and professional. 🚀

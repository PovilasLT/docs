# Code Quality Improvement Checklist

## 🔴 CRITICAL (Do Immediately)

### Security Issues
- [ ] **SQL Injection Prevention**
  - [ ] Review all database queries in the codebase
  - [ ] Replace string concatenation with PreparedStatements
  - [ ] Add parameterized queries for all user inputs
  - [ ] Test with SQL injection attempts
  - **Files to fix**: Everywhere SQL queries are used (search for `SQL.getConnection()`)
  - **Priority**: URGENT
  - **Time estimate**: 2-3 days

- [ ] **Resource Leak Prevention**
  - [ ] Add try-with-resources to all database operations
  - [ ] Close all Connections, PreparedStatements, ResultSets
  - [ ] Review file operations for proper closing
  - **Files to fix**: `SQL.java`, all files using database
  - **Priority**: URGENT
  - **Time estimate**: 1-2 days

- [ ] **Null Pointer Exception Fixes**
  - [ ] Fix `Main.getZaidejas()` method (line 410)
  - [ ] Add null checks before calling methods on objects
  - [ ] Use Optional<T> for methods that might return null
  - [ ] Review all `Bukkit.getPlayer()` calls
  - **Files to fix**: `Main.java`, `Util.java`, command classes
  - **Priority**: HIGH
  - **Time estimate**: 2 days

---

## 🟡 HIGH PRIORITY (Next 2 Weeks)

### Code Structure
- [ ] **Rename Classes to Follow Conventions**
  - [ ] `skelbti.java` → `SkelbtiCommand.java`
  - [ ] `chestblock.java` → `ChestBlockListener.java`
  - [ ] `tazeris.java` → `TaserItem.java`
  - [ ] `stars.java` → `StarsCosmetic.java`
  - [ ] `sapling.java` → `SaplingListener.java`
  - [ ] Update all imports after renaming
  - **Priority**: HIGH
  - **Time estimate**: 1 hour

- [ ] **Refactor onEnable Method**
  - [ ] Extract database setup to separate method
  - [ ] Extract event registration to separate method
  - [ ] Extract command registration to separate method
  - [ ] Extract scheduled task setup to separate method
  - [ ] Create `initializePlugin()` orchestrator method
  - **File**: `Main.java`
  - **Priority**: HIGH
  - **Time estimate**: 3-4 hours

- [ ] **Remove Static Abuse**
  - [ ] Convert static fields to instance variables
  - [ ] Implement proper singleton pattern for Main
  - [ ] Use dependency injection where possible
  - [ ] Create getter methods for shared resources
  - **Files**: `Main.java`, `Util.java`
  - **Priority**: HIGH
  - **Time estimate**: 1 day

### Database Improvements
- [ ] **Implement Connection Pooling**
  - [ ] Add HikariCP dependency to pom.xml/build.gradle
  - [ ] Create `DatabaseManager` class
  - [ ] Configure connection pool settings
  - [ ] Replace `SQL.java` with `DatabaseManager`
  - **Priority**: HIGH
  - **Time estimate**: 4 hours

- [ ] **Create Repository Pattern**
  - [ ] Create `PlayerRepository` class
  - [ ] Create `CellRepository` class
  - [ ] Create `MineRepository` class
  - [ ] Move all SQL queries to repositories
  - [ ] Use repositories in business logic
  - **Priority**: MEDIUM-HIGH
  - **Time estimate**: 1-2 days

---

## 🟢 MEDIUM PRIORITY (Next Month)

### Code Quality
- [ ] **Replace Magic Numbers with Constants**
  - [ ] Find all numeric literals in code
  - [ ] Create constants class or use enums
  - [ ] Replace numbers with named constants
  - **Example**: `1200L` → `SQL_KEEPALIVE_INTERVAL`
  - **Priority**: MEDIUM
  - **Time estimate**: 2-3 hours

- [ ] **Improve Error Handling**
  - [ ] Remove empty catch blocks
  - [ ] Replace `e.printStackTrace()` with proper logging
  - [ ] Add context to error messages
  - [ ] Create custom exception classes where needed
  - **Priority**: MEDIUM
  - **Time estimate**: 1 day

- [ ] **Add JavaDoc Documentation**
  - [ ] Document all public classes
  - [ ] Document all public methods
  - [ ] Add @param, @return, @throws tags
  - [ ] Document complex private methods
  - **Target**: 60%+ coverage
  - **Priority**: MEDIUM
  - **Time estimate**: Ongoing

- [ ] **Extract Utility Enums**
  - [ ] Create `Permission` enum for all permissions
  - [ ] Create `MineType` enum for mine configurations
  - [ ] Create `Message` enum for common messages
  - [ ] Replace string literals with enum references
  - **Priority**: MEDIUM
  - **Time estimate**: 4-6 hours

### Code Organization
- [ ] **Implement Command Pattern**
  - [ ] Create `SubCommand` abstract class
  - [ ] Break down large command classes
  - [ ] Create separate class for each subcommand
  - [ ] Implement `CommandManager` for registration
  - **Priority**: MEDIUM
  - **Time estimate**: 1-2 days

- [ ] **Simplify Boolean Logic**
  - [ ] Fix `return (con == null ? false : true);`
  - [ ] Simplify complex if conditions
  - [ ] Use early returns
  - [ ] Remove unnecessary boolean variables
  - **Priority**: LOW-MEDIUM
  - **Time estimate**: 1 hour

---

## 🔵 LOW PRIORITY (Future Improvements)

### Performance
- [ ] **Optimize Scheduled Tasks**
  - [ ] Review all Bukkit.getScheduler() calls
  - [ ] Combine similar tasks where possible
  - [ ] Use async for I/O operations
  - [ ] Profile performance bottlenecks
  - **Priority**: LOW
  - **Time estimate**: 4-6 hours

- [ ] **Cache Frequently Used Data**
  - [ ] Implement caching for player data
  - [ ] Cache configuration values
  - [ ] Add cache invalidation strategy
  - [ ] Monitor cache hit rates
  - **Priority**: LOW
  - **Time estimate**: 1 day

### Testing
- [ ] **Add Unit Tests**
  - [ ] Set up JUnit 5 or TestNG
  - [ ] Write tests for utility methods
  - [ ] Write tests for business logic
  - [ ] Mock Bukkit dependencies
  - **Target**: 40%+ code coverage
  - **Priority**: LOW
  - **Time estimate**: Ongoing

- [ ] **Integration Testing**
  - [ ] Test database operations
  - [ ] Test command execution
  - [ ] Test event handling
  - [ ] Create test server environment
  - **Priority**: LOW
  - **Time estimate**: 2-3 days

### Modernization
- [ ] **Use Java 8+ Features**
  - [ ] Replace anonymous classes with lambdas
  - [ ] Use Stream API for collections
  - [ ] Use method references where applicable
  - [ ] Use `forEach` with lambdas
  - **Priority**: LOW
  - **Time estimate**: Ongoing

- [ ] **Update Dependencies**
  - [ ] Review current dependency versions
  - [ ] Update to latest stable versions
  - [ ] Test for breaking changes
  - [ ] Update API usage if needed
  - **Priority**: LOW
  - **Time estimate**: 2-4 hours

---

## 📊 Progress Tracking

### Week 1 Goal
- [ ] Fix critical SQL injection vulnerabilities
- [ ] Implement try-with-resources for database
- [ ] Fix null pointer exceptions in Main.java

### Week 2 Goal
- [ ] Rename all classes to follow conventions
- [ ] Implement connection pooling with HikariCP
- [ ] Refactor Main.onEnable() method

### Month 1 Goal
- [ ] Complete all critical and high priority items
- [ ] Create at least 2 repository classes
- [ ] Add JavaDoc to core classes
- [ ] Replace magic numbers with constants

### Month 2 Goal
- [ ] Complete all medium priority items
- [ ] Implement command pattern for major commands
- [ ] Add basic unit tests
- [ ] Performance profiling and optimization

---

## 📝 Notes and Tips

### Before You Start
1. **Create a backup** of your entire project
2. **Use Git** for version control if you're not already
3. **Work in branches** - create a branch for each major change
4. **Test thoroughly** after each change
5. **Ask for help** if you're stuck on anything

### Testing Strategy
1. Test on a development server first
2. Keep a test player account
3. Test common user actions
4. Test edge cases (null values, invalid input)
5. Monitor server console for errors

### Learning Resources
- [ ] Read "Effective Java" chapters 1-4
- [ ] Review OWASP SQL Injection guide
- [ ] Study Java 8 Optional class documentation
- [ ] Watch tutorial on PreparedStatements
- [ ] Learn about design patterns (Repository, Command, Singleton)

### Getting Help
- Questions about specific refactoring? Ask me!
- Stuck on a pattern? I can provide more examples
- Need code review? Share what you've written
- Want pair programming? Let's work through it together

---

## ✅ Quick Wins (Do Today!)

These are small changes with big impact:

1. **Fix the boolean check** (2 minutes)
   ```java
   // In SQL.java line 39:
   // Change: return (con == null ? false : true);
   // To: return con != null;
   ```

2. **Add null check** (5 minutes)
   ```java
   // In Main.java line 410, add:
   Player player = Bukkit.getPlayer(nick);
   if (player == null) return null;
   ```

3. **Use lambda** (5 minutes)
   ```java
   // In Main.java line 362, change:
   Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
       @Override
       public void run() {
           Kanalizacija.iterate();
       }
   }, 200L, 200L);
   
   // To:
   Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, 
       Kanalizacija::iterate, 200L, 200L);
   ```

4. **Add constant** (3 minutes)
   ```java
   // At top of Main.java:
   private static final long SQL_KEEPALIVE_INTERVAL = 1200L;
   
   // Then use it in line 299:
   }, 0L, SQL_KEEPALIVE_INTERVAL);
   ```

---

## 🎯 Your Action Plan

### This Week
**Day 1-2**: SQL Injection fixes
**Day 3-4**: Resource leak fixes
**Day 5**: Null check fixes
**Weekend**: Testing and validation

### Next Week
**Day 1**: Rename classes
**Day 2-3**: Setup HikariCP
**Day 4-5**: Refactor onEnable
**Weekend**: Code review and cleanup

### This Month
**Week 3**: Create repositories
**Week 4**: Add documentation and constants

---

Remember: **Don't try to fix everything at once!** Work through the list systematically, test thoroughly, and ask questions when you need help. Good luck! 🚀

package com.goldtrader.service;

import com.goldtrader.dao.UserDao;
import com.goldtrader.model.User;
import com.goldtrader.util.PasswordUtil;
import com.goldtrader.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao = new UserDao();

    public AuthService() {
        ensureAdminExists();
    }

    public boolean login(String username, String password) {
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isActive() && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                SessionManager.getInstance().setCurrentUser(user);
                userDao.updateLastLogin(username);
                logger.info("User logged in: {}", username);
                return true;
            }
        }
        logger.warn("Failed login attempt for username: {}", username);
        return false;
    }

    public void logout() {
        String username = SessionManager.getInstance().getCurrentUsername();
        SessionManager.getInstance().logout();
        logger.info("User logged out: {}", username);
    }

    public User createUser(String username, String password, String fullName, String role) {
        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, fullName, role);
        return userDao.save(user);
    }

    private void ensureAdminExists() {
        if (!userDao.existsAny()) {
            String defaultPassword = System.getenv().getOrDefault("GOLD_TRADER_ADMIN_PASSWORD", "changeme");
            String hash = PasswordUtil.hashPassword(defaultPassword);
            User admin = new User("admin", hash, "System Administrator", "ADMIN");
            userDao.save(admin);
            logger.info("Default admin user created. Set GOLD_TRADER_ADMIN_PASSWORD env var to customize.");
        }
    }
}

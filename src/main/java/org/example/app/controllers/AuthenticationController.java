package org.example.app.controllers;

import org.example.app.handlers.CachedHandler;
import org.example.app.loggers.MyFormatter;
import org.example.app.models.User;
import org.example.app.repositories.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class AuthenticationController {

    public final static Logger logger = Logger.getLogger(AuthenticationController.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new MyFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    public static final int MAX_AGE = 30 * 60;
    public static final String JSESSION = "JSESSION";

    @Autowired
    @Qualifier("userRepository")
    private Repository<User> userRepository;

    @Autowired
    @Qualifier("memCachedHandler")
    private CachedHandler memCachedHandler;

    @PostMapping(value = "/registration", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public Cookie register(User user, HttpServletResponse response) {
        checkUser(user);

        logger.info("Response is received for registration user");

        if (userRepository.contains(user)) {
            logger.warning("User with same date is contained in database!");
            logger.log(Level.WARNING, "Set response status - ", HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        }

        if (!userRepository.insert(user)) {
            logger.severe("User isn't inserted in database!");
            logger.log(Level.SEVERE, "Response code - ", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
        logger.info("User is inserted in database");

        UUID uuid = getUUID(user.getEmail());

        if (!memCachedHandler.add(uuid.toString(), user.getEmail(), MAX_AGE)) {
            logger.severe("Cookie value isn't added to memCached!");
            logger.log(Level.SEVERE, "Response code - ", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
        logger.info("Cookie value is added to memCached");

        Cookie cookie = getCookie(uuid);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return cookie;
    }

    private void checkUser(User user) {
        logger.info("check user data");
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        logger.log(Level.INFO, "user email - %s", email);
        logger.log(Level.INFO, "user name - %s", name);
        logger.log(Level.INFO, "user password - %s", password);

        User checkUser = new User();
        checkUser.setEmail(email);
        checkUser.setPassword(password);
        checkUser.setName(name);
    }

    @GetMapping("/sign")
    public Cookie signIn(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        logger.info("Response is received for sign in user");

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        if (!userRepository.isAuthorized(user)) {
            logger.warning("User isn't contains in database!");
            logger.log(Level.WARNING, "Response code - ", HttpServletResponse.SC_FORBIDDEN);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        UUID uuid = getUUID(email);
        String key = uuid.toString();

        if (memCachedHandler.contains(email)) {
            memCachedHandler.set(key, email, MAX_AGE);
            logger.info("Cookie value is reset in memCached");
        } else {
            memCachedHandler.add(key, email, MAX_AGE);
            logger.info("Cookie value is added to memCached");
        }

        return getCookie(uuid);
    }

    @GetMapping("authorize")
    public void authorize(@CookieValue(value = JSESSION, defaultValue = "") String cookie, HttpServletResponse response) {
        logger.info("Response received for checking authentication");

        if (!cookie.equals("") && memCachedHandler.contains(cookie)) {
            logger.info("User with get cookie is authorized");
            memCachedHandler.set(cookie, memCachedHandler.get(cookie), MAX_AGE);
            logger.info("Cookie is reset");
            response.setStatus(HttpServletResponse.SC_OK);
            logger.log(Level.INFO, "Response code - ", HttpServletResponse.SC_OK);
        } else {
            logger.warning("User with get cookie isn't authorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.log(Level.WARNING, "Response code - ", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @GetMapping("getEmail")
    public String getEmail(@CookieValue(value = JSESSION, defaultValue = "") String cookie, HttpServletResponse response) {
        logger.info("Response received for getting email");

        if (!cookie.equals("") && memCachedHandler.contains(cookie)) {
            logger.info("User with get cookie is authorized");
            String email =  (String) memCachedHandler.get(cookie);
            memCachedHandler.set(cookie, email, MAX_AGE);
            logger.info("Cookie is reset");
            response.setStatus(HttpServletResponse.SC_OK);
            logger.log(Level.INFO, "Response code - ", HttpServletResponse.SC_OK);
            return email;
        } else {
            logger.warning("User with get cookie isn't authorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.log(Level.WARNING, "Response code - ", HttpServletResponse.SC_UNAUTHORIZED);
        }

        return null;
    }

    @GetMapping("exit")
    public void exit(@CookieValue(value = JSESSION, defaultValue = "") String cookie, HttpServletResponse response) {
        logger.info("Response received for exiting from system");

        if (cookie.equals("") || !memCachedHandler.contains(cookie)) {
            logger.warning("User with same cookie isn't authorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.log(Level.WARNING, "Response code - ", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (memCachedHandler.delete(cookie)) {
            logger.info("Cookie value is removed from memCached");
            logger.log(Level.INFO, "Response code - ", HttpServletResponse.SC_OK);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            logger.severe("Cookie value isn't removed from memCached!");
            logger.log(Level.SEVERE, "Response code - ", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private UUID getUUID(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes());
    }

    private Cookie getCookie(UUID uuid) {
        Cookie cookie = new Cookie(JSESSION, uuid.toString());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath("/");
        cookie.setDomain("localhost");

        return cookie;
    }
}

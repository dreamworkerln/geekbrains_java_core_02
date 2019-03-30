package ru.home.geekbrains.java.core_02.lesson06.server.utils;

import org.apache.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;


import java.lang.invoke.MethodHandles;

/**
 * Bcrypt wrapper
 * <br>
 * <a href="https://en.wikipedia.org/wiki/Bcrypt">https://en.wikipedia.org/wiki/Bcrypt</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BcryptAuth {
    //private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    //ToDo: increase cost to 13
    private static final int BCRYPT_COST = 13;

    /**
     * Hash password
     * @param password String password
     * @return String salt.hash
     */
    public static String hash(String password) {

        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Check hash
     * @param password string password
     * @param hash string hash
     * @return boolean
     */
    public static boolean check(String password, String hash) {

        return BCrypt.checkpw(password, hash);
    }
}

//     * @see <a href="https://en.wikipedia.org/wiki/Bcrypt">Bcrypt</a>



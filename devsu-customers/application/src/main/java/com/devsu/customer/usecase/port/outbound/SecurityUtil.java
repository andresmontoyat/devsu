package com.devsu.customer.usecase.port.outbound;

/**
 * Interface for utilizing security-related functionalities.
 */
public interface SecurityUtil {
    /**
     * Generates a BCrypt hash for the provided plain-text password.
     * @param password the plain-text password to hash.
     * @return the BCrypt hash string.<s
     */
    String hashPassword(String password);
}

package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

public final class PasswordHashingUtils {
  private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  // Make default constructor private to prevent initialization without the use of singleton getInstance()
  private PasswordHashingUtils() {}

  ///
  /// Returns the hashed version of a password.
  ///
  /// @param password a string representing the password.
  /// @return a non-null string representing the password hash.
  /// @spec.modifies nothing.
  ///
  public static String hashPassword(String password) {
    var result = passwordEncoder.encode(password);
    Assert.notNull(result, "Cannot be null as of the spec of the encode method");
    return result;
  }

  /// 
  /// Checks if a password matches a hash.
  /// 
  ///  @param password a password
  ///  @param hash     a hash
  ///  @return <code>true</code> iff the password matches the hash.
  /// 
  public static boolean passwordMatches(String password, String hash) {
    return passwordEncoder.matches(password, hash);
  }

}

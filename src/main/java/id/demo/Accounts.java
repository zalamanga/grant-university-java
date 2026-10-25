package id.demo;

import java.sql.*;
import java.util.*;

public final class Accounts {
  private Accounts() {}

  private static Account read(ResultSet rs) throws SQLException {
    return new Account(
        rs.getLong("id"),
        rs.getString("username"),
        rs.getString("name"),
        rs.getString("email"),
        "member",
        rs.getString("password_hash"));
  }

  public static Account find(long id) throws SQLException {
    try (Connection c = Db.open();
        PreparedStatement p = c.prepareStatement("SELECT * FROM accounts WHERE id=?")) {
      p.setLong(1, id);
      try (ResultSet rs = p.executeQuery()) {
        return rs.next() ? read(rs) : null;
      }
    }
  }

  public static Account login(String username, String password) throws SQLException {
    try (Connection c = Db.open();
        PreparedStatement p = c.prepareStatement("SELECT * FROM accounts WHERE username=?")) {
      p.setString(1, username);
      try (ResultSet rs = p.executeQuery()) {
        Account a = rs.next() ? read(rs) : null;
        return a != null && Passwords.verify(password, a.getHash()) ? a : null;
      }
    }
  }

  public static Map<String, String> validateProfile(Map<String, String> fields) {
    Map<String, String> result = new LinkedHashMap<>();
    result.put("first_name", Validation.text(fields.get("first_name"), "Nama depan", 80));
    result.put("last_name", Validation.text(fields.get("last_name"), "Nama belakang", 80));
    result.put("email", Validation.email(fields.get("email")));
    result.put("birth_date", Validation.birthDate(fields.get("birth_date")));
    String gender = fields.get("gender");
    if (!Set.of("Tidak dinyatakan", "Perempuan", "Laki-laki")
        .contains(gender == null ? "" : gender))
      throw new IllegalArgumentException("Pilih keterangan gender yang tersedia.");
    result.put("gender", gender);
    result.put("address", Validation.text(fields.get("address"), "Alamat contoh", 400));
    String phone = Validation.text(fields.get("phone"), "Nomor telepon contoh", 30);
    if (!phone.matches("[0-9+() -]{5,30}"))
      throw new IllegalArgumentException(
          "Nomor telepon harus 5 sampai 30 karakter angka, spasi, +, -, atau tanda kurung.");
    result.put("phone", phone);
    return result;
  }

  public static void register(String username, String password, Map<String, String> profile)
      throws SQLException {
    String hash = Passwords.hash(password);
    try (Connection c = Db.open()) {
      c.setAutoCommit(false);
      try {
        long id;
        try (PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO accounts(username,name,email,password_hash) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          p.setString(1, username);
          p.setString(2, profile.get("first_name") + " " + profile.get("last_name"));
          p.setString(3, profile.get("email"));
          p.setString(4, hash);
          p.executeUpdate();
          try (ResultSet rs = p.getGeneratedKeys()) {
            if (!rs.next()) throw new SQLException("Missing account id");
            id = rs.getLong(1);
          }
        }
        try (PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO profiles(first_name,last_name,birth_date,gender,address,phone,account_id) VALUES(?,?,?,?,?,?,?)")) {
          bindProfile(p, profile, id);
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  private static void bindProfile(PreparedStatement p, Map<String, String> profile, long userId)
      throws SQLException {
    p.setString(1, profile.get("first_name"));
    p.setString(2, profile.get("last_name"));
    p.setDate(3, java.sql.Date.valueOf(profile.get("birth_date")));
    p.setString(4, profile.get("gender"));
    p.setString(5, profile.get("address"));
    p.setString(6, profile.get("phone"));
    p.setLong(7, userId);
  }

  public static Map<String, Object> profile(long userId) throws SQLException {
    try (Connection c = Db.open();
        PreparedStatement p =
            c.prepareStatement(
                "SELECT p.*,a.username,a.email FROM profiles p JOIN accounts a ON a.id=p.account_id WHERE p.account_id=?")) {
      p.setLong(1, userId);
      try (ResultSet rs = p.executeQuery()) {
        if (!rs.next()) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData m = rs.getMetaData();
        for (int i = 1; i <= m.getColumnCount(); i++)
          result.put(m.getColumnLabel(i), rs.getObject(i));
        return result;
      }
    }
  }

  public static void updateProfile(long userId, Map<String, String> profile) throws SQLException {
    try (Connection c = Db.open()) {
      c.setAutoCommit(false);
      try {
        try (PreparedStatement p =
            c.prepareStatement("UPDATE accounts SET name=?,email=? WHERE id=?")) {
          p.setString(1, profile.get("first_name") + " " + profile.get("last_name"));
          p.setString(2, profile.get("email"));
          p.setLong(3, userId);
          if (p.executeUpdate() != 1) throw new IllegalArgumentException("Akun tidak ditemukan.");
        }
        try (PreparedStatement p =
            c.prepareStatement(
                "UPDATE profiles SET first_name=?,last_name=?,birth_date=?,gender=?,address=?,phone=? WHERE account_id=?")) {
          bindProfile(p, profile, userId);
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public static void changePassword(Account account, String current, String next)
      throws SQLException {
    if (!Passwords.verify(current, account.getHash()))
      throw new IllegalArgumentException("Kata sandi saat ini tidak sesuai.");
    String hash = Passwords.hash(next);
    try (Connection c = Db.open();
        PreparedStatement p =
            c.prepareStatement(
                "UPDATE accounts SET password_hash=? WHERE id=? AND password_hash=?")) {
      p.setString(1, hash);
      p.setLong(2, account.getId());
      p.setString(3, account.getHash());
      if (p.executeUpdate() != 1)
        throw new IllegalArgumentException("Akun berubah. Muat ulang halaman lalu coba lagi.");
    }
  }
}

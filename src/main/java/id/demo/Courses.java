package id.demo;

import java.sql.*;
import java.util.*;

public final class Courses {
  private Courses() {}

  private static Map<String, Object> row(ResultSet rs) throws SQLException {
    Map<String, Object> result = new LinkedHashMap<>();
    ResultSetMetaData meta = rs.getMetaData();
    for (int i = 1; i <= meta.getColumnCount(); i++)
      result.put(meta.getColumnLabel(i), rs.getObject(i));
    return result;
  }

  public static List<Map<String, Object>> all() throws SQLException {
    try (Connection c = Db.open();
        PreparedStatement p = c.prepareStatement("SELECT * FROM courses ORDER BY id");
        ResultSet rs = p.executeQuery()) {
      List<Map<String, Object>> result = new ArrayList<>();
      while (rs.next()) result.add(row(rs));
      return result;
    }
  }

  public static Map<String, Object> find(String id) throws SQLException {
    if (id == null || !id.matches("A-[0-9]{3}"))
      throw new IllegalArgumentException("Kode program contoh tidak valid.");
    try (Connection c = Db.open();
        PreparedStatement p = c.prepareStatement("SELECT * FROM courses WHERE id=?")) {
      p.setString(1, id);
      try (ResultSet rs = p.executeQuery()) {
        return rs.next() ? row(rs) : null;
      }
    }
  }
}

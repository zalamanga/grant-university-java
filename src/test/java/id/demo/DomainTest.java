package id.demo;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;

class DomainTest {
  @Test
  void saltedPasswordHashesRejectWrongAndMalformedValues() {
    String first = Passwords.hash("Portal-demo-password-123");
    String second = Passwords.hash("Portal-demo-password-123");
    assertNotEquals(first, second);
    assertTrue(Passwords.verify("Portal-demo-password-123", first));
    assertFalse(Passwords.verify("wrong password", first));
    assertFalse(Passwords.verify(null, first));
    for (String value :
        List.of(
            "plain",
            "pbkdf2$1$AA==$AA==",
            "pbkdf2$600000$invalid$invalid",
            first.replace("600000", "999999999")))
      assertFalse(Passwords.verify("Portal-demo-password-123", value));
    assertThrows(IllegalArgumentException.class, () -> Passwords.hash("short"));
  }

  private Map<String, String> validProfile() {
    Map<String, String> p = new LinkedHashMap<>();
    p.put("first_name", "Mahasiswa");
    p.put("last_name", "Contoh");
    p.put("email", "DEMO@example.test");
    p.put("birth_date", "2000-02-29");
    p.put("gender", "Tidak dinyatakan");
    p.put("address", "Alamat contoh\r\nBaris dua");
    p.put("phone", "+62 000 0000");
    return p;
  }

  @Test
  void profileValidationNormalizesInputsAndLeavesOriginalMapUnchanged() {
    Map<String, String> input = validProfile();
    Map<String, String> output = Accounts.validateProfile(input);
    assertEquals("demo@example.test", output.get("email"));
    assertEquals("Alamat contoh\nBaris dua", output.get("address"));
    assertEquals("DEMO@example.test", input.get("email"));
    assertEquals("2000-02-29", output.get("birth_date"));
  }

  @Test
  void profilesRejectFutureOrImpossibleDatesAndUnsupportedFields() {
    for (String date :
        List.of(
            LocalDate.now().plusDays(1).toString(), "1900-02-29", "1899-12-31", "2000-13-01", "")) {
      Map<String, String> p = validProfile();
      p.put("birth_date", date);
      assertThrows(IllegalArgumentException.class, () -> Accounts.validateProfile(p));
    }
    for (String field : List.of("first_name", "last_name", "email", "phone", "address")) {
      Map<String, String> p = validProfile();
      p.put(field, "");
      assertThrows(IllegalArgumentException.class, () -> Accounts.validateProfile(p));
    }
    Map<String, String> p = validProfile();
    p.put("gender", "admin");
    assertThrows(IllegalArgumentException.class, () -> Accounts.validateProfile(p));
    p.put("gender", "Tidak dinyatakan");
    p.put("phone", "<script>bad</script>");
    assertThrows(IllegalArgumentException.class, () -> Accounts.validateProfile(p));
  }

  @Test
  void usernameEmailAndPasswordLengthBoundariesAreEnforced() {
    assertEquals("demo_user", Validation.username("Demo_User"));
    assertThrows(IllegalArgumentException.class, () -> Validation.username("demo' OR 1=1"));
    assertThrows(IllegalArgumentException.class, () -> Validation.email("a@b"));
    assertThrows(IllegalArgumentException.class, () -> Validation.password("a".repeat(129)));
    assertEquals("a".repeat(12), Validation.password("a".repeat(12)));
  }

  @Test
  void csrfCannotMatchPartialNullOrDifferentTokens() {
    String token = "a".repeat(43);
    assertTrue(SecurityFilter.matches(token, token));
    assertFalse(SecurityFilter.matches(token, "b" + token.substring(1)));
    assertFalse(SecurityFilter.matches(null, token));
    assertFalse(SecurityFilter.matches(token, null));
    assertFalse(SecurityFilter.matches(token, "a"));
  }
}

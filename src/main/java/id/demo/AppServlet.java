package id.demo;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/")
public final class AppServlet extends HttpServlet {
  private static final Set<String> PRIVATE = Set.of("/profile", "/profile/edit", "/password");

  private static Account account(HttpServletRequest r) {
    return (Account) r.getAttribute("account");
  }

  private static void redirect(HttpServletRequest r, HttpServletResponse s, String path)
      throws IOException {
    s.sendRedirect(r.getContextPath() + path);
  }

  private boolean guard(HttpServletRequest r, HttpServletResponse s) throws IOException {
    if (PRIVATE.contains(r.getServletPath()) && account(r) == null) {
      redirect(r, s, "/login");
      return false;
    }
    return true;
  }

  @Override
  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws ServletException, IOException {
    if (r.getServletPath().startsWith("/assets/")) {
      r.getServletContext().getNamedDispatcher("default").forward(r, s);
      return;
    }
    if ("/health".equals(r.getServletPath())) {
      try (Connection c = Db.open();
          PreparedStatement p = c.prepareStatement("SELECT 1 FROM courses LIMIT 1")) {
        p.executeQuery().close();
        s.setContentType("text/plain");
        s.getWriter().print("ready");
      } catch (SQLException e) {
        s.setStatus(503);
        s.getWriter().print("unavailable");
      }
      return;
    }
    if (!guard(r, s)) return;
    try {
      render(r, s, r.getServletPath());
    } catch (SQLException e) {
      databaseError(r, s, e);
    } catch (IllegalArgumentException e) {
      s.setStatus(400);
      r.setAttribute("error", e.getMessage());
      view(r, s, "error");
    }
  }

  private static Map<String, String> fields(HttpServletRequest r) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (String key :
        List.of("first_name", "last_name", "email", "birth_date", "gender", "address", "phone"))
      fields.put(key, r.getParameter(key));
    return fields;
  }

  @Override
  protected void doPost(HttpServletRequest r, HttpServletResponse s)
      throws ServletException, IOException {
    if (!guard(r, s)) return;
    String path = r.getServletPath();
    try {
      String next;
      switch (path) {
        case "/register":
          String password = Validation.password(r.getParameter("password"));
          if (!password.equals(r.getParameter("confirm")))
            throw new IllegalArgumentException("Konfirmasi kata sandi tidak sama.");
          Accounts.register(
              Validation.username(r.getParameter("username")),
              password,
              Accounts.validateProfile(fields(r)));
          next = "/login?registered=1";
          break;
        case "/login":
          Account found =
              Accounts.login(
                  Validation.username(r.getParameter("username")), r.getParameter("password"));
          if (found == null)
            throw new IllegalArgumentException("Username atau kata sandi tidak sesuai.");
          r.getSession().invalidate();
          HttpSession session = r.getSession(true);
          session.setAttribute("accountId", found.getId());
          SecurityFilter.token(session);
          next = "/profile";
          break;
        case "/logout":
          r.getSession().invalidate();
          next = "/";
          break;
        case "/profile/edit":
          Accounts.updateProfile(account(r).getId(), Accounts.validateProfile(fields(r)));
          next = "/profile?saved=1";
          break;
        case "/password":
          Accounts.changePassword(
              account(r), r.getParameter("currentPassword"), r.getParameter("password"));
          next = "/profile?saved=1";
          break;
        default:
          s.sendError(404);
          return;
      }
      redirect(r, s, next);
    } catch (IllegalArgumentException e) {
      s.setStatus(400);
      r.setAttribute("error", e.getMessage());
      renderError(r, s, path);
    } catch (SQLException e) {
      if ("23000".equals(e.getSQLState())) {
        s.setStatus(409);
        r.setAttribute("error", "Username atau email sudah digunakan. Pilih nilai lain.");
        renderError(r, s, path);
      } else databaseError(r, s, e);
    }
  }

  private void renderError(HttpServletRequest r, HttpServletResponse s, String path)
      throws ServletException, IOException {
    try {
      render(r, s, "/password".equals(path) ? "/profile" : path);
    } catch (SQLException e) {
      databaseError(r, s, e);
    }
  }

  private void render(HttpServletRequest r, HttpServletResponse s, String path)
      throws SQLException, ServletException, IOException {
    String page;
    switch (path) {
      case "/":
        page = "home";
        break;
      case "/about":
        page = "about";
        break;
      case "/admissions":
        page = "admissions";
        break;
      case "/contacts":
        page = "contacts";
        break;
      case "/help":
        page = "help";
        break;
      case "/login":
        page = "login";
        break;
      case "/register":
        page = "register";
        break;
      case "/courses":
        r.setAttribute("courses", Courses.all());
        page = "courses";
        break;
      case "/course":
        Map<String, Object> course = Courses.find(r.getParameter("id"));
        if (course == null) {
          s.sendError(404);
          return;
        }
        r.setAttribute("course", course);
        page = "course";
        break;
      case "/profile":
      case "/profile/edit":
        Map<String, Object> profile = Accounts.profile(account(r).getId());
        if (profile == null) {
          s.sendError(404);
          return;
        }
        r.setAttribute("profile", profile);
        page = path.endsWith("/edit") ? "profile-edit" : "profile";
        break;
      default:
        s.sendError(404);
        return;
    }
    view(r, s, page);
  }

  private void view(HttpServletRequest r, HttpServletResponse s, String page)
      throws ServletException, IOException {
    r.setAttribute("page", page);
    r.getRequestDispatcher("/WEB-INF/views/app.jsp").forward(r, s);
  }

  private void databaseError(HttpServletRequest r, HttpServletResponse s, SQLException e)
      throws ServletException, IOException {
    getServletContext().log("Database request failed, SQL state=" + e.getSQLState());
    s.setStatus(503);
    r.setAttribute(
        "error",
        "Data belum dapat dimuat atau disimpan. Periksa layanan database, lalu coba lagi.");
    view(r, s, "error");
  }
}

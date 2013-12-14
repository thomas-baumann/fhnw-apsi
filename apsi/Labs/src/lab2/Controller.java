package lab2;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab2.database.CompanyDAO;
import lab2.helpers.MailHelper;
import lab2.helpers.Utility;
import lab2.model.Company;

public class Controller {

	private final static String MESSAGE_SESSION = "message";

	private final static String INDEX = "/index.jsp";
	private final static String LOGIN = "/login.jsp";
	private final static String MAIN = "/main.jsp";
	private final static String PASSWORDRESET = "/password_reset.jsp";
	private final static String REGISTER = "/register.jsp";

	private CompanyDAO companyDAO;

	public Controller() throws SQLException, ClassNotFoundException {
		this.companyDAO = new CompanyDAO();
	}

	public void indexGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			this.redirectToMain(request, response);
		} else {
			request.getRequestDispatcher(INDEX).forward(request, response);
		}
	}

	public void mainGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			String message = this.loadMessage(request);
			if (message != null) {
				List<String> success = new ArrayList<>();
				success.add(message);
				request.setAttribute("success", success);
			}
			request.getRequestDispatcher(MAIN).forward(request, response);
		} else {
			this.redirectToLogin(request, response);
		}
	}

	public void registerGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			this.redirectToMain(request, response);
		} else {
			request.getRequestDispatcher(REGISTER).forward(request, response);
		}
	}

	public void registerPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException, NoSuchAlgorithmException {
		if (this.isLoggedin(request)) {
			this.redirectToMain(request, response);
		} else {
			String name = request.getParameter("companyname");
			String address = request.getParameter("address");
			String zipcode = request.getParameter("plz");
			String town = request.getParameter("city");
			String email = request.getParameter("mail");
			Company c = new Company(email, name, address, zipcode, town);
			List<String> errors = c.validate();
			if (errors.size() == 0) {
				int counter = 1;
				while (this.companyDAO.getCompanyByUsername(name + counter) != null) {
					counter++;
				}
				c.setUsername(name + counter);
				String password = Utility.generateRandomString(12);
				c.setPassword(password);

				this.companyDAO.saveOrUpdateCompany(c);
				MailHelper.sendMail(c.getEmail(), c.getUsername(), c.getPasswordNotHashed());
				this.saveMessage(request,
						"Sie haben eine E-Mail mit Ihrem Benutzername und Passwort erhalten.\nBitte loggen Sie sich mit diesem nun ein.");
				this.redirectToLogin(request, response);
			} else {
				request.setAttribute("errors", errors);
				request.getRequestDispatcher(REGISTER).forward(request, response);
			}
		}
	}

	public void loginGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			this.redirectToMain(request, response);
		} else {
			String message = this.loadMessage(request);
			if (message != null) {
				List<String> errors = new ArrayList<>();
				errors.add(message);
				request.setAttribute("errors", errors);
			}
			request.getRequestDispatcher(LOGIN).forward(request, response);
		}
	}

	public void loginPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			this.redirectToMain(request, response);
		} else {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			Company cInput = new Company(username, password);
			Company c = this.companyDAO.getCompanyByUsername(username);
			List<String> errors = new ArrayList<>();
			if (cInput.validateUsernameAndPassword().size() == 0 && c != null && cInput.getPassword().equals(c.getPassword())) {
				c.setHashCode(Utility.generateRandomString(64));
				request.getSession().setAttribute("userId", c.getId());
				request.getSession().setAttribute("hashCode", c.getHashCode());
				this.companyDAO.saveOrUpdateCompany(c);
			} else {
				errors.add("Benutzername oder Passwort nicht korrekt.");
			}
			if (errors.size() == 0) {
				this.redirectToMain(request, response);
			} else {
				request.setAttribute("errors", errors);
				request.getRequestDispatcher(LOGIN).forward(request, response);
			}
		}
	}

	public void passwordResetGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			request.getRequestDispatcher(PASSWORDRESET).forward(request, response);
		} else {
			this.redirectToLogin(request, response);
		}
	}

	public void passwordResetPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException, NoSuchAlgorithmException {
		if (this.isLoggedin(request)) {
			List<String> errors = new ArrayList<String>();
			String oldPass = request.getParameter("oldpass");
			String newPass = request.getParameter("newpass");
			String newPassConf = request.getParameter("newpassconf");

			int id = (int) request.getSession().getAttribute("userId");
			Company c = this.companyDAO.getCompanyById(id);

			if (!c.getPassword().equals(Utility.hashString(oldPass))) {
				errors.add("Altes Passwort stimmt nicht.");
			}

			c.setPassword(newPass);
			errors.addAll(c.validateOnlyPassword());

			if (!newPass.equals(newPassConf)) {
				errors.add("Bestätigung stimmt nicht mit dem neuen Passwort überein.");
			}

			if (errors.size() == 0) {
				this.companyDAO.saveOrUpdateCompany(c);
				this.redirectToMain(request, response);
				this.saveMessage(request, "Änderung war erfolgreich.");
			} else {
				request.setAttribute("errors", errors);
				request.getRequestDispatcher(PASSWORDRESET).forward(request, response);
			}
		} else {
			this.redirectToLogin(request, response);
		}
	}

	public void logoutGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		if (this.isLoggedin(request)) {
			Company c = this.companyDAO.getCompanyById((int) request.getSession().getAttribute("userId"));
			c.setHashCode(null);
			this.companyDAO.saveOrUpdateCompany(c);
		}
		request.getSession().setAttribute("userId", null);
		request.getSession().setAttribute("hashCode", null);
		this.redirectToLogin(request, response);
	}

	private boolean isLoggedin(HttpServletRequest request) throws SQLException {
		if (request.getSession().getAttribute("userId") == null || request.getSession().getAttribute("hashCode") == null) {
			return false;
		}
		int userId = (int) request.getSession().getAttribute("userId");
		String hash = (String) request.getSession().getAttribute("hashCode");
		Company c = this.companyDAO.getCompanyById(userId);
		return c != null && hash.equals(c.getHashCode());
	}

	private void redirectToMain(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.redirect(request, response, "main");
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.redirect(request, response, "login");
	}

	private void redirect(HttpServletRequest request, HttpServletResponse response, String page) throws IOException {
		response.sendRedirect(request.getRequestURL().toString() + "?page=" + page);
	}

	private void saveMessage(HttpServletRequest request, String text) {
		request.getSession().setAttribute(MESSAGE_SESSION, text);
	}

	private String loadMessage(HttpServletRequest request) {
		String hash = (String) request.getSession().getAttribute(MESSAGE_SESSION);
		request.getSession().setAttribute(MESSAGE_SESSION, null);
		return hash;
	}
}

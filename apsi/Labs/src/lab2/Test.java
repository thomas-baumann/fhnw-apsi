package lab2;

import java.sql.SQLException;

import lab2.database.CompanyDAO;
import lab2.model.Company;

public class Test {

	public static void main(String[] args) throws SQLException {

		CompanyDAO dao = new CompanyDAO();

		Company c = new Company("tewgg st", "dfasd", "hui", "FHNW", "strasse 4", 2424, "Brugg");

		dao.saveOrUpdaetCompany(c);

		System.out.println(dao.getCompanyByUsername("test"));

	}

}
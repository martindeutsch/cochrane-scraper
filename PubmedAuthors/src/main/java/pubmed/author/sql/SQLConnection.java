package pubmed.author.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLConnection {

	private String url = null;
	private String user = null;
	private String password = null;
	private Connection con = null;

	public static SQLConnection local = new SQLConnection("localhost", "3306", "paul", "paul", "[paul3514]");

	public SQLConnection(String IP, String port, String dbname, String username, String password) {
		this.url = "jdbc:mysql://" + IP + ":" + port + "/" + dbname;
		this.user = username;
		this.password = password;
	}
	
	public synchronized ArrayList<String> getReferences(){
		ArrayList<String> list = new ArrayList<String>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("select reference_pubmed_id from refs;");
			while(rs.next())
				list.add(rs.getString(1));
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {

				if (rs != null) {
					rs.close();
				}

				if (st != null) {
					st.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}
	
	public synchronized String checkForAuthor(String authorName) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			// look for first author
			st = con.prepareStatement(
					"select first_gender, first_probability from refs_author where author_first = ?;");
			st.setString(1, authorName);
			rs = st.executeQuery();
			if (rs.next()) {
				return (rs.getString(1) + "#" + rs.getString(2));
			}

			st.close();
			rs.close();
			st = con.prepareStatement("select last_gender, last_probability from refs_author where author_last = ?;");
			st.setString(1, authorName);
			rs = st.executeQuery();
			if (rs.next()) {
				return (rs.getString(1) + "#" + rs.getString(2));
			}
			return null;
		} catch (SQLException ex) {
			System.err.println(ex.getLocalizedMessage() + "\n" + st.toString());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public synchronized boolean insertAuthors(String pubmedId, String first, String last, String gender1,
			String gender2, String probability1, String probability2) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(
					"REPLACE INTO paul.`refs_author` (pubmed_id, author_first, author_last, first_gender, last_gender, first_probability, last_probability) VALUES (?,?,?,?,?,?,?)");
			st.setString(1, pubmedId);
			st.setString(2, first);
			st.setString(3, last);
			st.setString(4, gender1);
			st.setString(5, gender2);
			st.setString(6, probability1);
			st.setString(7, probability2);
			st.executeUpdate();
			return true;
		} catch (SQLException ex) {
			System.err.println(ex.getLocalizedMessage() + "\n" + st.toString());
		} finally {
			try {
				
				if (st != null) {
					st.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public void getVersion() {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT VERSION()");
			if (rs.next())
				System.out.println(rs.getString(1));
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {

				if (rs != null) {
					rs.close();
				}

				if (st != null) {
					st.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized boolean open() {
		try {
			con = DriverManager.getConnection(url, user, password);
			return true;
		} catch (SQLException e) {
			System.err.println(url + ", " + user + ":" + password);
			e.printStackTrace();
			return false;
		}
	}

	public synchronized boolean close() {
		try {
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
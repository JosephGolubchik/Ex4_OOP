package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Database {

	public static ArrayList<String[]> queryData(String query) {
		String jdbcUrl="jdbc:mysql://ariel-oop.xyz:3306/oop"; //?useUnicode=yes&characterEncoding=UTF-8&useSSL=false";
		String jdbcUser="student";
		String jdbcPassword="student";

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = 
					DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);


			Statement statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(query);
			ArrayList<String[]> table = new ArrayList<String[]>();
			int numCols = resultSet.getMetaData().getColumnCount();
			while(resultSet.next()) {
				String[] row = new String[6];
				for (int i = 0; i < row.length; i++) {
					String colName = resultSet.getMetaData().getColumnName(i+1);
					if(colName.equals("FirstID")) {
						row[i] = ""+resultSet.getInt("FirstID");
					}
					else if(colName.equals("SecondID")) {
						row[i] = ""+resultSet.getInt("SecondID");
					}
					else if(colName.equals("ThirdID")) {
						row[i] = ""+resultSet.getInt("ThirdID");
					}
					else if(colName.equals("LogTime")) {
						row[i] = ""+resultSet.getTimestamp("LogTime");
					}
					else if(colName.equals("Point")) {
						row[i] = ""+resultSet.getDouble("Point");
					}
					else if(colName.equals("SomeDouble")) {
						row[i] = ""+resultSet.getDouble("SomeDouble");
					}
				}
				table.add(row);
			}

				
				
				resultSet.close();		
				statement.close();		
				connection.close();		
				
				return table;
			
		}

		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
}


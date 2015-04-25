import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class MySQLCheck extends Thread{
	private String server;
	private String user;
	private String pass;
	private String database;
	public MySQLCheck(String server,String user,String pass, String database) {
		this.server = server;
		this.user = user;
		this.pass = pass;
		this.database = database;
	}
	
	@Override
	public void run() {
		try {
			Connection conn;
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setUser(user);
			dataSource.setPassword(pass);
			dataSource.setServerName(server);
			dataSource.setDatabaseName(database);
			while(true) {
				try {
					//conn = dataSource.getConnection();
					conn = dataSource.getConnection();
					if(Main.mysqlStatus==false) {
						Main.conn = dataSource.getConnection();
						Main.conn2 = dataSource.getConnection();
						Main.stmt = Main.conn.createStatement();
						Main.stmt_ = Main.conn.createStatement();
						Main.stmt2 = Main.conn.createStatement();
						Main.mysqlLabel.setText("MySQL Online");
						Main.mysqlLabel.setForeground(Color.GREEN);
						Main.mysqlStatus = true;
					}
				}catch (SQLException e1) {
					Main.mysqlLabel.setText("MySQL Offline");
					Main.mysqlLabel.setForeground(Color.LIGHT_GRAY);
					Main.mysqlStatus = false;
				}
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

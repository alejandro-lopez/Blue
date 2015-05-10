import java.net.Socket;
import java.sql.SQLException;


public class pushNotification {

	public pushNotification(String token, String title, String message, String kidRegId, String kidName, String eType) throws SQLException, Exception {
		Engine.rs = Engine.stmt.executeQuery("SELECT regID FROM notifs WHERE parent = '"+Engine.mysql_real_escape_string(token)+"'");
		String regId = "";
		while(Engine.rs.next()) {
			regId = Engine.rs.getString("regID");
			if(regId != kidRegId) {
				/*Ya que las notificaciones push requieren de un API privado, en este proyecto Open Source no es viable, por lo que
				 * dependeremos del servidor remoto del proyecto. */
				new Socket("http://uabc.imeev.com/app/api/pushNotif?title="+title+"&message="+message+"&kidName="+kidName+"&eType="+eType+"&regId="+regId,80);
			}
		}
	}

}

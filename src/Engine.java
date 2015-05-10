import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.owlike.genson.Genson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;


public class Engine {
	public static MysqlDataSource dataSource;
	public static Statement stmt;
	public static Statement stmt2;
	public static Statement stmt3;
	public static ResultSet rs;
	public static ResultSet rs2;
	public static ResultSet chatRS;
	public static Connection conn;
	public static Connection conn2;
	public static StringEscapeUtils utils = new StringEscapeUtils();
	public static Genson genson = new Genson();
	public static String avatarUrl = "";
	public static String contactAvatar = "";
	public static String userAvatar = "";
	public static String url = "";
	
	public Engine(String section, HttpExchange t) throws IOException {
		startMysql();
		this.url = t.getRequestURI().toString();
		Map<String, Object> json = null;
		Map<String, Object> jsonMap = null;
		boolean isAsset = false;
		PasswordGenerator passGen = new PasswordGenerator();
		Avatar avatarProc = new Avatar();
		Photo photoProc = new Photo();
		Mail mailProc = new Mail();
		
		try{
			
			String response = "";
			int kid = 0;
			String kidID = "0";
			final ResultSet rs1;
			String name="";
			int cid=0;

			switch(section) {
				case "register":
					if(POST_("correo").length() == 0 || POST_("nombre").length() ==0 || POST_("pass").length() ==0 || POST_("tel").length() ==0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						rs= stmt.executeQuery("SELECT id FROM parents WHERE email = '"+mysql_real_escape_string(POST_("correo"))+"' LIMIT 1");
						if(rs.first() == false) {
							//$gen = new GeneratePassword();
							
							final String token = passGen.generate(5,5,5);
							final int userID = stmt.executeUpdate("INSERT INTO parents (nombre,email,pass,tel,token) VALUES ('"+mysql_real_escape_string(utils.escapeHtml(POST_("nombre")))+"','"+mysql_real_escape_string(POST_("correo"))+"','"+mysql_real_escape_string(POST_("pass"))+"','"+mysql_real_escape_string(POST_("tel"))+"','"+token+"')", stmt.RETURN_GENERATED_KEYS);
							final Map<String, Object>userData = new HashMap<String, Object>() {{
								put("id",userID);
								put("name",POST_("nombre"));
								put("email",POST_("correo"));
								put("tel",POST_("tel"));
								put("avatar", "");
								put("radius", 25);
								put("pin", 1234);
								put("skype", POST_("skype"));
								put("action", "call");
								put("token",token);
							}};
							json = new HashMap<String, Object>() {{
								put("status", "OK");
								put("user",userData);
							}};
						}else{
							json = new HashMap<String, Object>() {{
								put("status", "EXISTS");
							}};
						}
					}
					response = genson.serialize(json);
					break;
				case "login":
					if(POST_("correo").length() ==0 || POST_("pass").length()==0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						rs = stmt.executeQuery("SELECT id,nombre,email,token,dist_radius,avatar,exitPin,tel,skype,action FROM parents WHERE (email = '"+mysql_real_escape_string(POST_("correo"))+"' OR username = '"+mysql_real_escape_string(POST_("correo"))+"') AND (pass = '"+mysql_real_escape_string(POST_("pass"))+"' OR (MD5(passReset) = '"+mysql_real_escape_string(POST_("pass"))+"' AND passReset != '')) LIMIT 1");
						
						if(rs.next() == false) {
							json = new HashMap<String, Object>() {{
								put("status", "NO_EXISTS");
							}};
						}else{						
							if(rs.getInt("avatar") == 1)
								avatarUrl = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+rs.getInt("id")+".png";
							final Map<String, Object> auxData = new HashMap<String, Object>() {{
								put("id", rs.getInt("id"));
								put("name", rs.getString("nombre"));
								put("avatar", avatarUrl);
								put("email", rs.getString("email"));
								put("token", rs.getString("token"));
								put("radius", rs.getString("dist_radius"));
								put("pin", rs.getInt("exitPin"));
								put("tel", rs.getString("tel"));
								put("skype", rs.getString("skype"));
								put("action", rs.getString("action"));
							}};
							json = new HashMap<String, Object>() {{
								put("status", "OK");
								put("user", auxData);
							}};
							
						}
					}
					response = genson.serialize(json);
					break;
				case "getKidList":
					
					final List<Map> listaKids = new ArrayList<Map>();
					stmt3 = conn2.createStatement();
					String token = POST_("token");
					String persp = POST_("persp");
					rs = stmt.executeQuery("SELECT id,nombre,tel,avatar,skype FROM parents WHERE token = '"+mysql_real_escape_string(token) +"' LIMIT 1");
					rs.first();
					//list($uid,$pName,$pTel,$pAvatar) = mysql_fetch_array($q);
					int uid = rs.getInt("id");
					String pName = rs.getString("nombre");
					String pTel = rs.getString("tel");
					int pAvatar = rs.getInt("avatar");
					String pSkype = rs.getString("skype");
					
					rs = stmt.executeQuery("SELECT k.id,k.nombre,k.sexo,k.last_active,k.avatar,k.coords,k.default_coords,k.coord_address FROM kids AS k  WHERE k.parent =  '"+uid+"' ORDER BY k.nombre ASC");
					if(rs.first() == false) {
						json = new HashMap<String, Object>() {{
							put("status", "EMPTY");
						}};
					}else{
						kid = 0;
						name = "";
						long last = 0;
						int kAvatar = 0;
						String last_pos = "";
						String coord = "";
						String address = "";
						String isActive = "offline";
						String avatar;
						
						do {
							//list($kid,$name,$sex,$last,$kAvatar,$last_pos,$coord,$address) = $r;
							kid = rs.getInt("id");
							name = rs.getString("nombre");
							last = rs.getLong("last_active");
							kAvatar = rs.getInt("avatar");
							last_pos = rs.getString("coords");
							coord = rs.getString("default_coords");
							address = utils.escapeHtml(rs.getString("coord_address"));
							
							rs2 = stmt3.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
							
							final List<Map>contactos = new ArrayList<Map>();
							contactos.addAll(new VIPContacts(uid,pName,pTel,pAvatar,pSkype).prepend());
							
							while(rs2.next()) {
								
								//list($id,$cname,$num,$cAvatar,$skype) = $r_;
								contactAvatar = "";
								if(rs2.getInt("contact_Avatar") == 1)
									contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/contacts/"+rs2.getInt("id")+".png";
								Map<String, Object> lista = new HashMap<String, Object>(){{
									put("type", "contact");
									put("cid",rs2.getInt("id"));
									put("name",rs2.getString("contact_name"));
									put("tel",rs2.getString("contact_number"));
									put("avatar",contactAvatar);
									put("skype", rs2.getString("contact_skype"));
								}};
								contactos.add(lista);
							}
							
							if(time() - last <= 180)
								isActive = "online";
							else
								isActive = "offline";
							
							if(kAvatar == 1) {
								avatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kid+".png";	
							}else{
								avatar = "";
							}
							
							chatRS = stmt3.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic,c.seenByKid,c.seenByParent FROM (SELECT id,isKid,message,timeStamp,isPic,seenByKid,seenByParent FROM chat WHERE kid = '"+kid+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");
							String chat = "";
							if(chatRS.first()) {
								do{
									
									String cname="";
									String chatClass="";
									String cAvatar = "";
									int isKid = chatRS.getInt("isKid");
									int isPic = chatRS.getInt("isPic");
									cid = chatRS.getInt("id");
									System.out.println("Chat ID: "+cid);
									String msg = chatRS.getString("message");
									String timestamp = chatRS.getString("timeStamp");
									//list($cid,$isKid,$msg,$timestamp,$isPic) = $rChat;
									if(isKid == 0){
										cname = pName;
										chatClass= "parentChat";
										if(pAvatar == 1) 
											cAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
										else
											cAvatar = "images/parent.png";
									}else{
										cname = name;
										chatClass= "kidChat";
										if(kAvatar == 1) 
											cAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kid+".png";
										else
											cAvatar = "images/kid.png";
									}
									String pic = "";
									if(isPic == 1)
										pic = "isPic";
									chat+= "<div class=\"chatBody "+chatClass+" "+persp+"Persp "+pic+"\" id=\"chat-"+cid+"\"><div class=\"chatName\">"+cname+"</div><div class=\"chatAvatar\" style=\"background:url("+cAvatar+") center no-repeat;background-size:cover;\"></div><div class=\"chatTime\" old_time=\""+timestamp+"\"></div><div class=\"chatMessage\">"+msg+"</div></div>";
								}while(chatRS.next());
							}
							List<GeocoderResult> results = null;
							try {
								final Geocoder geocoder = new Geocoder();
								GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(last_pos).setLanguage("en").getGeocoderRequest();
								GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
								results = geocoderResponse.getResults();
							}catch(Exception e) {}
							final Map<String, Object> list = new HashMap<String, Object>();
							System.out.println("Kid: "+kid+" "+name);
							
							String geocode = "";
							
							try{
								geocode = utils.escapeHtml(results.get(0).getFormattedAddress());
							}catch(Exception e){}
							list.put("kid", kid);
							list.put("name",name);
							list.put("coord", coord);
							list.put("current_address",geocode);
							list.put("address",address);
							list.put("active", isActive);
							list.put("avatar",avatar);
							list.put("contacts",contactos);
							list.put("chat",chat);
							listaKids.add(list);
						}while(rs.next());
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("list", listaKids);
						}};
					}
					response = genson.serialize(json);
					break;
				case "forgot":
					String email = POST_("correo");
					rs = stmt.executeQuery("SELECT id,nombre FROM parents WHERE email = '"+mysql_real_escape_string(email)+"' LIMIT 1");
					if(rs.next() == true) {
						
						String pass = passGen.generate(2,2,2);
						stmt.executeUpdate("UPDATE parents SET passReset = '"+mysql_real_escape_string(pass)+"' WHERE email = '"+mysql_real_escape_string(email)+"' LIMIT 1");
						String cuerpo = "Hola, recientemente solicit&oacute; recuperar el acceso a su cuenta, aqu&iacute; est&aacute; su nombre de usuario (o correo), y una contrase&ntilde;a provisional.<br/>Asegurese de cambiarla una vez haya iniciado sesi&oacute;n en Presens, tocando el &iacute;cono de l&aacute;piz en la parte superior derecha de la pantalla.<br/><br/>Correo: "+email+"<br/>Contrase&ntilde;a Provisional: "+pass+"<br/><br/><b>Blue App Team</b>";
						String asunto = "Recuperación de Contraseña";
						mailProc.send(email,asunto,cuerpo);
						json = new HashMap<String, Object>() {{
							put("status", "OK");
						}};
					}else{
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}
					response = genson.serialize(json);
					break;
				case "logout":
					stmt.executeUpdate("DELETE FROM notifs WHERE token = '"+mysql_real_escape_string(POST_("token"))+"' AND regID = '"+mysql_real_escape_string(POST_("regId"))+"'");
					break;
				case "editShadow":
					token = POST_("token");
					name = POST_("name");
					String phone = POST_("tel");
					String skype = POST_("skype");
					String correo = POST_("correo");
					String action = POST_("action");
					
					String pass = POST_("pass");
					String passConf = POST_("passConf");
					System.out.println("Pass: "+pass+",Conf: "+passConf+".");
					String radius = POST_("radio");
					String exitPin = POST_("exitPin");
					userAvatar = POST_("avatar");
					pAvatar = 0;
					rs = stmt.executeQuery("SELECT id,pass,avatar FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$password,$pAvatar) = mysql_fetch_array($q);
					rs.first();
					uid = rs.getInt("id");
					String password = rs.getString("pass");
					pAvatar = rs.getInt("avatar");
					
					if(password != pass && password != passConf && pass == passConf && pass.trim().length() >0) {
						password = pass;	
					}
					if(name.trim().length() == 0 || correo.trim().length() == 0 || phone.trim().length()==0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else if(mailProc.isValid(correo) == false){
						json = new HashMap<String, Object>() {{
							put("status", "EMAIL");
						}};
					}else if((pass.trim().length() >0 || passConf.trim().length()>0) && passConf !=pass) {
						json = new HashMap<String, Object>() {{
							put("status", "PASS");
						}};
					}else{
						if(userAvatar.length() > 0) {
							avatarProc.save(uid,"parents",userAvatar);
							stmt2.executeUpdate("UPDATE parents SET nombre = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',tel = '"+mysql_real_escape_string(phone)+"',email = '"+mysql_real_escape_string(correo)+"',pass = '"+mysql_real_escape_string(password)+"',passReset='', skype = '"+mysql_real_escape_string(skype)+"', exitPin = '"+mysql_real_escape_string(exitPin)+"', dist_radius='"+mysql_real_escape_string(radius)+"', action = '"+mysql_real_escape_string(action)+"',avatar=1 WHERE id = '"+uid+"' LIMIT 1");
							pAvatar = 1;
						}else{
							stmt2.executeUpdate("UPDATE parents SET nombre = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',tel = '"+mysql_real_escape_string(phone)+"',email = '"+mysql_real_escape_string(correo)+"',pass = '"+mysql_real_escape_string(password)+"',passReset='',skype = '"+mysql_real_escape_string(skype)+"', exitPin = '"+mysql_real_escape_string(exitPin)+"', dist_radius='"+mysql_real_escape_string(radius)+"', action = '"+mysql_real_escape_string(action)+"' WHERE id = '"+uid+"' LIMIT 1");		
							pAvatar = 0;
						}
						
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("name",POST_("name"));
						}};
						json.put("avatar",(pAvatar == 1) ? "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+rs.getInt("id")+".png" : "");
						
					}
					response = genson.serialize(json);
					break;
				case "postChat":
					token = POST_("token");
					boolean kidMode = Boolean.parseBoolean(POST_("kidMode"));
					boolean shadowMode = Boolean.parseBoolean(POST_("shadowMode"));
					String msg = POST_("message");
					int encode = Integer.parseInt(POST_("encode"));
					String message = "";
					if(encode == 1)
						message = utils.escapeHtml(msg);
					else
						message = msg;
					kid = Integer.parseInt(POST_("kid"));
					int isKid = 0;
					if(kidMode == true)
						isKid = 1;
					Long timestamp = time();
					rs1 = stmt.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+kid+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pAvatar,$kAvatar,$pname,$kname) = mysql_fetch_array($q);
					rs1.first();
					uid = rs1.getInt("id");
					if(kidMode)
						stmt2.executeUpdate("INSERT INTO chat (isKid,kid,parent,message,timeStamp,seenByKid) VALUES ('"+isKid+"','"+kid+"','"+uid+"','"+mysql_real_escape_string(message)+"','"+timestamp+"',1)", Statement.RETURN_GENERATED_KEYS);
					if(shadowMode)
						stmt2.executeUpdate("INSERT INTO chat (isKid,kid,parent,message,timeStamp,seenByParent) VALUES ('"+isKid+"','"+kid+"','"+uid+"','"+mysql_real_escape_string(message)+"','"+timestamp+"',1)", Statement.RETURN_GENERATED_KEYS);
					ResultSet rsAux = stmt2.getGeneratedKeys();
					rsAux.next();
					int chatId = rsAux.getInt(1);
					
					if(isKid == 0){
						if(rs1.getInt("pAvatar") == 1) 
							userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
						else
							userAvatar = "images/parent.png";
					}else{
						if(rs1.getInt("kAvatar") == 1) 
							userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kid+".png";
						else
							userAvatar = "images/kid.png";
					}
					final HashMap<String,Object>postHtml = new HashMap<String, Object>() {{
						put("status", "OK");
					}};
					postHtml.put("id", chatId);
					postHtml.put("name", (isKid==0) ? rs1.getString("pName") : rs1.getString("kName"));
					postHtml.put("avatar", userAvatar);
					postHtml.put("time", timestamp);
					postHtml.put("message",msg);
					postHtml.put("class", (isKid==0) ? "parentChat" : "kidChat");
					postHtml.put("isPic",0);
					
					json = new HashMap<String, Object>() {{
						put("status", "OK");
						put("post", postHtml);
					}};
					response = genson.serialize(json);
					break;
				case "sendPic":
					token = POST_("token");
					String image = POST_("image");
					kid = Integer.parseInt(POST_("kid"));
					String key = passGen.generate(3,3,3);
					photoProc.save(image,key);
					
					msg = "<img src=\"http://"+Main.ip+":"+Main.port+"/pics/"+key+"_thumb.jpg\" class=\"parentPic\" full=\""+key+"\"/>";
					timestamp = time();
					rs1 = stmt.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+kid+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pAvatar,$kAvatar,$pname,$kname) = mysql_fetch_array($q);
					rs1.first();
					uid = rs1.getInt("id");
					stmt2.executeUpdate("INSERT INTO chat (isKid,kid,parent,message,timeStamp,isPic,seenByParent) VALUES ('0','"+kid+"','"+uid+"','"+msg+"','"+timestamp+"',1,1)", Statement.RETURN_GENERATED_KEYS);
					rsAux = stmt2.getGeneratedKeys();
					rsAux.next();
					cid = rsAux.getInt(1);
					
					if(rs1.getInt("pAvatar") == 1) 
						userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
					else
						userAvatar = "images/parent.png";
					
					final HashMap<String,Object>picHtml = new HashMap<String, Object>();
					picHtml.put("id", cid);
					picHtml.put("name", rs1.getString("pName"));
					picHtml.put("avatar", userAvatar);
					picHtml.put("time", timestamp);
					picHtml.put("message",msg);
					picHtml.put("class", "parentChat");
					picHtml.put("isPic",1);
					json = new HashMap<String, Object>() {{
						put("status", "OK");
						put("post", picHtml);
					}};
					response = genson.serialize(json);
					break;
				case "alertParent":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					String eType = POST_("emergencyType");
					String submessage = "";
					rs = stmt.executeQuery("SELECT p.id,p.avatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName,p.last_active FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+kid+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pAvatar,$kAvatar,$pname,$kname,$last) = mysql_fetch_array($q);
					rs.first();
					uid = rs.getInt("id");
					message = "";
					String kname = rs.getString("kName");
					switch(eType) {
						case "POLICE":
							submessage = "¡Necesito que llames a la policía, por favor!";
						break;
						case "MEDIC":
							submessage = "¡Tengo una emergencia medica!";
						break;
						case "FIRE":
							submessage = "¡Necesito que llames a los bomberos, por favor!";
						break;
						default:
							submessage = "Necesito asistencia, llama a mi casa o al dispositivo que cargo. ¡Gracias!";
						break;
					}
					message = "¡Soy "+kname+"! "+submessage;
					msg = "<div class=\"chatText\">"+utils.escapeHtml(message)+"</div>";
					
					timestamp = time();
					new pushNotification(token,"Mensaje de "+kname,submessage,POST_("regId"),kname,eType);
					/*[DEPRECATED] stmt2.executeUpdate("INSERT INTO notifs (kid,parent,message) VALUES ('"+kid+"','"+uid+"','"+message+"')");*/
					stmt2.executeUpdate("INSERT INTO chat (isKid,kid,parent,message,timeStamp,isPic) VALUES ('1','"+kid+"','"+uid+"','"+msg+"','"+timestamp+"',0)", Statement.RETURN_GENERATED_KEYS);
					rsAux = stmt2.getGeneratedKeys();
					rsAux.next();
					cid = rsAux.getInt(1);
					
					if(rs.getInt("Avatar") == 1) 
						userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kid+".png";
					else
						userAvatar = "images/kid.png";
					
					final HashMap<String,Object>alertHtml = new HashMap<String, Object>();
					alertHtml.put("id", cid);
					alertHtml.put("name", rs.getString("kName"));
					alertHtml.put("avatar", userAvatar);
					alertHtml.put("time", timestamp);
					alertHtml.put("message",msg);
					alertHtml.put("class", "kidChat");
					alertHtml.put("isPic",0);
					json = new HashMap<String, Object>() {{
						put("status", "OK");
						
					}};
					json.put("post", alertHtml);
					json.put("message",msg);
					json.put("sendSMS",(time() - rs.getLong("last_active") <= 180) ? 0 : 1);
					response = genson.serialize(json);
					break;
				case "kidData":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					rs1 = stmt2.executeQuery("SELECT p.id,k.default_coords,k.coord_address FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+kid+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$coord,$address) = mysql_fetch_array($q);
					rs1.first();
					rs = stmt.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
					//$contacts = array();
					final List<Map>contactos = new ArrayList<Map>();
					while(rs.next()) {
						//list($id,$name,$num,$avatar,$skype) = $r;
						contactAvatar = "";
						if(rs.getInt("contact_avatar") == 1)
							contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/contacts/"+rs.getInt("id")+".png";
						
						Map<String, Object> lista = new HashMap<String, Object>(){{
							put("cid",rs.getInt("id"));
							put("name",rs.getString("contact_name"));
							put("tel",rs.getString("contact_number"));
							put("avatar",contactAvatar);
							put("skype", rs.getString("contact_skype"));
						}};
						contactos.add(lista);
					}
					/*$json = array(
						'status'=>'OK',
						'coord'=>$coord,
						'address'=>$address,
						'contacts'=>$contacts
					);*/
					json = new HashMap<String, Object>() {{
						put("status", "OK");
						put("coord", rs1.getString("default_coords"));
						put("address",rs1.getString("coord_address"));
						put("contacts",contactos);
					}};
					response = genson.serialize(json);
					break;
				/* [DEPRECATED]
				 * case "notifPoll":
					stmt3 = conn2.createStatement();
					token = POST_("token");
					rs = stmt.executeQuery("SELECT id FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					rs.next();
					//list($uid) = mysql_fetch_array($q);
					uid = rs.getInt("id");
					stmt.executeUpdate("UPDATE parents SET last_active = UNIX_TIMESTAMP() WHERE id = '"+uid+"' LIMIT 1");
					//notifs = array();
					final List<Map> lista = new ArrayList<Map>();
					rs = stmt.executeQuery("SELECT id,message FROM notifs WHERE parent = '"+uid+"' ORDER BY id ASC");
					while(rs.next()) {
						//list($nid,$message) = $r;
						Map<String, Object> mensaje = new HashMap<String, Object>(){{
							put("msg",  rs.getString("message"));
						}};
						lista.add(mensaje);
						stmt3.executeUpdate("DELETE FROM notifs WHERE id = '"+rs.getInt("id")+"'");
					}
					response = genson.serialize(lista);
					break;*/
				case "shadowPoll":
					final List<Map> html = new ArrayList<Map>();
					final List<Map> notifs = new ArrayList<Map>();
					//stmt2 = conn2.createStatement();
					
					token = POST_("token");
					kidID = POST_("kidID");
					rs1 = stmt2.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName,k.last_active,k.coords FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+mysql_real_escape_string(kidID)+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					
					rs1.first();
					uid = rs1.getInt("id");
					stmt3.executeUpdate("UPDATE parents SET last_active = UNIX_TIMESTAMP() WHERE id = '"+uid+"' LIMIT 1");
					stmt.executeUpdate("UPDATE chat SET seenByParent = 1 WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"'");
					if(rs1.getInt("kAvatar") == 1) {
						userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kidID+".png";	
					}else{
						userAvatar = "";	
					}
					
					if(POST_("requestAvatar") == "0")
						userAvatar = "";
					/* [DEPRECATED]
					rs = stmt.executeQuery("SELECT id,message FROM notifs WHERE parent = '"+uid+"' ORDER BY id ASC");
					while(rs.next()) {
						//list($nid,$message) = $r;
						Map<String, Object> spNotifMensaje = new HashMap<String, Object>(){{
							put("msg", rs.getString("message"));
						}};
						notifs.add(spNotifMensaje);
						stmt3.executeUpdate("DELETE FROM notifs WHERE id = '"+rs.getInt("id")+"'");
					}*/
					
				 	rs = stmt.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic FROM (SELECT id,isKid,message,timeStamp,isPic FROM chat WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");
					
					while(rs.next()) {
						if(rs.getInt("isKid") == 0){
							if(rs1.getInt("pAvatar") == 1) 
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
							else
								contactAvatar = "images/parent.png";
						}else{
							if(rs1.getInt("kAvatar") == 1) 
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kidID+".png";
							else
								contactAvatar = "images/kid.png";
						}
						Map<String, Object> chat = new HashMap<String, Object>(){{
							put("id", rs.getInt("id"));
							put("name", (rs.getInt("isKid") == 0) ? rs1.getString("pName") : rs1.getString("kName"));
							put("avatar",contactAvatar);
							put("time",rs.getString("timeStamp"));
							put("message",utils.unescapeHtml(rs.getString("message")));
							put("class",(rs.getInt("isKid") == 0) ? "parentChat" : "kidChat");
							put("isPic",rs.getInt("isPic"));
						}};
						html.add(chat);
					}
					
					json = new HashMap<String, Object>() {{
						put("active",(time() - rs1.getLong("last_active") <= 180) ? "online" : "offline");
						put("coords",rs1.getString("coords"));
						put("avatar",userAvatar);
						put("chat",html);
						/*[DEPRECATED] put("notifs",notifs);*/
					}};
					response = genson.serialize(json);
					break;
				case "addKid":
					final List<Map> list = new ArrayList<Map>();
					token = POST_("token");
					name = POST_("nombre");
					rs = stmt.executeQuery("SELECT id FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid) = mysql_fetch_array($q);
					rs.next();
					uid = rs.getInt("id");
					if(name.trim().length() == 0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						Long time_ = time() - 190;
						stmt.executeUpdate("INSERT INTO kids (parent,nombre,last_active) VALUES ('"+uid+"','"+mysql_real_escape_string(utils.escapeHtml(name))+"','"+time_+"')");
						
						rs = stmt.executeQuery("SELECT k.id,k.nombre,k.sexo,k.last_active FROM kids AS k WHERE k.parent = '"+uid+"' ORDER BY k.nombre ASC");
						if(rs.first() == false) {
							json = new HashMap<String, Object>() {{
								put("status", "EMPTY");
							}};
						}else{
							//$list = array();
							try {
								do {
									//list($kid,$name,$sex,$last) = $r;
									
									Map<String, Object> lista_ = new HashMap<String, Object>(){{
										put("kid", rs.getInt("id"));
										put("name",rs.getString("nombre"));
										put("active",(time() - rs.getLong("last_active") <= 180) ? "online" : "offline");
									}};
									list.add(lista_);
								}while(rs.next());
								json = new HashMap<String, Object>() {{
									put("status", "OK");
									put("list",list);
								}};
							}catch(NullPointerException e){
								System.out.println("Told ya..."+e);
							}
						}
					}
					response = genson.serialize(json);
					break;
				case "editKid":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					name = POST_("name");
					String default_coord = POST_("default_coords");
					String default_address = POST_("coord_address");
					userAvatar = POST_("avatar");
					
					rs = stmt.executeQuery("SELECT id FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid) = mysql_fetch_array($q);
					rs.first();
					uid = rs.getInt("id");
					if(name.trim().length() == 0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						if(userAvatar.trim().length() > 0) 
							stmt.executeUpdate("UPDATE kids SET nombre = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',avatar=1,default_coords = '"+mysql_real_escape_string(default_coord)+"',coord_address='"+mysql_real_escape_string(utils.escapeHtml(default_address))+"' WHERE parent = '"+uid+"' AND id = '"+kid+"' LIMIT 1");
						else
							stmt.executeUpdate("UPDATE kids SET nombre = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',default_coords = '"+mysql_real_escape_string(default_coord)+"',coord_address='"+mysql_real_escape_string(utils.escapeHtml(default_address))+"' WHERE parent = '"+uid+"' AND id = '"+kid+"' LIMIT 1");

						rs  = stmt2.executeQuery("SELECT last_active,coords,avatar FROM kids WHERE id = '"+kid+"' LIMIT 1");
						rs.first();
						//list($last,$coords,$oldAvatar) = mysql_fetch_array($q);
						avatarProc.save(kid,"kid",userAvatar);
						
						if(rs.getInt("avatar") == 1) {
							userAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kid+".png";	
						}else{
							userAvatar = "";	
						}
						
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("active",(time() - rs.getLong("last_active") <= 180) ? "online" : "offline");
							put("avatar",userAvatar);
						}};
						
					}
					response = genson.serialize(json);
					break;
				case "addContact":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					name = POST_("name");
					phone = POST_("phone");
					skype = POST_("skype");
					userAvatar = POST_("avatar");
					cid = 0;
					
					rs1 = stmt.executeQuery("SELECT id,nombre,tel,avatar,skype FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pName,$pTel,$pAvatar) = mysql_fetch_array($q);
					rs1.first();
					if(name.trim().length() == 0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						if(userAvatar.length() > 0) {
							stmt2.executeUpdate("INSERT INTO contacts (kid,contact_name,contact_number,contact_skype,contact_avatar) VALUES ('"+kid+"','"+mysql_real_escape_string(utils.escapeHtml(name))+"','"+mysql_real_escape_string(phone)+"','"+mysql_real_escape_string(skype)+"',1)", Statement.RETURN_GENERATED_KEYS);
						}else{
							stmt2.executeUpdate("INSERT INTO contacts (kid,contact_name,contact_number,contact_skype) VALUES ('"+kid+"','"+mysql_real_escape_string(utils.escapeHtml(name))+"','"+mysql_real_escape_string(phone)+"','"+mysql_real_escape_string(skype)+"')", Statement.RETURN_GENERATED_KEYS);
						}
						rsAux = stmt2.getGeneratedKeys();
						rsAux.next();
						cid = rsAux.getInt(1);
						avatarProc.save(cid,"contacts",userAvatar);
						rs2 = stmt2.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
						
						final ArrayList<Map>addContactList = new ArrayList<Map>();
						addContactList.addAll(new VIPContacts(rs1.getInt("id"),rs1.getString("nombre"),rs1.getString("tel"),rs1.getInt("avatar"),rs1.getString("skype")).prepend());
						
						while(rs2.next()) {
							
							//list($id,$cname,$num,$cAvatar,$skype) = $r_;
							
							if(rs2.getInt("contact_avatar") == 1)
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/contacts/"+rs2.getInt("id")+".png";
							else
								contactAvatar = "";
							Map<String, Object> listaContactos = new HashMap<String, Object>(){{
								put("type", "contact");
								put("cid",rs2.getInt("id"));
								put("name",rs2.getString("contact_name"));
								put("tel",rs2.getString("contact_number"));
								put("avatar",contactAvatar);
								put("skype", rs2.getString("contact_skype"));
							}};
							addContactList.add(listaContactos);
						}
						
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("contacts",addContactList);
						}};
						
					}
					response = genson.serialize(json);
					break;
				case "editContact":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					cid = Integer.parseInt(POST_("cid"));
					name = POST_("name");
					phone = POST_("phone");
					skype = POST_("skype");
					userAvatar = POST_("avatar");
					
					rs1 = stmt.executeQuery("SELECT id,nombre,tel,avatar,skype FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pName,$pTel,$pAvatar) = mysql_fetch_array($q);
					rs1.first();
					if(name.trim().length() == 0) {
						json = new HashMap<String, Object>() {{
							put("status", "ERROR");
						}};
					}else{
						if(userAvatar.length() > 0) {
							avatarProc.save(cid,"contacts",userAvatar);
							stmt2.executeUpdate("UPDATE contacts SET contact_name = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',contact_number = '"+mysql_real_escape_string(phone)+"',contact_skype = '"+mysql_real_escape_string(skype)+"', contact_avatar=1 WHERE id = '"+cid+"' AND kid = '"+kid+"' LIMIT 1");
						}else{
							stmt2.executeUpdate("UPDATE contacts SET contact_name = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',contact_number = '"+mysql_real_escape_string(phone)+"',contact_skype = '"+mysql_real_escape_string(skype)+"' WHERE id = '"+cid+"' AND kid = '"+kid+"' LIMIT 1");
						}
						
						rs2 = stmt2.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
						
						final ArrayList<Map>editContactList = new ArrayList<Map>();
						editContactList.addAll(new VIPContacts(rs1.getInt("id"),rs1.getString("nombre"),rs1.getString("tel"),rs1.getInt("avatar"),rs1.getString("skype")).prepend());
						
						while(rs2.next()) {
							
							//list($id,$cname,$num,$cAvatar,$skype) = $r_;
							
							if(rs2.getInt("contact_avatar") == 1)
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/contacts/"+rs2.getInt("id")+".png";
							else
								contactAvatar = "";
							Map<String, Object> listaContactos = new HashMap<String, Object>(){{
								put("type", "contact");
								put("cid",rs2.getInt("id"));
								put("name",rs2.getString("contact_name"));
								put("tel",rs2.getString("contact_number"));
								put("avatar",contactAvatar);
								put("skype", rs2.getString("contact_skype"));
							}};
							editContactList.add(listaContactos);
						}
						
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("contacts",editContactList);
						}};
						
					}
					response = genson.serialize(json);
					break;
				case "deleteContact":
					token = POST_("token");
					kid = Integer.parseInt(POST_("kid"));
					cid = Integer.parseInt(POST_("cid"));
					
					rs1 = stmt.executeQuery("SELECT id,nombre,tel,avatar,skype FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid) = mysql_fetch_array($q);
					rs1.first();
					uid = rs1.getInt("id");
					if(uid >0) {
						stmt2.executeUpdate("DELETE FROM contacts WHERE kid = '"+kid+"' AND id = '"+cid+"' LIMIT 1");
						try {
							File file = new File("\\BlueServer\\avatars\\contacts\\"+cid+".png");
							 
				    		if(file.delete()){
				    			System.out.println("Avatar de contacto eliminado.");
				    		}
						}catch(Exception e){}
						rs2 = stmt2.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
						
						final ArrayList<Map>delContactList = new ArrayList<Map>();
						delContactList.addAll(new VIPContacts(rs1.getInt("id"),rs1.getString("nombre"),rs1.getString("tel"),rs1.getInt("avatar"),rs1.getString("skype")).prepend());
						
						while(rs2.next()) {
							
							//list($id,$cname,$num,$cAvatar,$skype) = $r_;
							
							if(rs2.getInt("contact_avatar") == 1)
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/contacts/"+rs2.getInt("id")+".png";
							else
								contactAvatar = "";
							Map<String, Object> listaContactos = new HashMap<String, Object>(){{
								put("type", "contact");
								put("cid",rs2.getInt("id"));
								put("name",rs2.getString("contact_name"));
								put("tel",rs2.getString("contact_number"));
								put("avatar",contactAvatar);
								put("skype", rs2.getString("contact_skype"));
							}};
							delContactList.add(listaContactos);
						}
						
						json = new HashMap<String, Object>() {{
							put("status", "OK");
							put("contacts",delContactList);
						}};
					}else{
						json = new HashMap<String, Object>() {{
							put("status","ERROR");
						}};
					}
					response = genson.serialize(json);
					break;
				case "kidPoll":
					final ArrayList<Map> htmlContainer = new ArrayList<Map>();
					stmt3 = conn2.createStatement();
					token = POST_("token");
					kidID = POST_("kidID");
					String lat = POST_("lat");
					String lon = POST_("lon");
					String coords = lat+", "+lon;
					
					final ResultSet rsPoll = stmt3.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+mysql_real_escape_string(kidID)+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					//list($uid,$pAvatar,$kAvatar,$pname,$kname) = mysql_fetch_array($q);
					rsPoll.first();
					uid = rsPoll.getInt("id");
					
					if(coords != "0, 0")
						stmt.executeUpdate("UPDATE kids SET last_active = UNIX_TIMESTAMP(),coords = '"+coords+"' WHERE id = '"+mysql_real_escape_string(kidID)+"' LIMIT 1");
					else
						stmt.executeUpdate("UPDATE kids SET last_active = UNIX_TIMESTAMP() WHERE id = '"+mysql_real_escape_string(kidID)+"' LIMIT 1");
					stmt.executeUpdate("UPDATE chat SET seenByKid = 1 WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"'");
					rs = stmt.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic FROM (SELECT id,isKid,message,timeStamp,isPic FROM chat WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");

					while(rs.next()) {
						//list($cid,$isKid,$msg,$timestamp,$isPic) = $r;
						
						if(rs.getInt("isKid") == 0){
							if(rsPoll.getInt("pAvatar") == 1) 
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/parents/"+uid+".png";
							else
								contactAvatar = "images/parent.png";
						}else{
							if(rsPoll.getInt("kAvatar") == 1) 
								contactAvatar = "http://"+Main.ip+":"+Main.port+"/avatars/kid/"+kidID+".png";
							else
								contactAvatar = "images/kid.png";
						}
						Map<String, Object> chat = new HashMap<String, Object>(){{
							put("id", rs.getInt("id"));
							put("name", (rs.getInt("isKid") == 0) ? rsPoll.getString("pName") : rsPoll.getString("kName"));
							put("avatar",contactAvatar);
							put("time",rs.getString("timeStamp"));
							put("message",rs.getString("message"));
							put("class",(rs.getInt("isKid") == 0) ? "parentChat" : "kidChat");
							put("isPic",rs.getInt("isPic"));
						}};
						
						htmlContainer.add(chat);
					}
					json = new HashMap<String, Object>() {{
						put("chat",htmlContainer);
					}};
					response = genson.serialize(json);
					break;
				case "saveRegId":
					token = POST_("token");
					String regId = POST_("regId");
					stmt.executeUpdate("INSERT IGNORE INTO notifs SET regID = '"+mysql_real_escape_string(regId)+"',parent = '"+mysql_real_escape_string(token)+"'");
					stmt.executeUpdate("UPDATE parents SET gcmRegID = '"+mysql_real_escape_string(regId)+"' WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
					
					json = new HashMap<String, Object>() {{
						put("status","OK");
					}};
					response = genson.serialize(json);
				break;
				default:
					isAsset = true;
					URI uri = t.getRequestURI();
					OutputStream os;
					FileInputStream fs = null;
					Headers headers = t.getResponseHeaders();
					headers.add("Access-Control-Allow-Origin", "*");
					
					try{
					    File file = new File("/BlueServer/"+uri.getPath()).getAbsoluteFile();
					    if (!file.isFile()) {
					    	headers.add("Content-type","text/html");
					    	if(file.getName().trim().equals("BlueServer") ) {
					    		String mysqlStatText = "---";
					    		response = "<html><head><title>Hola! - Blue Server</title><style>"
					    				+ "body{background:#2E8BF3;color:#FFF;font-family:Arial,sans-serif}";
					    		if(Main.mysqlStatus ==true) {
					    				response+= ".mysql{color:greenyellow;font-weight:bold}";
					    				mysqlStatText = "MySQL Conectado";
					    		}else{
					    				response+= ".mysql{color:wheat;font-weight:bold}";
					    				mysqlStatText = "MySQL Desconectado";
					    		}
					    		response+= "</style></head><body><b>Blue Server</b> est&aacute; en l&iacute;nea. :)<br/><br/>"
					    				+ "<ul class=\"mysql\"><li>"+mysqlStatText+"</li></ul>"
					    				+ "</body></html>";
					    		t.sendResponseHeaders(200, response.length());
					    	}else{
					    		response = "<html><head><title>Error 404 - Blue Server</title><style>"
					    				+ "body{background:#2E8BF3;color:#FFF;font-family:Arial,sans-serif}</style></head><body>";
								response+= "<b>404 (Not Found)</b><br/>"+file.getAbsolutePath()+"</body></html>";
								t.sendResponseHeaders(404, response.length());
					    	}
							os = t.getResponseBody();
							os.write(response.getBytes());
					    }else{
						    //System.out.println(file.getPath());
							t.sendResponseHeaders(200, 0);
						    os = t.getResponseBody();
						    fs = new FileInputStream(file);
						    final byte[] buffer = new byte[0x10000];
						    int count = 0;
						    while ((count = fs.read(buffer)) >= 0) {
						    	os.write(buffer,0,count);
						    }
						    fs.close();
					    }
					    os.close();
					}catch(FileNotFoundException e){
						response = "404 (Not Found)\n";
						t.sendResponseHeaders(404, response.length());
					    os = t.getResponseBody();
					    os.write(response.getBytes());
						os.close();
					}
				    break;
			}
			if(isAsset == false){
				Headers headers = t.getResponseHeaders();
				headers.add("Access-Control-Allow-Origin", "*");
				headers.add("Content-type","text/json");
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
		}catch(Exception e){
			
			String response = "Error 404\n";
			t.sendResponseHeaders(404, response.length());
			OutputStream os = t.getResponseBody();
		    os.write(response.getBytes());
			os.close();
		}
	}
	public static void startMysql() {
		dataSource = new MysqlDataSource();
		dataSource.setUser(Main.mysqlUser);
		dataSource.setPassword(Main.mysqlPass);
		dataSource.setServerName(Main.mysqlServer);
		dataSource.setDatabaseName(Main.mysqlDatabase);
	}
	public static String POST_(String p) {
		String paramValue = "";
		try{
			paramValue = params(url).get(p);
			if(paramValue == null || paramValue == "null")
				paramValue = "";
		}catch(Exception e){
			System.out.println("["+p+"] Param error...");
		}
		return paramValue;
	}
	public static String mysql_real_escape_string(String str)  throws Exception {
		 if (str == null) {
		     return null;
		 }
		
		 if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]","").length() < 1) {
		     return str;
		 }
		
		 String clean_string = str;
		 clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
		 clean_string = clean_string.replaceAll("\\n","\\\\n");
		 clean_string = clean_string.replaceAll("\\r", "\\\\r");
		 clean_string = clean_string.replaceAll("\\t", "\\\\t");
		 clean_string = clean_string.replaceAll("\\00", "\\\\0");
		 clean_string = clean_string.replaceAll("'", "\\\\'");
		 clean_string = clean_string.replaceAll("\\\"", "\\\\\"");
		
		 if (clean_string.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]"
		   ,"").length() < 1) 
		 {
		     return clean_string;
		 }
		
		 java.sql.Statement stmt = conn.createStatement();
		 String qry = "SELECT QUOTE('"+clean_string+"')";
		
		 stmt.executeQuery(qry);
		 java.sql.ResultSet resultSet = stmt.getResultSet();
		 resultSet.first();
		 String r = resultSet.getString(1);
		 return r.substring(1,r.length() - 1);       
	}
	public static Map<String, String> params(final String url) throws URISyntaxException {
	    return new HashMap<String, String>() {{
	        for(NameValuePair p : URLEncodedUtils.parse(new URI(url), "UTF-8")) 
	            put(p.getName(), p.getValue());
	    }};
	}

	public static long time() {
		return System.currentTimeMillis() / 1000L;
	}
}

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JFrame;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.owlike.genson.Genson;

import org.apache.commons.lang.StringEscapeUtils;

import javax.swing.JLabel;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.SwingConstants;

import java.awt.Font;

public class Main {
	/* HTTP START */
	static int port = 8000;
	/* HTTP END */
	
	/* MYSQL START */
	String mysqlUser = "bluetester";
	String mysqlPass = "fcqi@uabc";
	String mysqlServer = "blue.uabc.imeev.com";
	String mysqlDatabase = "blue_testing_db";
	/* MYSQL END */
	public static Connection conn;
	public static Connection conn2;
	public static Connection conn3;
	public static MysqlDataSource dataSource;
	public static Statement stmt;
	public static Statement stmt2;
	public static Statement stmt_;
	public static ResultSet rs;
	public static ResultSet rs2;
	public static ResultSet chatRS;
	public static String url;
	private JFrame frmBlueAppServer;
	public static String avatarUrl = "";
	public static String contactAvatar = "";
	public static String userAvatar = "";
	public static Genson genson = new Genson();
	public static int rowSet;
	public static StringEscapeUtils utils = new StringEscapeUtils();
	JLabel ipAddress;
	static String ip;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmBlueAppServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public Main() throws IOException {
		System.out.println("Inicializando servidor...");
		initialize();
		startServer(9000);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBlueAppServer = new JFrame();
		frmBlueAppServer.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/logoBig.png")));
		frmBlueAppServer.setResizable(false);
		frmBlueAppServer.setTitle("Blue App Server");
		frmBlueAppServer.setBounds(100, 100, 323, 412);
		frmBlueAppServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ipAddress = new JLabel("http://0.0.0.0");
		ipAddress.setForeground(Color.WHITE);
		ipAddress.setFont(new Font("Tahoma", Font.BOLD, 15));
		ipAddress.setHorizontalAlignment(SwingConstants.CENTER);
		frmBlueAppServer.getContentPane().add(ipAddress, BorderLayout.SOUTH);
		
		JLabel bgImg = new JLabel("");
		bgImg.setIcon(new ImageIcon(Main.class.getResource("/images/splash_320x426.png")));
		frmBlueAppServer.getContentPane().add(bgImg, BorderLayout.NORTH);
	}
	HttpServer server;
    ServerSocket server_socket;
   
	public void startServer(int port) throws IOException {
		this.port = port;
		dataSource = new MysqlDataSource();
		dataSource.setUser(mysqlUser);
		dataSource.setPassword(mysqlPass);
		dataSource.setServerName(mysqlServer);
		dataSource.setDatabaseName(mysqlDatabase);
		//dataSource.setPort(3306);
		
		try {
			conn = dataSource.getConnection();
			conn2 = dataSource.getConnection();
			stmt = conn.createStatement();
			stmt_ = conn.createStatement();
			stmt2 = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpServer server = HttpServer.create(new InetSocketAddress(this.port), 0);
        server.createContext("/login", new Handler("login")); 				//COMPLETO
        server.createContext("/register", new Handler("register"));			//COMPLETO
        server.createContext("/addKid",new Handler("addKid"));				//COMPLETO
        server.createContext("/editKid",new Handler("editKid"));			//COMPLETO
        server.createContext("/addContact",new Handler("addContact"));		//COMPLETO
        server.createContext("/editContact",new Handler("editContact"));	//COMPLETO
        server.createContext("/deleteContact",new Handler("deleteContact"));
        server.createContext("/kidData",new Handler("kidData"));			//COMPLETO
        server.createContext("/notifPoll",new Handler("notifPoll"));		//COMPLETO
        server.createContext("/shadowPoll",new Handler("shadowPoll"));		//COMPLETO
        server.createContext("/kidPoll",new Handler("kidPoll"));			//COMPLETO
        server.createContext("/getKidList", new Handler("getKidList"));		//COMPLETO
        server.createContext("/forgot",new Handler("forgot"));				//COMPLETO
        server.createContext("/editShadow",new Handler("editShadow"));		//COMPLETO
        server.createContext("/postChat",new Handler("postChat"));
        server.createContext("/sendPic",new Handler("sendPic")); 
        server.createContext("/alertParent",new Handler("alertParent"));
        server.createContext("/", new Handler("empty"));
        server.setExecutor(null); // creates a default executor
        server.start();
        
        //String ip = InetAddress.getLocalHost().getHostAddress();
        try {
        Socket s = new Socket("google.com", 80);
        ip = s.getLocalAddress().getHostAddress();
        s.close();
        System.out.println("Conexion abierta en: http://"+ip+":"+port);
        ipAddress.setText("http://"+ip+":"+port);
        }catch(Exception e) {
        	System.out.println("FUERA DE LINEA");
        	ipAddress.setText("http://127.0.0.1:"+port);
        	ip = "127.0.0.1";
        }
        
	}
	static class Handler implements HttpHandler {
		String section;
		//Map<String, List<String>> mapa = null;
		HashMap<String,String> mapa = null;
        public Handler(String part) {
        	section = part;
		}

		public void handle(HttpExchange t) throws IOException {
			url = t.getRequestURI().toString();
			Map<String, Object> json = null;
			Map<String, Object> jsonMap = null;
			
			try{
				
				String response = "";
				int kid = 0;
				String kidID = "0";
				final ResultSet rs1;
				String name="";
				final int auxInt=0;

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
								final String token = passwordGenerator(5,5,5);
								final int userID = stmt.executeUpdate("INSERT INTO parents (nombre,email,pass,tel,token) VALUES ('"+mysql_real_escape_string(utils.escapeHtml(POST_("nombre")))+"','"+mysql_real_escape_string(POST_("correo"))+"','"+mysql_real_escape_string(POST_("pass"))+"','"+mysql_real_escape_string(POST_("tel"))+"','"+token+"')", stmt.RETURN_GENERATED_KEYS);
								final Map<String, Object>userData = new HashMap<String, Object>() {{
									put("id",userID);
									put("name",POST_("nombre"));
									put("email",POST_("correo"));
									put("tel",POST_("tel"));
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
									avatarUrl = "http://"+ip+":"+port+"/avatars/parents/"+rs.getInt("id")+".png";
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
						stmt_ = conn2.createStatement();
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
								address = rs.getString("coord_address");
								
								rs2 = stmt_.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
								//auxData = prependVIPContacts(uid,pName,pTel,pAvatar,pSkype);
								final List<Map>contactos = new ArrayList<Map>();
								contactos.addAll(prependVIPContacts(uid,pName,pTel,pAvatar,pSkype));
								
								while(rs2.next()) {
									
									//list($id,$cname,$num,$cAvatar,$skype) = $r_;
									contactAvatar = "";
									if(rs2.getInt("contact_Avatar") == 1)
										contactAvatar = "http://"+ip+":"+port+"/avatars/contacts/"+rs2.getInt("id")+".png";
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
									avatar = "http://"+ip+":"+port+"/avatars/kid/"+kid+".png";	
								}else{
									avatar = "";
								}
								
								chatRS = stmt_.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic FROM (SELECT id,isKid,message,timeStamp,isPic FROM chat WHERE kid = '"+kid+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");
								String chat = "";
								if(chatRS.first()) {
									do{
										
										String cname="";
										String chatClass="";
										String cAvatar = "";
										int isKid = chatRS.getInt("isKid");
										int isPic = chatRS.getInt("isPic");
										int cid = chatRS.getInt("id");
										System.out.println("Chat ID: "+cid);
										String msg = chatRS.getString("message");
										String timestamp = chatRS.getString("timeStamp");
										//list($cid,$isKid,$msg,$timestamp,$isPic) = $rChat;
										if(isKid == 0){
											cname = pName;
											chatClass= "parentChat";
											if(pAvatar == 1) 
												cAvatar = "http://"+ip+":"+port+"/avatars/parents/"+uid+".png";
											else
												cAvatar = "images/parent.png";
										}else{
											cname = name;
											chatClass= "kidChat";
											if(kAvatar == 1) 
												cAvatar = "http://"+ip+":"+port+"/avatars/kid/"+kid+".png";
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
							
							String pass = passwordGenerator(2,2,2);
							stmt.executeUpdate("UPDATE parents SET passReset = '"+mysql_real_escape_string(pass)+"' WHERE email = '"+mysql_real_escape_string(email)+"' LIMIT 1");
							String cuerpo = "Hola, recientemente solicit&oacute; recuperar el acceso a su cuenta, aqu&iacute; est&aacute; su nombre de usuario (o correo), y una contrase&ntilde;a provisional.<br/>Asegurese de cambiarla una vez haya iniciado sesi&oacute;n en Presens, tocando el &iacute;cono de l&aacute;piz en la parte superior derecha de la pantalla.<br/><br/>Correo: "+email+"<br/>Contrase&ntilde;a Provisional: "+pass+"<br/><br/><b>Blue App Team</b>";
							String asunto = "Recuperación de Contraseña";
							enviar_correo(email,asunto,cuerpo);
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
						}else if(valid_email(correo) == false){
							json = new HashMap<String, Object>() {{
								put("status", "EMAIL");
							}};
						}else if((pass.trim().length() >0 || passConf.trim().length()>0) && passConf !=pass) {
							json = new HashMap<String, Object>() {{
								put("status", "PASS");
							}};
						}else{
							if(userAvatar.length() > 0) {
								saveAvatar(uid,"parents",userAvatar);
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
							json.put("avatar",(pAvatar == 1) ? "http://"+ip+":"+port+"/avatars/parents/"+rs.getInt("id")+".png" : "");
							
						}
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
								contactAvatar = "http://"+ip+":"+port+"/avatars/contacts/"+rs.getInt("id")+".png";
							
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
					case "notifPoll":
						stmt_ = conn2.createStatement();
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
							stmt_.executeUpdate("DELETE FROM notifs WHERE id = '"+rs.getInt("id")+"'");
						}
						response = genson.serialize(lista);
						break;
					case "shadowPoll":
						final List<Map> html = new ArrayList<Map>();
						final List<Map> notifs = new ArrayList<Map>();
						//stmt2 = conn2.createStatement();
						
						token = POST_("token");
						kidID = POST_("kidID");
						rs1 = stmt2.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName,k.last_active,k.coords FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+mysql_real_escape_string(kidID)+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
						
						rs1.first();
						uid = rs1.getInt("id");
						stmt_.executeUpdate("UPDATE parents SET last_active = UNIX_TIMESTAMP() WHERE id = '"+uid+"' LIMIT 1");
						if(rs1.getInt("kAvatar") == 1) {
							userAvatar = "http://uabc.imeev.com/app/avatars/kid/"+kidID+".png";	
						}else{
							userAvatar = "";	
						}
						
						if(POST_("requestAvatar") == "0")
							userAvatar = "";
						
						rs = stmt.executeQuery("SELECT id,message FROM notifs WHERE parent = '"+uid+"' ORDER BY id ASC");
						while(rs.next()) {
							//list($nid,$message) = $r;
							Map<String, Object> spNotifMensaje = new HashMap<String, Object>(){{
								put("msg", rs.getString("message"));
							}};
							notifs.add(spNotifMensaje);
							stmt_.executeUpdate("DELETE FROM notifs WHERE id = '"+rs.getInt("id")+"'");
						}
						
					 	rs = stmt.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic FROM (SELECT id,isKid,message,timeStamp,isPic FROM chat WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");
						
						while(rs.next()) {
							if(rs.getInt("isKid") == 0){
								if(rs1.getInt("pAvatar") == 1) 
									contactAvatar = "http://"+ip+":"+port+"/avatars/parents/"+uid+".png";
								else
									contactAvatar = "images/parent.png";
							}else{
								if(rs1.getInt("kAvatar") == 1) 
									contactAvatar = "http://"+ip+":"+port+"/avatars/kid/"+kidID+".png";
								else
									contactAvatar = "images/kid.png";
							}
							Map<String, Object> chat = new HashMap<String, Object>(){{
								put("id", rs.getInt("id"));
								put("name", (rs.getInt("isKid") == 0) ? rs1.getString("pName") : rs1.getString("kName"));
								put("avatar",contactAvatar);
								put("time",rs.getString("timeStamp"));
								put("message",rs.getString("message"));
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
							put("notifs",notifs);
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
							saveAvatar(kid,"kid",userAvatar);
							
							if(rs.getInt("avatar") == 1) {
								userAvatar = "http://"+ip+":"+port+"/avatars/kid/"+kid+".png";	
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
						int cid = 0;
						
						rs1 = stmt.executeQuery("SELECT id,nombre,tel,avatar,skype FROM parents WHERE token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
						//list($uid,$pName,$pTel,$pAvatar) = mysql_fetch_array($q);
						rs1.first();
						if(name.trim().length() == 0) {
							json = new HashMap<String, Object>() {{
								put("status", "ERROR");
							}};
						}else{
							if(userAvatar.length() > 0) {
								cid = stmt2.executeUpdate("INSERT INTO contacts (kid,contact_name,contact_number,contact_skype,contact_avatar) VALUES ('"+kid+"','"+mysql_real_escape_string(utils.escapeHtml(name))+"','"+mysql_real_escape_string(phone)+"','"+mysql_real_escape_string(skype)+"',1)", Statement.RETURN_GENERATED_KEYS);
							}else{
								cid = stmt2.executeUpdate("INSERT INTO contacts (kid,contact_name,contact_number,contact_skype) VALUES ('"+kid+"','"+mysql_real_escape_string(utils.escapeHtml(name))+"','"+mysql_real_escape_string(phone)+"','"+mysql_real_escape_string(skype)+"')", Statement.RETURN_GENERATED_KEYS);
							}
							saveAvatar(cid,"contacts",userAvatar);
							rs2 = stmt2.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
							
							final ArrayList<Map>addContactList = new ArrayList<Map>();
							addContactList.addAll(prependVIPContacts(rs1.getInt("id"),rs1.getString("nombre"),rs1.getString("tel"),rs1.getInt("avatar"),rs1.getString("skype")));
							
							while(rs2.next()) {
								
								//list($id,$cname,$num,$cAvatar,$skype) = $r_;
								
								if(rs2.getInt("contact_avatar") == 1)
									contactAvatar = "http://"+ip+":"+port+"/avatars/contacts/"+rs2.getInt("id")+".png";
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
								saveAvatar(cid,"contacts",userAvatar);
								stmt2.executeUpdate("UPDATE contacts SET contact_name = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',contact_number = '"+mysql_real_escape_string(phone)+"',contact_skype = '"+mysql_real_escape_string(skype)+"', contact_avatar=1 WHERE id = '"+cid+"' AND kid = '"+kid+"' LIMIT 1");
							}else{
								stmt2.executeUpdate("UPDATE contacts SET contact_name = '"+mysql_real_escape_string(utils.escapeHtml(name))+"',contact_number = '"+mysql_real_escape_string(phone)+"',contact_skype = '"+mysql_real_escape_string(skype)+"' WHERE id = '"+cid+"' AND kid = '"+kid+"' LIMIT 1");
							}
							
							rs2 = stmt2.executeQuery("SELECT id,contact_name,contact_number,contact_avatar,contact_skype FROM contacts WHERE kid = '"+kid+"' ORDER BY contact_name ASC");
							
							final ArrayList<Map>editContactList = new ArrayList<Map>();
							editContactList.addAll(prependVIPContacts(rs1.getInt("id"),rs1.getString("nombre"),rs1.getString("tel"),rs1.getInt("avatar"),rs1.getString("skype")));
							
							while(rs2.next()) {
								
								//list($id,$cname,$num,$cAvatar,$skype) = $r_;
								
								if(rs2.getInt("contact_avatar") == 1)
									contactAvatar = "http://"+ip+":"+port+"/avatars/contacts/"+rs2.getInt("id")+".png";
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
					case "kidPoll":
						final ArrayList<Map> htmlContainer = new ArrayList<Map>();
						stmt_ = conn2.createStatement();
						token = POST_("token");
						kidID = POST_("kidID");
						String lat = POST_("lat");
						String lon = POST_("lon");
						String coords = lat+", "+lon;
						
						final ResultSet rsPoll = stmt_.executeQuery("SELECT p.id,p.avatar AS pAvatar,k.avatar AS kAvatar,p.nombre AS pName,k.nombre AS kName FROM parents AS p LEFT JOIN kids AS k ON k.id = '"+mysql_real_escape_string(kidID)+"' WHERE p.token = '"+mysql_real_escape_string(token)+"' LIMIT 1");
						//list($uid,$pAvatar,$kAvatar,$pname,$kname) = mysql_fetch_array($q);
						rsPoll.first();
						uid = rsPoll.getInt("id");
						
						if(coords != "0, 0")
							stmt.executeUpdate("UPDATE kids SET last_active = UNIX_TIMESTAMP(),coords = '"+coords+"' WHERE id = '"+mysql_real_escape_string(kidID)+"' LIMIT 1");
						else
							stmt.executeUpdate("UPDATE kids SET last_active = UNIX_TIMESTAMP() WHERE id = '"+mysql_real_escape_string(kidID)+"' LIMIT 1");
						rs = stmt.executeQuery("SELECT c.id,c.isKid,c.message,c.timeStamp,c.isPic FROM (SELECT id,isKid,message,timeStamp,isPic FROM chat WHERE kid = '"+mysql_real_escape_string(kidID)+"' AND parent = '"+uid+"' ORDER BY id DESC LIMIT 10) AS c ORDER BY c.id ASC");

						while(rs.next()) {
							//list($cid,$isKid,$msg,$timestamp,$isPic) = $r;
							
							if(rs.getInt("isKid") == 0){
								if(rsPoll.getInt("pAvatar") == 1) 
									contactAvatar = "http://"+ip+":"+port+"/avatars/parents/"+uid+".png";
								else
									contactAvatar = "images/parent.png";
							}else{
								if(rsPoll.getInt("kAvatar") == 1) 
									contactAvatar = "http://"+ip+":"+port+"/avatars/kid/"+kidID+".png";
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
					default:
						try{
							URI uri = t.getRequestURI();
						    File file = new File(uri.getPath()).getAbsoluteFile();
						    System.out.println(file.getPath());
							t.sendResponseHeaders(200, 0);
						    OutputStream os = t.getResponseBody();
						    FileInputStream fs = new FileInputStream(file);
						    final byte[] buffer = new byte[0x10000];
						    int count = 0;
						    while ((count = fs.read(buffer)) >= 0) {
						    	os.write(buffer,0,count);
						    }
						    fs.close();
						    os.close();
						}catch(FileNotFoundException e){
							System.out.println(e);
						}
					    break;
				}
				Headers headers = t.getResponseHeaders();
				headers.add("Access-Control-Allow-Origin", "*");
				headers.add("Content-type","text/json");
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}catch(Exception e){
				System.out.println(e);
			}
        }
    }
	
	public static Map<String, List<String>> splitQuery(String urlStr) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		URL url=null;
		try {
			url = new URL("http://"+urlStr);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String[] pairs = url.getQuery().split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}
	public static Map<String, String> params(final String url) throws URISyntaxException {
	    return new HashMap<String, String>() {{
	        for(NameValuePair p : URLEncodedUtils.parse(new URI(url), "UTF-8")) 
	            put(p.getName(), p.getValue());
	    }};
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
	public static ResultSet mysql_query(String query) throws SQLException {
		ResultSet res = null;
		try{
			res = stmt.executeQuery(query);
		}catch(Exception e){}
		return res;
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
	public static long time() {
		return System.currentTimeMillis() / 1000L;
	}
	public static int getRows(ResultSet resultSet) throws SQLException {
		int size = 0;
		try {
		    resultSet.last();
		    size = resultSet.getRow();
		    resultSet.first();
		}
		catch(Exception ex) {
		    return 0;
		}
		return size;
	}
	public static String avatarAux = "";
	public static List<Map>prependVIPContacts(int uid, final String pName, final String pTel, final int pAvatar,final String pSkype) {
		
		if(pAvatar == 1)
			avatarAux = "http://"+ip+":"+port+"/avatars/parents/"+uid+".png";
		final List<Map>contacts = new ArrayList<Map>();
		Map<String, Object> parentMap = new HashMap<String, Object>(){{
			put("type", "parent");
			put("name",pName);
			put("tel",pTel);
			put("avatar",avatarAux);
			put("skype", pSkype);
		}};
		contacts.add(parentMap);
		Map<String, Object> policeMap = new HashMap<String, Object>(){{
			put("type", "police");
			put("name","Polic&iacute;a");
			put("avatar","images/police.png");
		}};
		contacts.add(policeMap);
		Map<String, Object> fireMap = new HashMap<String, Object>(){{
			put("type", "fire");
			put("name","Bomberos");
			put("avatar","images/fire.png");
		}};
		contacts.add(fireMap);
		
		Map<String, Object> medicMap = new HashMap<String, Object>(){{
			put("type", "medic");
			put("name","M&eacute;dico");
			put("avatar","images/medic.png");
		}};
		contacts.add(medicMap);
		
		return contacts;
	}
	public static String passwordGenerator(int val1,int val2,int val3) {
		char[] values1 = {'w','e','l','c','s','p','a','m','S','X','M','A','H','E','V','x'};
	    char[] values2 = {'Z','W','B','T','Y','P','I','L'};
	    char[] values3 = {'1','2','3','4','5','6','7','8','9','0'};
	    String out1="";
	    String out2="";
	    String out3="";
	    Random rand = new Random();
         for (int i=0;i<val1;i++) {
            int idx=rand.nextInt(values1.length);
            out1+= values1[idx];
         }
 
         for (int i=0;i<val3;i++) {
            int idx=rand.nextInt(values3.length);
             out2+= values3[idx];
         }
 
         for (int i=0;i<val2;i++) {
            int idx=rand.nextInt(values2.length);
             out3+= values2[idx];
         }
	 
	    String out= out1.concat(out3).concat(out2);
	    return out;
		
	}
	public static boolean valid_email(String email) {
        String ePattern = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
	}
	public static void saveAvatar(int id,String type,String newAvatar) {
		if( newAvatar.length() > 0) {
			
			try {
		        newAvatar = newAvatar.substring(22);
		        byte[] imgByteArray = Base64.decodeBase64(newAvatar);
		        
		        InputStream in = new ByteArrayInputStream(imgByteArray);
		        
		        BufferedImage bufferedImage = ImageIO.read(in);
		        ImageIO.write(bufferedImage, "png", new File("C:\\avatars\\"+type+"\\"+id+".png"));
			}catch(Exception ex){
		        System.out.println("AVATAR ERROR: "+ex);
		    }
			
		}
	}
	public static void saveImage(String img, String key) {
		try {
	        img = img.substring(22);
	        byte[] imgByteArray = Base64.decodeBase64(img);
	        
	        InputStream in = new ByteArrayInputStream(imgByteArray);
	        
	        BufferedImage image = ImageIO.read(in);
	        image = fillTransparentPixels(image,Color.WHITE);
	        
        	JPEGImageWriteParam param = new JPEGImageWriteParam(null);
        	param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        	param.setCompressionQuality((float) 0.85);
        	java.util.Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("jpg");
        	ImageWriter writer = it.next();
        	//dest.getParentFile().mkdirs();
   
        	writer.setOutput(new FileImageOutputStream(new File("C:\\pics\\"+key+".jpg")));
        	writer.write(null, new IIOImage(image, null, null), param);
        	param.setCompressionQuality((float) 0.20);
        	writer.setOutput(new FileImageOutputStream(new File("C:\\pics\\"+key+"_thumb.jpg")));
        	writer.write(null, new IIOImage(image, null, null), param);
        	writer.dispose();
		}catch(Exception ex){
	        System.out.println("IMAGE ERROR: "+ex);
	    }
	}
	public static BufferedImage fillTransparentPixels( BufferedImage image, Color fillColor ) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage image2 = new BufferedImage(w, h, 
		BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image2.createGraphics();
		g.setColor(fillColor);
		g.fillRect(0,0,w,h);
		g.drawRenderedImage(image, null);
		g.dispose();
		return image2;
	}
	
	public static void enviar_correo(String email, String asunto, String cuerpo) throws UnsupportedEncodingException {
		// Sender's email ID needs to be mentioned
	    String from = "presens@uabc.imeev.com";

	    // Get system properties
	    Properties properties = System.getProperties();

	    // Setup mail server
	    properties.setProperty("mail.smtp.host", "mail.imeev.com");
	    properties.setProperty("mail.smtp.port","587");
	    properties.setProperty("mail.smtp.auth", "true");

	    // Get the default Session object.
	    
	    Session session = Session.getDefaultInstance(properties,new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("presens@uabc.imeev.com", "Aan*e4xy");
            }
         });

	    try{
	    	// Create a default MimeMessage object.
	        MimeMessage message = new MimeMessage(session);

	        // Set From: header field of the header.
	        message.setFrom(new InternetAddress(from,"Blue App"));

	        // Set To: header field of the header.
	        message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(email));

	        // Set Subject: header field
	        message.setSubject(asunto, "UTF-8");

	        // Now set the actual message
	        message.setContent(cuerpo,"text/html");

	        // Send message
	        Transport.send(message);
	        System.out.println("Sent message successfully....");
	    }catch (MessagingException mex) {
	    	mex.printStackTrace();
	    }
	}
}

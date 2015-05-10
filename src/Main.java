import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
	/* HTTP START */
	static int port = 8000;
	/* HTTP END */
	
	/* MYSQL START */
	public static String mysqlUser = "bluetester";
	public static String mysqlPass = "fcqi@uabc";
	public static String mysqlServer = "blue.uabc.imeev.com";
	public static String mysqlDatabase = "blue_testing_db";
	/* MYSQL END */
	public static String ip = "127.0.0.1";
	private JFrame frmBlueAppServer;
	public static JLabel ipAddress;
	public static JLabel mysqlLabel;
	private JLabel openConn;
	HttpServer server;
    ServerSocket server_socket;
    URI ipUri;
    public static boolean mysqlStatus = false; //FALSE = Offline, TRUE = Online
    public static boolean httpStatus = false; //FALSE = Offline, TRUE = Online


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
		
		/* CREAR FOLDERS */
		File out = new File("\\BlueServer\\pics\\blank.txt");
    	out.getParentFile().mkdirs();
    	out = new File("\\BlueServer\\avatars\\kid\\blank.txt");
    	out.getParentFile().mkdirs();
    	out = new File("\\BlueServer\\avatars\\contacts\\blank.txt");
    	out.getParentFile().mkdirs();
    	out = new File("\\BlueServer\\avatars\\parents\\blank.txt");
    	out.getParentFile().mkdirs();
    	/* FIN CREAR FOLDERS */
    	
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
		frmBlueAppServer.getContentPane().setLayout(null);
		
		openConn = new JLabel("Conexi\u00F3n abierta en:");
		openConn.setFont(new Font("Tahoma", Font.BOLD, 11));
		openConn.setHorizontalAlignment(SwingConstants.CENTER);
		openConn.setForeground(Color.WHITE);
		openConn.setBounds(10, 333, 297, 14);
		frmBlueAppServer.getContentPane().add(openConn);
		
		ipAddress = new JLabel("<html><u>http://127.0.0.1</u>");
		ipAddress.setBounds(0, 354, 317, 19);
		ipAddress.setForeground(Color.WHITE);
		ipAddress.setFont(new Font("Tahoma", Font.BOLD, 15));
		ipAddress.setHorizontalAlignment(SwingConstants.CENTER);
		ipAddress.setCursor(new Cursor(Cursor.HAND_CURSOR));
		frmBlueAppServer.getContentPane().add(ipAddress);
	}
	public void startServer(final int port) throws IOException {
		
		mysqlLabel = new JLabel("---");
		mysqlLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		mysqlLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mysqlLabel.setForeground(Color.WHITE);
		mysqlLabel.setBounds(10, 31, 297, 18);
		frmBlueAppServer.getContentPane().add(mysqlLabel);
		JLabel bgImg = new JLabel("");
		bgImg.setBounds(0, 0, 317, 426);
		bgImg.setIcon(new ImageIcon(Main.class.getResource("/images/splash_320x426.png")));
		frmBlueAppServer.getContentPane().add(bgImg);
		
		this.port = port;
		new MySQLCheck(mysqlServer, mysqlUser, mysqlPass, mysqlDatabase).start();
		
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/login", new Handler("login")); 				//COMPLETO
        server.createContext("/register", new Handler("register"));			//COMPLETO
        server.createContext("/addKid",new Handler("addKid"));				//COMPLETO
        server.createContext("/editKid",new Handler("editKid"));			//COMPLETO
        server.createContext("/addContact",new Handler("addContact"));		//COMPLETO
        server.createContext("/editContact",new Handler("editContact"));	//COMPLETO
        server.createContext("/deleteContact",new Handler("deleteContact"));//COMPLETO
        server.createContext("/kidData",new Handler("kidData"));			//COMPLETO
        server.createContext("/notifPoll",new Handler("notifPoll"));		//COMPLETO
        server.createContext("/shadowPoll",new Handler("shadowPoll"));		//COMPLETO
        server.createContext("/kidPoll",new Handler("kidPoll"));			//COMPLETO
        server.createContext("/getKidList", new Handler("getKidList"));		//COMPLETO
        server.createContext("/forgot",new Handler("forgot"));				//COMPLETO
        server.createContext("/editShadow",new Handler("editShadow"));		//COMPLETO
        server.createContext("/postChat",new Handler("postChat"));			//COMPLETO
        server.createContext("/sendPic",new Handler("sendPic")); 			//COMPLETO
        server.createContext("/alertParent",new Handler("alertParent"));	//COMPLETO
        server.createContext("/", new Handler("empty"));
        server.setExecutor(null); // creates a default executor
        server.start();
        
        //String ip = InetAddress.getLocalHost().getHostAddress();
        new ConnCheck().start();
        ipAddress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					ipUri = new URI("http://"+ip+":"+port);
					if (Desktop.isDesktopSupported()) {
					      try {
					        Desktop.getDesktop().browse(ipUri);
					      } catch (IOException ex) { System.out.println("No openUrl"); }
					    } else {System.out.println("Desktop not supported."); }
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
	}

	static class Handler implements HttpHandler {
		String section;
        public Handler(String part) {
        	section = part;
		}

		public void handle(HttpExchange t) throws IOException {
			new Engine(section,t);
		}
    }
}

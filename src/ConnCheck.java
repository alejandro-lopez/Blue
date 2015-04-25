import java.net.Socket;


public class ConnCheck extends Thread{

	public ConnCheck() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				String ip;
				try {
			        Socket s = new Socket("google.com", 80);
			        ip = s.getLocalAddress().getHostAddress();
			        s.close();
			   
			        Main.ipAddress.setText("<html><u>http://"+ip+":"+Main.port+"</u></html>");
			        Main.ip = ip;
			        if(Main.httpStatus == false) {
			        	Main.httpStatus = true;
			        	System.out.println("Conexion abierta en: http://"+ip+":"+Main.port);
			        }
		        }catch(Exception e) {
		        	
		        	Main.ipAddress.setText("<html><u>http://127.0.0.1:"+Main.port+"</u></html>");
		        	Main.ip = "127.0.0.1";
		        	if(Main.httpStatus == true) {
		        		System.out.println("FUERA DE LINEA (http://127.0.0.1:"+Main.port+")");
		        		Main.httpStatus = false;
		        	}
		        }
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

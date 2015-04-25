import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;


public class OpenUrlAction  implements ActionListener{
	URI ipUri;
	public OpenUrlAction(URI uri) {
		ipUri = uri;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	      open(ipUri);
	}
	private static void open(URI uri) {
	    if (Desktop.isDesktopSupported()) {
	      try {
	        Desktop.getDesktop().browse(uri);
	      } catch (IOException e) { /* TODO: error handling */ }
	    } else { /* TODO: error handling */ }
	  }
}
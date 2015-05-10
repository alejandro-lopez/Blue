import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;


public class Avatar {

	public Avatar() {
		// TODO Auto-generated constructor stub
	}
	void save(int id,String type,String avatarB64) {
		if(avatarB64.length() > 0) {
			
			try {
		        avatarB64 = avatarB64.substring(22);
		        byte[] imgByteArray = Base64.decodeBase64(avatarB64);
		        
		        InputStream in = new ByteArrayInputStream(imgByteArray);
		        
		        BufferedImage bufferedImage = ImageIO.read(in);
		        ImageIO.write(bufferedImage, "png", new File("\\BlueServer\\avatars\\"+type+"\\"+id+".png"));
			}catch(Exception ex){
		        System.out.println("AVATAR ERROR: "+ex);
		    }
			
		}
	}
}

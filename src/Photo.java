import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.codec.binary.Base64;


public class Photo {

	public Photo() {
		// TODO Auto-generated constructor stub
	}
	void save(String img, String key) throws IOException {
		ImageWriter writer = null;
		ImageWriter writerThumb = null;
		FileImageOutputStream out1 = null;
		FileImageOutputStream out2 = null;
		try {
			
	        //img = img.substring(22);
	        byte[] imgByteArray = Base64.decodeBase64(img);
	        
	        InputStream in = new ByteArrayInputStream(imgByteArray);
	        
	        BufferedImage image = ImageIO.read(in);
	        in.close();
	        image = fillTransparentPixels(image,Color.WHITE);
	        
        	JPEGImageWriteParam param = new JPEGImageWriteParam(null);
        	param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        	param.setCompressionQuality((float) 0.85);
        	java.util.Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("jpg");
        	writer = it.next();
        	writerThumb = writer;
        	//dest.getParentFile().mkdirs();
        	out1 = new FileImageOutputStream(new File("\\BlueServer\\pics\\"+key+".jpg"));
        	writer.setOutput(out1);
        	writer.write(null, new IIOImage(image, null, null), param);
        	out1.flush();
        	out2 = new FileImageOutputStream(new File("\\BlueServer\\pics\\"+key+"_thumb.jpg"));
        	param.setCompressionQuality((float) 0.15);
        	writerThumb.setOutput(out2);
        	writerThumb.write(null, new IIOImage(image, null, null), param);
        	out2.flush();
        	
		}catch(IOException e) { 
	    	System.out.println("IMAGE ERROR: "+e);
	    	writer.abort(); 
	    	writerThumb.abort(); 
	    	throw e;
	    } finally {
			try {                           
				out1.close(); 
				out2.close();
			} catch(Exception inner) {}
			 writer.dispose();
			 writerThumb.dispose();
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
}

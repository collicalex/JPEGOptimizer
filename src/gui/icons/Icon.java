package gui.icons;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icon {

	private static Icon _instance = null;
	
	private BufferedImage _fileIcon;
	private BufferedImage _frameIcon;
	
	private ImageIcon _fileImageIcon;
	
	private ImageIcon _diffIcon;
	private ImageIcon _diffIconGray;
	
	private Icon() {
		createIcons();
	}
	
	public static Icon getInstance() {	
		if (_instance == null) { 	
			synchronized(Icon.class) {
				if (_instance == null) {
					_instance = new Icon();
				}
			}
		}
		return _instance;
	}
	
	public BufferedImage getFileIcon() {
		return _fileIcon;
	}
	
	public ImageIcon getFileImageIcon() {
		return _fileImageIcon;
	}	
	
	public BufferedImage getFrameIcon() {
		return _frameIcon;
	}
	
	public ImageIcon getDiffIcon() {
		return _diffIcon;
	}
	
	public ImageIcon getDiffIconGray() {
		return _diffIconGray;
	}
	
	private void createIcons() {
		createFileIcon();
		createFileImageIcon();
		createFrameIcon();
		createDiffIcon();
		createDiffIconGray();
	}
	
	private void createDiffIconGray() {
		int size = 20;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		g.fillRect(0,0,size,size);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		
		Image di = RGBGrayFilter.createDisabledImage(_fileIcon);
		
		int x = 0;
		int y = 1;
		g.drawImage(di, x, y, null);
		g.drawImage(di, x+3, y+3, null);
		
		g.dispose();
		
		_diffIconGray = new ImageIcon(img);		
	}
	
	private void createDiffIcon() {
		int size = 20;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		g.fillRect(0,0,size,size);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		
		int x = 0;
		int y = 1;
		g.drawImage(_fileIcon, x, y, null);
		g.drawImage(_fileIcon, x+3, y+3, null);
		
		g.dispose();
		
		_diffIcon = new ImageIcon(img);
	}
	
	private void createFrameIcon() {
		int size = 16;
		_frameIcon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) _frameIcon.getGraphics();
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		g.fillRect(0,0,size,size);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));	
		
		g.setColor(new Color(127,127,127));
		
		int x = 0;
		int y = 0+1;
		
		g.drawImage(_fileIcon,
			       x+0, y+0, x+11, y+9,
			       2, 2, 13, 11,
			       null);
		g.drawRect(x, y, 11, 9);
		
		x = 4;
		y = 4+1;
		g.drawImage(_fileIcon,
			       x+0, y+0, x+11, y+9,
			       2, 2, 13, 11,
			       null);
		g.drawRect(x, y, 11, 9);
		
		
		g.dispose();
	}
	
	private void createFileImageIcon() {
		_fileImageIcon = new ImageIcon(_fileIcon);
	}
	
	private void createFileIcon() {
		_fileIcon = new BufferedImage(16, 14, BufferedImage.TYPE_INT_RGB);
		_fileIcon.setRGB(0, 0, -5066062);
		_fileIcon.setRGB(0, 1, -5066062);
		_fileIcon.setRGB(0, 2, -5066062);
		_fileIcon.setRGB(0, 3, -5131855);
		_fileIcon.setRGB(0, 4, -5263441);
		_fileIcon.setRGB(0, 5, -5460820);
		_fileIcon.setRGB(0, 6, -5592406);
		_fileIcon.setRGB(0, 7, -5789785);
		_fileIcon.setRGB(0, 8, -5987164);
		_fileIcon.setRGB(0, 9, -6184543);
		_fileIcon.setRGB(0, 10, -6316129);
		_fileIcon.setRGB(0, 11, -6513508);
		_fileIcon.setRGB(0, 12, -6645094);
		_fileIcon.setRGB(0, 13, -6710887);
		_fileIcon.setRGB(1, 0, -5066062);
		_fileIcon.setRGB(1, 1, -1);
		_fileIcon.setRGB(1, 2, -1);
		_fileIcon.setRGB(1, 3, -65794);
		_fileIcon.setRGB(1, 4, -197380);
		_fileIcon.setRGB(1, 5, -328966);
		_fileIcon.setRGB(1, 6, -460552);
		_fileIcon.setRGB(1, 7, -592138);
		_fileIcon.setRGB(1, 8, -789517);
		_fileIcon.setRGB(1, 9, -921103);
		_fileIcon.setRGB(1, 10, -1052689);
		_fileIcon.setRGB(1, 11, -1184275);
		_fileIcon.setRGB(1, 12, -1315861);
		_fileIcon.setRGB(1, 13, -6710887);
		_fileIcon.setRGB(2, 0, -5066062);
		_fileIcon.setRGB(2, 1, -1);
		_fileIcon.setRGB(2, 2, -13203759);
		_fileIcon.setRGB(2, 3, -11759143);
		_fileIcon.setRGB(2, 4, -5977896);
		_fileIcon.setRGB(2, 5, -3480091);
		_fileIcon.setRGB(2, 6, -10778739);
		_fileIcon.setRGB(2, 7, -12489885);
		_fileIcon.setRGB(2, 8, -14396368);
		_fileIcon.setRGB(2, 9, -14991282);
		_fileIcon.setRGB(2, 10, -14195814);
		_fileIcon.setRGB(2, 11, -13865578);
		_fileIcon.setRGB(2, 12, -1315861);
		_fileIcon.setRGB(2, 13, -6710887);
		_fileIcon.setRGB(3, 0, -5066062);
		_fileIcon.setRGB(3, 1, -1);
		_fileIcon.setRGB(3, 2, -13400360);
		_fileIcon.setRGB(3, 3, -9191169);
		_fileIcon.setRGB(3, 4, -1966081);
		_fileIcon.setRGB(3, 5, -2949121);
		_fileIcon.setRGB(3, 6, -9133420);
		_fileIcon.setRGB(3, 7, -11960231);
		_fileIcon.setRGB(3, 8, -14723027);
		_fileIcon.setRGB(3, 9, -14199450);
		_fileIcon.setRGB(3, 10, -13073471);
		_fileIcon.setRGB(3, 11, -13208163);
		_fileIcon.setRGB(3, 12, -1315861);
		_fileIcon.setRGB(3, 13, -6710887);
		_fileIcon.setRGB(4, 0, -5066062);
		_fileIcon.setRGB(4, 1, -1);
		_fileIcon.setRGB(4, 2, -13203497);
		_fileIcon.setRGB(4, 3, -10373889);
		_fileIcon.setRGB(4, 4, -1966081);
		_fileIcon.setRGB(4, 5, -1114113);
		_fileIcon.setRGB(4, 6, -2228225);
		_fileIcon.setRGB(4, 7, -9853822);
		_fileIcon.setRGB(4, 8, -14326993);
		_fileIcon.setRGB(4, 9, -14200990);
		_fileIcon.setRGB(4, 10, -12482110);
		_fileIcon.setRGB(4, 11, -12551520);
		_fileIcon.setRGB(4, 12, -1315861);
		_fileIcon.setRGB(4, 13, -6710887);
		_fileIcon.setRGB(5, 0, -5066062);
		_fileIcon.setRGB(5, 1, -1);
		_fileIcon.setRGB(5, 2, -13071913);
		_fileIcon.setRGB(5, 3, -10834433);
		_fileIcon.setRGB(5, 4, -5119745);
		_fileIcon.setRGB(5, 5, -1);
		_fileIcon.setRGB(5, 6, -1376257);
		_fileIcon.setRGB(5, 7, -7225915);
		_fileIcon.setRGB(5, 8, -12615595);
		_fileIcon.setRGB(5, 9, -13605780);
		_fileIcon.setRGB(5, 10, -11036975);
		_fileIcon.setRGB(5, 11, -12026206);
		_fileIcon.setRGB(5, 12, -1315861);
		_fileIcon.setRGB(5, 13, -6710887);
		_fileIcon.setRGB(6, 0, -5066062);
		_fileIcon.setRGB(6, 1, -1);
		_fileIcon.setRGB(6, 2, -12809001);
		_fileIcon.setRGB(6, 3, -9323009);
		_fileIcon.setRGB(6, 4, -4923393);
		_fileIcon.setRGB(6, 5, -2884609);
		_fileIcon.setRGB(6, 6, -2621441);
		_fileIcon.setRGB(6, 7, -1771009);
		_fileIcon.setRGB(6, 8, -11827103);
		_fileIcon.setRGB(6, 9, -13603728);
		_fileIcon.setRGB(6, 10, -10314538);
		_fileIcon.setRGB(6, 11, -11894622);
		_fileIcon.setRGB(6, 12, -1315861);
		_fileIcon.setRGB(6, 13, -6710887);
		_fileIcon.setRGB(7, 0, -5066062);
		_fileIcon.setRGB(7, 1, -1);
		_fileIcon.setRGB(7, 2, -13466153);
		_fileIcon.setRGB(7, 3, -10242561);
		_fileIcon.setRGB(7, 4, -6500609);
		_fileIcon.setRGB(7, 5, -3345153);
		_fileIcon.setRGB(7, 6, -2818049);
		_fileIcon.setRGB(7, 7, -1);
		_fileIcon.setRGB(7, 8, -9917309);
		_fileIcon.setRGB(7, 9, -13800084);
		_fileIcon.setRGB(7, 10, -9460518);
		_fileIcon.setRGB(7, 11, -11303771);
		_fileIcon.setRGB(7, 12, -1315861);
		_fileIcon.setRGB(7, 13, -6710887);
		_fileIcon.setRGB(8, 0, -5066062);
		_fileIcon.setRGB(8, 1, -1);
		_fileIcon.setRGB(8, 2, -12810281);
		_fileIcon.setRGB(8, 3, -6826241);
		_fileIcon.setRGB(8, 4, -7352065);
		_fileIcon.setRGB(8, 5, -4001281);
		_fileIcon.setRGB(8, 6, -1);
		_fileIcon.setRGB(8, 7, -1048577);
		_fileIcon.setRGB(8, 8, -11164284);
		_fileIcon.setRGB(8, 9, -12484492);
		_fileIcon.setRGB(8, 10, -9066018);
		_fileIcon.setRGB(8, 11, -11370077);
		_fileIcon.setRGB(8, 12, -1315861);
		_fileIcon.setRGB(8, 13, -6710887);
		_fileIcon.setRGB(9, 0, -5066062);
		_fileIcon.setRGB(9, 1, -1);
		_fileIcon.setRGB(9, 2, -9260841);
		_fileIcon.setRGB(9, 3, -3933185);
		_fileIcon.setRGB(9, 4, -4591617);
		_fileIcon.setRGB(9, 5, -3868929);
		_fileIcon.setRGB(9, 6, -1114113);
		_fileIcon.setRGB(9, 7, -1310721);
		_fileIcon.setRGB(9, 8, -9391954);
		_fileIcon.setRGB(9, 9, -12945309);
		_fileIcon.setRGB(9, 10, -8473882);
		_fileIcon.setRGB(9, 11, -11304030);
		_fileIcon.setRGB(9, 12, -1315861);
		_fileIcon.setRGB(9, 13, -6710887);
		_fileIcon.setRGB(10, 0, -5066062);
		_fileIcon.setRGB(10, 1, -1);
		_fileIcon.setRGB(10, 2, -6436137);
		_fileIcon.setRGB(10, 3, -1048577);
		_fileIcon.setRGB(10, 4, -1966849);
		_fileIcon.setRGB(10, 5, -2753025);
		_fileIcon.setRGB(10, 6, -2424833);
		_fileIcon.setRGB(10, 7, -1245185);
		_fileIcon.setRGB(10, 8, -9654324);
		_fileIcon.setRGB(10, 9, -11826321);
		_fileIcon.setRGB(10, 10, -7554068);
		_fileIcon.setRGB(10, 11, -9727573);
		_fileIcon.setRGB(10, 12, -1315861);
		_fileIcon.setRGB(10, 13, -6710887);
		_fileIcon.setRGB(11, 0, -5066062);
		_fileIcon.setRGB(11, 1, -1);
		_fileIcon.setRGB(11, 2, -7814953);
		_fileIcon.setRGB(11, 3, -1507329);
		_fileIcon.setRGB(11, 4, -2688769);
		_fileIcon.setRGB(11, 5, -2622209);
		_fileIcon.setRGB(11, 6, -2424833);
		_fileIcon.setRGB(11, 7, -1);
		_fileIcon.setRGB(11, 8, -8208451);
		_fileIcon.setRGB(11, 9, -12152460);
		_fileIcon.setRGB(11, 10, -5189899);
		_fileIcon.setRGB(11, 11, -8479564);
		_fileIcon.setRGB(11, 12, -1315861);
		_fileIcon.setRGB(11, 13, -6710887);
		_fileIcon.setRGB(12, 0, -5066062);
		_fileIcon.setRGB(12, 1, -1);
		_fileIcon.setRGB(12, 2, -6566696);
		_fileIcon.setRGB(12, 3, -8988673);
		_fileIcon.setRGB(12, 4, -3869953);
		_fileIcon.setRGB(12, 5, -196609);
		_fileIcon.setRGB(12, 6, -2490369);
		_fileIcon.setRGB(12, 7, -1);
		_fileIcon.setRGB(12, 8, -2889492);
		_fileIcon.setRGB(12, 9, -11229550);
		_fileIcon.setRGB(12, 10, -3810049);
		_fileIcon.setRGB(12, 11, -8676171);
		_fileIcon.setRGB(12, 12, -1315861);
		_fileIcon.setRGB(12, 13, -6710887);
		_fileIcon.setRGB(13, 0, -5066062);
		_fileIcon.setRGB(13, 1, -1);
		_fileIcon.setRGB(13, 2, -6502705);
		_fileIcon.setRGB(13, 3, -7814695);
		_fileIcon.setRGB(13, 4, -8275497);
		_fileIcon.setRGB(13, 5, -4073257);
		_fileIcon.setRGB(13, 6, -4073258);
		_fileIcon.setRGB(13, 7, -2958884);
		_fileIcon.setRGB(13, 8, -2039847);
		_fileIcon.setRGB(13, 9, -9330035);
		_fileIcon.setRGB(13, 10, -4471592);
		_fileIcon.setRGB(13, 11, -9202260);
		_fileIcon.setRGB(13, 12, -1315861);
		_fileIcon.setRGB(13, 13, -6710887);
		_fileIcon.setRGB(14, 0, -5066062);
		_fileIcon.setRGB(14, 1, -1);
		_fileIcon.setRGB(14, 2, -1);
		_fileIcon.setRGB(14, 3, -65794);
		_fileIcon.setRGB(14, 4, -197380);
		_fileIcon.setRGB(14, 5, -328966);
		_fileIcon.setRGB(14, 6, -460552);
		_fileIcon.setRGB(14, 7, -592138);
		_fileIcon.setRGB(14, 8, -789517);
		_fileIcon.setRGB(14, 9, -921103);
		_fileIcon.setRGB(14, 10, -1052689);
		_fileIcon.setRGB(14, 11, -1184275);
		_fileIcon.setRGB(14, 12, -1315861);
		_fileIcon.setRGB(14, 13, -6710887);
		_fileIcon.setRGB(15, 0, -5066062);
		_fileIcon.setRGB(15, 1, -5066062);
		_fileIcon.setRGB(15, 2, -5066062);
		_fileIcon.setRGB(15, 3, -5131855);
		_fileIcon.setRGB(15, 4, -5263441);
		_fileIcon.setRGB(15, 5, -5460820);
		_fileIcon.setRGB(15, 6, -5592406);
		_fileIcon.setRGB(15, 7, -5789785);
		_fileIcon.setRGB(15, 8, -5987164);
		_fileIcon.setRGB(15, 9, -6184543);
		_fileIcon.setRGB(15, 10, -6316129);
		_fileIcon.setRGB(15, 11, -6513508);
		_fileIcon.setRGB(15, 12, -6645094);
		_fileIcon.setRGB(15, 13, -6710887);
	}
	
	private static class RGBGrayFilter extends RGBImageFilter {

		private RGBGrayFilter() {
			canFilterIndexColorModel = true;
		}

		public static Image createDisabledImage(Image i) {
			RGBGrayFilter filter = new RGBGrayFilter();
			ImageProducer prod = new FilteredImageSource(i.getSource(), filter);
			Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
			return grayImage;
		}

		public int filterRGB(int x, int y, int rgb) {
			// Find the average of red, green, and blue.
			float avg = (((rgb >> 16) & 0xff) / 255f +
						((rgb >>  8) & 0xff) / 255f +
						(rgb        & 0xff) / 255f) / 3;
			// Pull out the alpha channel.
			float alpha = (((rgb >> 24) & 0xff) / 255f);

			// Calculate the average.
			// Sun's formula: Math.min(1.0f, (1f - avg) / (100.0f / 35.0f) + avg);
			// The following formula uses less operations and hence is faster.
			avg = Math.min(1.0f, 0.35f + 0.65f * avg);
			// Convert back into RGB.
			return (int) (alpha * 255f) << 24 |
					(int) (avg   * 255f) << 16 |
					(int) (avg   * 255f) << 8  |
					(int) (avg   * 255f);
		}
	}	
	
	//-------------------------------------------------------------------------
	
	public static void read() {
		try  {
			BufferedImage img = ImageIO.read(new File("c:\\test\\icon.png"));
			
			for (int i = 0; i < img.getWidth(); ++i) {
				for (int j = 0; j < img.getHeight(); ++j) {
					System.out.println("_bi.setRGB("+ i + ", " + j + ", " + img.getRGB(i, j) + ");");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

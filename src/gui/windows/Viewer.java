package gui.windows;

import gui.icons.Icon;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import utils.ImageUtils;
import utils.ReadableUtils;

public class Viewer implements KeyListener {

	private JFrame _jframe;
	private ViewerPanel	_viewerPanel;
	private PercentPanel _percentPanel;
	private File _tmp;
	
	private int			_jframePreviousState;
	private Point		_jframePreviousLocationOnScreen;
	private Dimension	_jframePreviousSize;
	
	private boolean _jpegLoading = false;
	
	public Viewer(JFrame parent) {
		try {
			_tmp = File.createTempFile("JPEGOptimizerViewer", ".jpg");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		_viewerPanel = new ViewerPanel();
		_percentPanel = new PercentPanel();
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(_viewerPanel, BorderLayout.CENTER);
		contentPanel.add(_percentPanel, BorderLayout.EAST);
		
		_jframe = new JFrame("JPEG Optimizer - Viewer");
		_jframe.setSize(new Dimension(800, 600));
		_jframe.setLocationRelativeTo(parent);
		_jframe.addKeyListener(this);
		_jframe.setContentPane(contentPanel);
		_jframe.setIconImage(Icon.getInstance().getFrameIcon());
	}
	
	//Display pictures without displaying quality
	public void setPictures(File src, File dst) throws IOException {
		_jframe.setTitle("JPEG Optimizer - " + src.getAbsolutePath());
		_jframe.setVisible(true);
		_viewerPanel.setPictures(null, null);
		loadPicturesInThread(src, dst);
	}
	
	//Compute new dst picure from src picture, by compressing it with the given quality
	synchronized public boolean changeDstQuality(int quality) {
		if ((quality == _percentPanel.getSelectedQuality()) || (quality < 0) || (quality > 100) || (_jpegLoading == true)) {
			return false ;
		}
		try {
			_viewerPanel.setDstPicture(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadJPEGinThread(quality);
		return true;
	}
	
	private void loadPicturesInThread(final File src, final File dst) {
		_jpegLoading = true;
		_percentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		_percentPanel.setSelectedQuality(-1, false);
		
		new Thread() {
			
			@Override
		    public void run() {
		    	try {
					_viewerPanel.setPictures(src, dst);
					_percentPanel.setSelectedQuality(ImageUtils.readQualityInJPEG(dst), true);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					_jpegLoading = false;
					_percentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
		    }
		}.start();					
	}
	
	private void loadJPEGinThread(final int quality) {
		_jpegLoading = true;
		_percentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new Thread() {
			
			@Override
		    public void run() {
		    	try {
		    		_viewerPanel.setDstStateStr("Creating JPEG File...");
					ImageUtils.createJPEG(_viewerPanel.getSrcPicture(), _tmp, quality);
					_viewerPanel.setDstPicture(_tmp);
					_tmp.delete();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					_jpegLoading = false;
					_percentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
		    }
		}.start();
	}
	
	private void switchFullScreen() {
		if (_jframe.isUndecorated()) { //go back from full screen to windowed
			_jframe.dispose();
			_jframe.setUndecorated(false);
			_jframe.setResizable(true);
			
			//restore state
			_jframe.setExtendedState(_jframePreviousState);
			_jframe.setSize(_jframePreviousSize);
			_jframe.setLocation(_jframePreviousLocationOnScreen);
			
			_jframe.setVisible(true);
		} else { //go to full screen mode
			//Save frame position and size
			_jframePreviousState = _jframe.getExtendedState();
			_jframePreviousSize = _jframe.getSize();
			_jframePreviousLocationOnScreen = _jframe.getLocationOnScreen();
			//go to fullscreen
			_jframe.dispose();
			_jframe.setUndecorated(true);
			_jframe.setResizable(false);
			_jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
			_jframe.setVisible(true);
		}
	}
	

	@Override
	public void keyPressed(KeyEvent evt) {}

	@Override
	public void keyTyped(KeyEvent evt) {}

	@Override
	public void keyReleased(KeyEvent evt) {
		int kchar = evt.getKeyChar();
		
		if ((kchar == 'f') || (kchar == 'F'))  {
			switchFullScreen();
		} else if ((kchar == 'm') || (kchar == 'M'))  {
			_viewerPanel.switchCompareMode();
		}
	}
	
	private class PercentPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {

		private static final long serialVersionUID = 1L;
		private Font _font;
		private int _offsetY = 0;
		private int _selectedValue = -1;
		private int _hoverValue = -1;
		
		public PercentPanel() {
			_font = new JLabel("Test").getFont();
			
			int fwidth = getFontMetrics(_font).stringWidth("100%");
			
			this.setPreferredSize(new Dimension(fwidth+5, 50));
			this.setSize(new Dimension(fwidth+5, 50));
			this.addMouseWheelListener(this);
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		public int getSelectedQuality() {
			return _selectedValue;
		}
		
		public void setSelectedQuality(int quality, boolean ensureQualityIsVisible) {
			if (_selectedValue != quality) {
				_selectedValue = quality;
				if (ensureQualityIsVisible) {
					while (getPositionAt(quality) < (getHeight() / 2)) {
						if (scrollUp() == false) {
							break ;
						}
					}
					while (getPositionAt(quality) > (getHeight() / 2)) {
						if (scrollDown() == false) {
							break ;
						}
					}
				}
				this.repaint();
			}
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			int width = (int)this.getSize().getWidth();
			int height = (int)this.getSize().getHeight();
			
			Graphics2D g2d = (Graphics2D)g;

			g2d.setFont(_font);
			FontMetrics metrics = g2d.getFontMetrics();
			int fheight = metrics.getHeight();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);			
			
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);			
			
			int y = fheight + _offsetY;
			
			for (int i = 100; i >= 0; --i) {
				if (y > 0) {
					Color c = Color.DARK_GRAY;
					if (i == _selectedValue) {
						c = Color.WHITE;
					} else if (i == _hoverValue) {
						c = Color.GRAY;
					}
					g2d.setColor(c);
					String str = i + "%";
					int fwidth = metrics.stringWidth(str);
					g2d.drawString(str, width - fwidth, y);
				}
				y+= fheight;
				if (y > height) {
					break ;
				}
			}

			g.dispose();
		}

		private int getQualityAt(int y) {
			int fheight = getFontMetrics(_font).getHeight();
			return 100 - ((y -_offsetY) / fheight);
		}
		
		private int getPositionAt(int quality) {
			int fheight = getFontMetrics(_font).getHeight();
			return ((100 - quality) * fheight) + _offsetY;
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			boolean needRepaint = false;
			if (e.getWheelRotation() < 0) { //scroll up
				needRepaint = scrollUp();
			} else {
				needRepaint = scrollDown();
			}
			if (needRepaint) {
				this.repaint();
			}
		}

		private boolean scrollUp() {
			int fheight = getFontMetrics(_font).getHeight();
			if ((fheight + _offsetY) > 0) {
				return false;
			}
			_offsetY -= (fheight * -3);
			return true;
		}
		
		private boolean scrollDown() {
			int fheight = getFontMetrics(_font).getHeight();
			if (getQualityAt(getHeight() - fheight) <= 0) {
				return false;
			}
			_offsetY -= (fheight * +3);
			return true;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			int quality = getQualityAt(e.getPoint().y);
			boolean willChange = changeDstQuality(quality);
			if (willChange) {
				this.setSelectedQuality(quality, false); //false as we already sur that quality is visible (else user cannot click on it)
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			_hoverValue = getQualityAt(e.getPoint().y);
			this.repaint();
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {
			_hoverValue = -1;
			this.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseDragged(MouseEvent e) {}
		
	}
	
	private class ViewerPanel extends JPanel implements MouseListener, MouseMotionListener {
		
		private static final long serialVersionUID = 1L;

		public static final int COMPARE_MODE_SPLIT = 0;
		public static final int COMPARE_MODE_SIDE = 1;
		
		private Font			_font;
		
		private BufferedImage 	_srcImg;
		private BufferedImage 	_dstImg;
		private String			_srcSize;
		private String			_dstSize;
		private String			_visualDiff;
		
		private int				_moveX;
		private int				_moveY;
		private int				_compareMode;
		
		private int				_x;
		private	int				_y;

		private String			_srcStateStr;
		private String			_dstStateStr;
		
		public ViewerPanel() {
			addMouseListener(this);
			addMouseMotionListener(this);
			_compareMode = COMPARE_MODE_SPLIT;
			_font = new JLabel("Test").getFont();
			
			_srcSize = "";
			_dstSize = "";
			_visualDiff = "";
			_srcStateStr = "";
			_dstStateStr = "";
		}
		
		public void setPictures(File src, File dst) throws IOException {
			setSrcPicture(src);
			setDstPicture(dst);
			reinit();
		}
		
		public BufferedImage getSrcPicture() {
			return _srcImg;
		}
		
		public void setDstStateStr(String stateStr) {
			if (stateStr == null) {
				stateStr = "";
			}
			_dstStateStr = stateStr;
			repaint();
		}
		
		private void setSrcPicture(File src) throws IOException {
			if ((src != null) && (src.exists() == true)) {
				_srcImg = null;
				_srcSize = "";
				_srcStateStr = "Loading...";
				repaint();
				_srcImg = ImageIO.read(src);
				_srcSize = ReadableUtils.fileSize(src.length());
			} else if ((src != null) && (src.exists() == false)) {
				_srcImg = null;
				_srcSize = "";
				_srcStateStr = "File does not exist!";
			} else {
				_srcImg = null;
				_srcSize = "";
				_srcStateStr = "";
			}
			repaint();
		}
		
		public void setDstPicture(File dst) throws IOException {
			if ((dst != null) && (dst.exists() == true)) {
				_dstImg = null;
				_dstSize = ReadableUtils.fileSize(dst.length());
				_visualDiff = "loading jpeg...";
				_dstStateStr = "Loading...";
				repaint();
				_dstImg = ImageIO.read(dst);
				_visualDiff = "computing...";
				repaint();
				_visualDiff = ReadableUtils.rate(ImageUtils.computeSimilarityRGB(_srcImg, _dstImg));
			} else if ((dst != null) && (dst.exists() == false)) {
				_dstImg = null;
				_dstSize = "";
				_visualDiff = "";
				_dstStateStr = "File does not exist!";
			} else {
				_dstImg = null;
				_dstSize = "";
				_visualDiff = "";
				_dstStateStr = "";
			}
			repaint();
		}
		
		private void reinit() {
			_moveX = 0;
			_moveY = 0;
			repaint();
		}
		
		public void movePhoto(int deltaX, int deltaY) {
			_moveX += deltaX;
			_moveY += deltaY;
			repaint();
		}
		
		public void switchCompareMode() {
			if (_compareMode == COMPARE_MODE_SPLIT) {
				_compareMode = COMPARE_MODE_SIDE;
			} else {
				_compareMode = COMPARE_MODE_SPLIT;
			}
			repaint();
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			int width = (int)this.getSize().getWidth();
			int height = (int)this.getSize().getHeight();
			int w2 = width/2;
			
			Graphics2D g2d = (Graphics2D)g;

			g2d.setFont(_font);
			FontMetrics metrics = g2d.getFontMetrics();
			int fheight = metrics.getHeight();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);			
			
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);
			
			Shape clip = g2d.getClip();
			
			if (_srcImg != null) {
				g2d.clipRect(0, 0, w2, height);
				AffineTransform at = AffineTransform.getTranslateInstance(_moveX, _moveY);
				g2d.drawRenderedImage(_srcImg, at);
				g2d.setClip(clip);
			} else {
				g2d.setColor(Color.WHITE);
				String text = _srcStateStr;//"Loading";
			    int fwidth = metrics.stringWidth(text);
				g2d.drawString(text, (w2-fwidth)/2, (height-fheight)/2);		
			}
			
			if (_dstImg != null) {
				g2d.clipRect(w2, 0, w2, height);
				
				AffineTransform at;
				if (_compareMode == COMPARE_MODE_SPLIT) {
					at = AffineTransform.getTranslateInstance(_moveX, _moveY);
				} else {
					at = AffineTransform.getTranslateInstance(_moveX+w2, _moveY);
				}
				
				g2d.drawRenderedImage(_dstImg, at);
				g2d.setClip(clip);
			} else {
				g2d.setColor(Color.WHITE);
				String text = _dstStateStr;//"Loading";
			    int fwidth = metrics.stringWidth(text);
				g2d.drawString(text, w2 + (w2-fwidth)/2, (height-fheight)/2);		
			}
			
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
			g2d.setComposite(ac);
			
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, fheight);
			
			g2d.setColor(Color.WHITE);
			g2d.drawLine(w2, 0, w2, height);
			g2d.setColor(Color.GRAY);
			g2d.drawLine(w2+1, 0, w2+1, height);
			g2d.drawLine(w2-1, 0, w2-1, height);

			
			ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
			g2d.setComposite(ac);
			g2d.setColor(Color.WHITE);
			g2d.drawString("F: swap fullscreen mode / M: swap display mode", 0 + 5, metrics.getAscent());
			g2d.drawString(_srcSize, w2 - metrics.stringWidth(_srcSize) - 5, metrics.getAscent());
			g2d.drawString(_dstSize, width - metrics.stringWidth(_dstSize) - 5, metrics.getAscent());
			
			g2d.drawString("Visual diff : " + _visualDiff, w2+5, metrics.getAscent());
			
			g2d.dispose();			
			
		}

		@Override
		public void mousePressed(MouseEvent evt) {
			if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
    			_x = evt.getX();
    			_y = evt.getY();
			}
		}

		@Override
		public void mouseDragged(MouseEvent evt) {
			if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
    			int x = evt.getX();
    			int y = evt.getY();
    			int deltaX = x - _x;
    			int deltaY = y - _y;
           		_x = x;
           		_y = y;
           		movePhoto(deltaX, deltaY);
            }
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent evt) {}

		@Override
		public void mouseExited(MouseEvent evt) {}

		@Override
		public void mouseReleased(MouseEvent evt) {}
	}


	
}

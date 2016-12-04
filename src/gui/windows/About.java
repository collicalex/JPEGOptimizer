package gui.windows;

import gui.icons.Logo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


public class About extends JDialog {

	private static final long serialVersionUID = 1L;
	private String _version = "v1.0 - 2016/12/04";

	public About(Frame frame) {
		super(frame, "About", true);
		
		JPanel contentPanel = new JPanel();

		contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints cLogo = new GridBagConstraints();
		cLogo.gridx = 0;
		cLogo.gridy = 0;
		cLogo.gridheight = 5;
		cLogo.insets = new Insets(0,0,0,25);
		contentPanel.add(new LogoPanel(), cLogo);
		
		
		GridBagConstraints cTitle = new GridBagConstraints();
		cTitle.gridx = 1;
		cTitle.gridy = 0;
		cTitle.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getTitleLabel(), cTitle);

		GridBagConstraints cVersion = new GridBagConstraints();
		cVersion.gridx = 1;
		cVersion.gridy = 1;
		cVersion.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getVersionLabel(), cVersion);
		
		GridBagConstraints cGitHub = new GridBagConstraints();
		cGitHub.gridx = 1;
		cGitHub.gridy = 2;
		cGitHub.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getWebLabel("Check source and latest release","https://github.com/collicalex/JPEGOptimizer/releases"), cGitHub);
		
		GridBagConstraints cAuthor = new GridBagConstraints();
		cAuthor.gridx = 1;
		cAuthor.gridy = 3;
		cAuthor.fill = GridBagConstraints.HORIZONTAL;
		cAuthor.insets = new Insets(20,0,0,0);
		contentPanel.add(getAuthorLabel(), cAuthor);
		
		GridBagConstraints cFlickr = new GridBagConstraints();
		cFlickr.gridx = 1;
		cFlickr.gridy = 4;
		cFlickr.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getWebLabel("Follow me on Flickr (Colliculus)","http://www.flickr.com/photos/colliculus"), cFlickr);

		
		this.setContentPane(contentPanel);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(frame);
	}
	
	private JLabel getTitleLabel() {
		JLabel title = new JLabel("JPEG Optimizer");
		title.setFont(title.getFont().deriveFont(24.0f));
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		return title;
	}
	
	private JLabel getVersionLabel() {
		JLabel version = new JLabel(_version);
		return version;
	}
	
	private JLabel getAuthorLabel() {
		JLabel author = new JLabel("Created by : Alexandre Bargeton");
		return author;
	}
	
	private JLabel getWebLabel(String caption, final String url) {
		JLabel flickr = new JLabel(caption);
		flickr.setCursor(new Cursor(Cursor.HAND_CURSOR));
		flickr.setForeground(Color.GRAY);
		flickr.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.GRAY);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.BLUE);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (IOException ee) { 
					} catch (URISyntaxException e1) {
					}
				}
			}
		});
		return flickr;
	}
	
	private class LogoPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		private int _logoWidth;
		private int _logoHeight;
		
		public LogoPanel() {
			_logoWidth = Logo.getInstance().getLogo().getWidth(null) + 15 + 15;
			_logoHeight = Logo.getInstance().getLogo().getHeight(null) + 15 + 15;
			int border = 0;
			Dimension dimension = new Dimension(_logoWidth+border,_logoHeight+border);
			this.setSize(dimension);
			this.setPreferredSize(dimension);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			
			//background
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			
			//logo
			int offsetX = (getWidth() - _logoWidth) / 2;
			int offsetY = (getHeight() - _logoHeight) / 2;
			paintLogo_Part(g2d, offsetX, offsetY);
			paintLogo_Part(g2d, offsetX+15, offsetY+15);
			
		}

		private void paintLogo_Part(Graphics2D g2d, int x, int y) {
			Image img = Logo.getInstance().getLogo();
			int w = img.getWidth(null);
			int h = img.getHeight(null);
			
			g2d.setColor(Color.WHITE);
			g2d.fillRect(x, y, w+15, h+15);
			
			g2d.setColor(new Color(237,237,237));
			g2d.drawRect(x+0, y+0, w+15, h+15);
			
			g2d.setColor(new Color(215,215,215));
			g2d.drawRect(x+1, y+1, w+13, h+13);
			
			g2d.setColor(new Color(182,182,182));
			g2d.drawRect(x+2, y+2, w+11, h+11);
			
			g2d.drawImage(img, x+8, y+8, null);
		}
		
	}
	
}

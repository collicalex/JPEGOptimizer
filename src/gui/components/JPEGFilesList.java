package gui.components;

import gui.icons.Icon;
import gui.windows.Gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import utils.ReadableUtils;
import core.JPEGFiles;

public class JPEGFilesList extends JList<JPEGFiles> implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;
	private Gui _gui;
	private int _hoverViewerButtonIndex = -1;
	private DefaultListModel<JPEGFiles> _listModel;


	public JPEGFilesList(Gui gui) {
		_gui = gui;
		_listModel = new DefaultListModel<JPEGFiles>();
		this.setModel(_listModel);
		this.setCellRenderer(new JPEGFilesListCellRenderer());
		this.addMouseListener(this);
        this.addMouseMotionListener(this);		
	}
	
	public void listSrcFiles(File src) {
		if (src != null) {
			File files[] = src.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					String lowercaseName = file.getName().toLowerCase();
					if ((lowercaseName.endsWith(".jpg")) || (lowercaseName.endsWith(".jpeg"))) {
						return true;
					}
					return false;
				}
			});
	
			_listModel.removeAllElements();
			for (File file : files) {
				JPEGFiles jpegFiles = new JPEGFiles(file);
				jpegFiles.setLoger(_gui.getLoger());
				jpegFiles.setListener(_gui);
				_listModel.addElement(jpegFiles);
			}
		}
	}
	
	public void updateListDstDir(File dst) {
		if (dst != null) {
			for (int i = 0; i < _listModel.getSize(); ++i) {
				JPEGFiles jpegFile = _listModel.getElementAt(i);
				if (jpegFile.getSrc() != null) {
					jpegFile.setDst(new File(dst, jpegFile.getSrc().getName()));
				}
			}
			this.repaint();
		}
	}	
	
	private void setHoverViewerButton(int index) {
		if (index != _hoverViewerButtonIndex) {
			_hoverViewerButtonIndex = index;
			this.repaint();
		}
	}
	
	private int getHoverViewerButtonIndex(Point p) {
		if (p.x > (this.getWidth() - 26) && (p.x < this.getWidth() - 4)) {
			int index = this.locationToIndex(p);
			if (index > -1) {
				Rectangle bounds = this.getCellBounds(index, index);
				int offsetY = (bounds.height - 24) / 2;
				if ((p.y > (bounds.y+offsetY)) && (p.y < (bounds.y+bounds.height-offsetY))) {
					return index;
				}
			}
		}
		return -1;
	}
	
	@Override
	public void mouseClicked(MouseEvent evt) {
		if (getHoverViewerButtonIndex(evt.getPoint()) != -1) {
			_gui.displayJPEGFiles((JPEGFiles) this.getSelectedValue());
		}
	}

	@Override
	public void mouseMoved(MouseEvent evt) {
		setHoverViewerButton(getHoverViewerButtonIndex(evt.getPoint()));
	}

	@Override
	public void mouseDragged(MouseEvent evt) {}
	
	@Override
	public void mouseEntered(MouseEvent evt) {}

	@Override
	public void mouseExited(MouseEvent evt) {}

	@Override
	public void mousePressed(MouseEvent evt) {}

	@Override
	public void mouseReleased(MouseEvent evt) {}
	
	
	private class JPEGFilesListCellRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = 1L;
		private Border _selectedBorder;
		private Border _notSelectedBorder;

	    public JPEGFilesListCellRenderer() {
	    	UIDefaults defaults = javax.swing.UIManager.getDefaults();
	    	
	        Border padBorder = new MatteBorder(3,3,3,3, Color.WHITE);
	        Border selectedBorder = new MatteBorder(1, 3, 1, 1, defaults.getColor("List.selectionBackground"));
	        Border notSelectedBorder = new MatteBorder(1, 3, 1, 1, Color.WHITE);
	        
	        _selectedBorder = new CompoundBorder(selectedBorder, padBorder);
	        _notSelectedBorder = new CompoundBorder(notSelectedBorder, padBorder);
	    	
	    }
	    
	    private String getSrcFileStr(JPEGFiles jpgfile) {
	    	File file = jpgfile.getSrc();
	    	if (file != null) {
	    		if (file.exists()) {
	    			return file.getAbsolutePath() + " (" + ReadableUtils.fileSize(jpgfile.getOriginalSrcSize()) + ")";
	    		} else {
	    			return file.getAbsolutePath() + " (???)";
	    		}
	    	}
	    	return "";
	    }
	    
	    private String getDstFileStr(JPEGFiles jpgfile) {
	    	File file = jpgfile.getDst();
	    	if (file != null) {
	    		if (file.exists()) {
	    			return file.getAbsolutePath() + " (" + ReadableUtils.fileSize(file.length()) + ")";
	    		} else {
	    			return file.getAbsolutePath() + " (???)";
	    		}
	    	}
	    	return "";
	    }    
	    
	    private String getEarnStr(JPEGFiles jpgFiles) {
	    	if (jpgFiles.getState() == JPEGFiles.NOT_YET_OPTIMIZED) {
	    		return "<font color='gray'><i>Not yet optimized.</i></font>";
	    	} else if (jpgFiles.getState() == JPEGFiles.OPTIMIZING) {
	    		return "<font color='#FF6600'>Optimizing in progress... ("+jpgFiles.getCurrentOptimStep() + "/" + jpgFiles.getMaxOptimStep() + ")</font>";
	    	} else if (jpgFiles.getState() == JPEGFiles.OPTIMIZED_KO) { 
	    		return "<font color='red'>Unable to optimize file (too many visual difference when compressing). Failed in " + ReadableUtils.interval(jpgFiles.getElaspedTime()) + "</font>";
	    	} else if (jpgFiles.getState() == JPEGFiles.OPTIMIZED_OK) {
	    		if (jpgFiles.getEarnRate() != null) {
	    			return "<font color='green'>Optimization done. Earn " + ReadableUtils.fileSize(jpgFiles.getEarnSize()) + " (" + ReadableUtils.rate(jpgFiles.getEarnRate()) + ") in " + ReadableUtils.interval(jpgFiles.getElaspedTime()) + ". Jpeg compression quality found is <b>" + jpgFiles.getJpegQualityFound() + "%</b>.</font>";
	    		}
	    	} else if (jpgFiles.getState() == JPEGFiles.OPTIMIZED_UNNECESSARY) {
	    		return "<font color='green'>Optimization unnecessary (src file already too small).</font>";
	    	} else if (jpgFiles.getState() == JPEGFiles.OPTIMIZED_OVERWRITE_NOT_ALLOWED) {
	    		return "<font color='red'>Destination file already exists and overwrite is not allowed.</font>";    		
	    	}
			return "UNKNOWN STATE";
	    }
	    
	    
	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    	Component c = super.getListCellRendererComponent(list, value, index, /*isSelected*/ false, cellHasFocus);
	    	JLabel l = (JLabel)c;
	        JPEGFiles jpegFiles = (JPEGFiles)value;
	        
	        l.setText("<html>src : " + getSrcFileStr(jpegFiles) + "<BR>dst : " + getDstFileStr(jpegFiles) + "<BR>"+ getEarnStr(jpegFiles) +"</html>");
	        //l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(jpegFiles.getSrc()));
	        l.setIcon(Icon.getInstance().getFileImageIcon());
	        l.setBorder(null);

	        
	        JLabel diffLabel = new JLabel();
	        if (index == _hoverViewerButtonIndex) {
	        	diffLabel.setIcon(Icon.getInstance().getDiffIcon());
	        } else {
	        	diffLabel.setIcon(Icon.getInstance().getDiffIconGray());
	        }
	        
	        JPanel panel = new JPanel(new BorderLayout());
	        panel.setBackground(Color.WHITE);
	        panel.add(l, BorderLayout.CENTER);
	        panel.add(diffLabel, BorderLayout.EAST);
	        if (isSelected) {
	        	panel.setBorder(_selectedBorder);
	        } else {
	        	panel.setBorder(_notSelectedBorder);
	        }
	        
	        return panel;
	    }
	}




}

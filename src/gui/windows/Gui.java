package gui.windows;

import gui.components.JDirectoryChooser;
import gui.components.JDirectoryChooserListener;
import gui.components.JLoger;
import gui.components.JPEGFilesList;
import gui.icons.Icon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import core.JPEGFiles;
import core.JPEGFilesListener;
import core.Loger;
import utils.GuiUtils;
import utils.ReadableUtils;


public class Gui implements JDirectoryChooserListener, JPEGFilesListener {

	private JDirectoryChooser _srcDir;
	private JDirectoryChooser _dstDir;
	private JComboBox<Double> _minSize;
	private JComboBox<Boolean> _overwrite;
	
	//---------------------------------------------------------
	
	private JButton _optimizeButton;
	private JProgressBar _jprogressBar;
	private JLoger _loger;
	
	//---------------------------------------------------------
	
	private JPEGFilesList _jList;
	
	private JFrame 	_jFrame;
	private Viewer	_viewer;
	
	public Gui() {
		GuiUtils.setSystemLookAndFeel();
		
		//---------------------------------------------------------

		_srcDir = new JDirectoryChooser("Source:", 75);
		_dstDir = new JDirectoryChooser("Destination:", 75);
		_srcDir.addDirectoryChooserListner(this);
		_dstDir.addDirectoryChooserListner(this);
		
		//---------------------------------------------------------
		
		JLabel minLabel = new JLabel(" Min file size: ");
		minLabel.setPreferredSize(new Dimension(75, 1));
		_minSize = new JComboBox<Double>();
		for (int i = 0; i <= 100*10; i+=5) {
			_minSize.addItem(new Double(i/10.));
		}
		_minSize.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
	        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	            component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	            JLabel label = (JLabel)component;
	            label.setText(label.getText() + " MB");
	            return component;
	        }
		});
		
		
		JPanel minPanel = new JPanel(new BorderLayout());
		minPanel.add(minLabel, BorderLayout.WEST);
		minPanel.add(_minSize, BorderLayout.CENTER);
		
		//---------------------------------------------------------		
		
		JLabel overwriteLabel = new JLabel(" Overwrite : ");
		overwriteLabel.setPreferredSize(new Dimension(75, 1));
		
		_overwrite = new JComboBox<Boolean>();
		_overwrite.addItem(Boolean.TRUE);
		_overwrite.addItem(Boolean.FALSE);
		_overwrite.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
	        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	            component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	            return component;
	        }
		});		
		
		
		JPanel overwritePanel = new JPanel(new BorderLayout());
		overwritePanel.add(overwriteLabel, BorderLayout.WEST);
		overwritePanel.add(_overwrite, BorderLayout.CENTER);
		
		//---------------------------------------------------------
		
		JPanel srcdstPanel = new JPanel();
		srcdstPanel.setLayout(new GridLayout(4, 1));
		srcdstPanel.add(_srcDir);
		srcdstPanel.add(_dstDir);
		srcdstPanel.add(minPanel);
		srcdstPanel.add(overwritePanel);
		
		//---------------------------------------------------------
		
		_optimizeButton = new JButton("Optimize");
		_optimizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optimize();
			}
		});
		_optimizeButton.setEnabled(false);
		
		_jprogressBar = new JProgressBar();
		_jprogressBar.setValue(0);
		_jprogressBar.setMaximum(100);
		_jprogressBar.setStringPainted(true);
		
		//---------------------------------------------------------
		
		_jList = new JPEGFilesList(this);
        JScrollPane scrollPaneList = new JScrollPane(_jList);
        scrollPaneList.setPreferredSize(new Dimension(650,300));
        
		//---------------------------------------------------------
        _loger = new JLoger();
        _loger.setPreferredSize(new Dimension(200,100));
        JScrollPane scrollPaneLog = new JScrollPane(_loger);
        
        //---------------------------------------------------------
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneList, scrollPaneLog);
        
		//---------------------------------------------------------
		
        JLabel aboutLabel = new JLabel("About");
        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        aboutLabel.setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new CompoundBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY), new EmptyBorder(1, 4, 1, 5))));
        
        aboutLabel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
		        e.getComponent().setForeground(Color.BLACK);
		        e.getComponent().setCursor(Cursor.getDefaultCursor());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				e.getComponent().setForeground(Color.BLUE);
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				about();
			}
		});
        
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(aboutLabel, BorderLayout.EAST);

        
        
        //---------------------------------------------------------
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(srcdstPanel, BorderLayout.CENTER);
		northPanel.add(_optimizeButton, BorderLayout.EAST);
		northPanel.add(_jprogressBar, BorderLayout.SOUTH);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(northPanel, BorderLayout.NORTH);
		contentPanel.add(splitPane, BorderLayout.CENTER);
		contentPanel.add(statusBar, BorderLayout.SOUTH);
		
		//---------------------------------------------------------
		
		readConfig();
		
		//---------------------------------------------------------
		
		_jFrame = new JFrame("JPEG Optimizer");
		_jFrame.setContentPane(contentPanel);
		_jFrame.setIconImage(Icon.getInstance().getFrameIcon());
		_jFrame.pack();
		_jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		_jFrame.setLocationRelativeTo(null);
		_jFrame.setVisible(true);
		
		//---------------------------------------------------------
		
		_viewer = new Viewer(_jFrame);
	}
	
	
	public void displayJPEGFiles(JPEGFiles jpegFile) {
		if (jpegFile == null) {
			return ;
		}
		
		try {
			_viewer.setPictures(jpegFile.getSrc(), jpegFile.getDst());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void about() {
		new About(_jFrame).setVisible(true);
	}
	
	private void optimize() {
		setOptimizeState(false);
		writeConfig();
		new OptimizeThread().start();
	}
	
	private File getConfigFile() {
		return new File("JpegOptimizer.ini");
	}
	
	private void writeConfig() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(getConfigFile()));
			writer.write(_srcDir.getSelectedDirectory().getAbsolutePath() + "\n");
			writer.write(_dstDir.getSelectedDirectory().getAbsolutePath() + "\n");
			writer.write(_minSize.getSelectedIndex() + "\n");
			writer.write(_overwrite.getSelectedIndex() + "\n");
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            try {
            	writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	private void readConfig() {
		File configFile = getConfigFile();
		if (configFile.exists() == false) {
			return ;
		}
		BufferedReader reader = null;
		try  {
			reader = new BufferedReader(new FileReader(configFile));
			_srcDir.setSelectedDirectory(reader.readLine());
			_dstDir.setSelectedDirectory(reader.readLine());
			_minSize.setSelectedIndex(Integer.parseInt(reader.readLine()));
			_overwrite.setSelectedIndex(Integer.parseInt(reader.readLine()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            try {
            	reader.close();
            } catch (Exception e) {
            }
        }
	}
	
	private void setOptimizeState(boolean state) {
		_srcDir.setEnabled(state);
		_dstDir.setEnabled(state);
		_minSize.setEnabled(state);
		_overwrite.setEnabled(state);
		_optimizeButton.setEnabled(state);
	}
	
	@Override
	public void directoryChoosed(File selectedDir, JDirectoryChooser src) {
		if (src == _srcDir) {
			_jList.listSrcFiles(_srcDir.getSelectedDirectory());
		}
		_jList.updateListDstDir(_dstDir.getSelectedDirectory());
		_optimizeButton.setEnabled((_srcDir.getSelectedDirectory() != null) && (_dstDir.getSelectedDirectory() != null));
	}
	
	public Loger getLoger() {
		return _loger;
	}
	
	private long isNull(Long l, long value) {
		return l == null ? value : l;
	}

	private class OptimizeThread extends Thread {
		public void run() {
			if ((_srcDir.getSelectedDirectory() != null) && (_dstDir.getSelectedDirectory() != null)) {
				if (_srcDir.getSelectedDirectory().isDirectory() && _dstDir.getSelectedDirectory().isDirectory()) {
					long minSize = (long) ((Double)_minSize.getSelectedItem() * 1024 * 1024); //convert from Mb to Kb to b;
					boolean overwriteDst = (Boolean)_overwrite.getSelectedItem();
					
					//Init
					_loger.setText("");
					_jprogressBar.setValue(0);
					for (int i = 0; i < _jList.getModel().getSize(); ++i) {
						JPEGFiles jpegFile = _jList.getModel().getElementAt(i);
						jpegFile.reinitState();
					}
					
					//Do compression
					long earnSize = 0;
					for (int i = 0; i < _jList.getModel().getSize(); ++i) {
						_jprogressBar.setValue((i*100)/_jList.getModel().getSize());
						JPEGFiles jpegFile = _jList.getModel().getElementAt(i);
						_jList.setSelectedIndex(i);
						_jList.ensureIndexIsVisible(i);
						if (jpegFile.getSrc() != null) {
							try {
								jpegFile.optimize(_dstDir.getSelectedDirectory(), minSize, overwriteDst);
								earnSize += isNull(jpegFile.getEarnSize(), 0);
							} catch (IOException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(_jFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
							_jList.repaint();
						}
					}
					
					//End
					_jprogressBar.setValue(100);
					JOptionPane.showMessageDialog(_jFrame, "Optmization done. Earn " + ReadableUtils.fileSize(earnSize) + "!", "Done", JOptionPane.INFORMATION_MESSAGE);
					
				}
			}
			setOptimizeState(true);
		}
	}
	
	//-- Gui Interface --------------------------------------------------------
	
	public void stateChange(JPEGFiles jpegFile) {
		_jList.repaint();
	}

}

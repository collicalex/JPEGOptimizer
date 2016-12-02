package gui.components;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utils.GuiUtils;


public class JDirectoryChooser extends JPanel implements ActionListener, DocumentListener {
	
	private static final long serialVersionUID = 1L;
	private JLabel _label;
	private JTextField _textField;
	private JButton _browseButton;
	private JFileChooser _fileChooser;
	
	private Color _okBg;
	private Color _okFg;
	private Color _koBg;
	private Color _koFg;
	
	private File _selectedDirectory;
	
	private List<JDirectoryChooserListener> _listeners;
	
	public JDirectoryChooser(String label, int width) {
		_selectedDirectory = null;
		
		_listeners = new LinkedList<JDirectoryChooserListener>();
		
		_label = new JLabel(" " + label + " ");
		_label.setPreferredSize(new Dimension(width, 1));
		
		_textField = new JTextField();
		_textField.getDocument().addDocumentListener(this);
		
		_koBg = new Color(255, 204, 204);
		_koFg = new Color(143, 0, 0);
		_okFg = _textField.getForeground();
		_okBg = _textField.getBackground();		
		
		_browseButton = new JButton("...");
		_browseButton.addActionListener(this);
		
		_fileChooser = new JFileChooser();
		_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
		
		this.setLayout(new BorderLayout());
		this.add(_label, BorderLayout.WEST);
		this.add(_textField, BorderLayout.CENTER);
		this.add(_browseButton, BorderLayout.EAST);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_textField.setEnabled(enabled);
		_browseButton.setEnabled(enabled);
	}
	
	public File getSelectedDirectory() {
		return _selectedDirectory;
	}
	
	public void setSelectedDirectory(String path) {
		setSelectedDirectory(new File(path), _browseButton);
	}

	public void addDirectoryChooserListner(JDirectoryChooserListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _browseButton) {
			chooseDirectory();
		}
	}
	
	private void chooseDirectory() {
		int returnVal = _fileChooser.showOpenDialog(GuiUtils.getOldestParent(_browseButton));
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			setSelectedDirectory(_fileChooser.getSelectedFile(), _browseButton);
        }
	}
	
	private void setSelectedDirectory(File selectedDirectory, Object src) {
		if (selectedDirectory.exists()) {
			if (selectedDirectory.isDirectory()) {
				_selectedDirectory = selectedDirectory;
				if (src == _browseButton) {
					_textField.setText(_selectedDirectory.getAbsolutePath());
				}
				setValidateButtonState(true);
				for (JDirectoryChooserListener listener : _listeners) {
					listener.directoryChoosed(_selectedDirectory, this);
				}
			}
		}
	}
	
	private void setValidateButtonState(boolean isValidated) {
		if (isValidated) {
			_textField.setBackground(_okBg);
			_textField.setForeground(_okFg);
		} else {
			_textField.setBackground(_koBg);
			_textField.setForeground(_koFg);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		toggleValidateButtonState();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		toggleValidateButtonState();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		toggleValidateButtonState();
	}
	
	private void toggleValidateButtonState() {
		File dir = new File(_textField.getText());
		if (dir.exists()) {
			if (dir.isDirectory()) {
				setSelectedDirectory(dir, _textField);
				return;
			}
		}
		setValidateButtonState(_textField.getText().length() == 0);
	}
}

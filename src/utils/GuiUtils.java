package utils;

import java.awt.Container;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class GuiUtils {
	
	public static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
    public static Container getOldestParent(Container container) {
    	while (container.getParent() != null) {
    		container = container.getParent();
    	}
    	return container;
    }
}

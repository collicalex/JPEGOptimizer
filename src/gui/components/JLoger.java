package gui.components;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import core.Loger;

public class JLoger extends JTextPane implements Loger {

  private static final long serialVersionUID = 1L;
  private String _cr = System.getProperty("line.separator");

  private void appendToPane(JTextPane tp, String msg, Color c) {
    synchronized (tp) {
      StyleContext sc = StyleContext.getDefaultStyleContext();
      AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

      aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
      aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

      int len = tp.getDocument().getLength();
      tp.setCaretPosition(len);
      tp.setCharacterAttributes(aset, false);
      tp.replaceSelection(msg);
    }
  }

  private void appendCR(boolean addCR) {
    if (addCR) {
      this.appendToPane(this, _cr, Color.BLACK);
    }
  }

  @Override
  public void log(String txt, boolean addCR) {
    appendToPane(this, txt, Color.BLACK);
    appendCR(addCR);
  }

  @Override
  public void warn(String txt, boolean addCR) {
    appendToPane(this, txt, Color.ORANGE);
    appendCR(addCR);
  }

  @Override
  public void error(String txt, boolean addCR) {
    appendToPane(this, txt, Color.RED);
    appendCR(addCR);
  }

  @Override
  public void success(String txt, boolean addCR) {
    appendToPane(this, txt, new Color(34, 139, 34));
    appendCR(addCR);
  }

}

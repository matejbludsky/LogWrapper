package cz.wincor.pnc.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/**
 * @author matej.bludsky
 * 
 *         Test field with Hint option
 */

public class HintTextField extends BasicTextFieldUI implements FocusListener {

    private String hint;
    private boolean hideOnFocus = true;
    private Color color;

    public HintTextField(String hint, boolean hideOnFocus, Color color) {
        super();
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }

    public HintTextField(String hint, boolean hideOnFocus) {
        super();
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
    }

    private void repaint() {
        if (getComponent() != null) {
            getComponent().repaint();
        }
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isHideOnFocus() {
        return hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        this.hideOnFocus = hideOnFocus;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(this);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);

        JTextComponent comp = getComponent();
        if (hint != null && comp.getText().length() == 0 && (!(hideOnFocus && comp.hasFocus()))) {
            if (color != null) {
                g.setColor(color);
            } else {
                g.setColor(comp.getForeground().brighter().brighter().brighter());
            }

            g.drawString(hint, 5, comp.getHeight() - 6);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (hideOnFocus) {
            repaint();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (hideOnFocus) {
            repaint();
        }
    }

}

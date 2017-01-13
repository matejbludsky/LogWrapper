package cz.wincor.pnc.gui.component;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * @author matej.bludsky
 * 
 * 
 */

public class ImageJLabel extends JLabel {

    private BufferedImage image;
    private int initialX = 0;
    private int initialY = 0;

    public ImageJLabel(BufferedImage image, ImageIcon icon) {
        super(icon);
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getInitialX() {
        return initialX;
    }

    public void setInitialX(int initialX) {
        this.initialX = initialX;
    }

    public int getInitialY() {
        return initialY;
    }

    public void setInitialY(int initialY) {
        this.initialY = initialY;
    }

}

package cz.wincor.pnc.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;

/**
 * @author matej.bludsky
 * 
 *         JFrame class for displaying About me
 * 
 */

public class AboutMeFrame extends JFrame implements ILogWrapperUIRenderer, WindowListener {

    private static final Logger LOG = Logger.getLogger(AboutMeFrame.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderUI(Object... parameters) throws UIRenderException {
        LOG.info("Rendering About Me Frame");

        try {

            setResizable(false);

            Font header = new Font("TimesRoman", Font.PLAIN, 20);

            setPreferredSize(new Dimension(300, 170));

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(new Point((int) screenSize.getWidth() / 2 - 150, (int) screenSize.getHeight() / 2 - 85));

            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(300, 250));

            Label author = new Label("Author");
            author.setFont(header);
            author.setPreferredSize(new Dimension(280, 20));

            Label name = new Label("Name : Matej Bludsky");
            name.setPreferredSize(new Dimension(280, 15));
            Label email = new Label("Email : matej.bludsky@dieboldnixdorf.com");
            email.setPreferredSize(new Dimension(280, 15));
            Label email2 = new Label("Email : matej.bludsky@gmail.com");
            email2.setPreferredSize(new Dimension(280, 15));

            Properties p = loadMavenProperties();
            String versionMaven = p.getProperty("logWrapper.version");

            Label appName = new Label("LogWrapper");
            appName.setPreferredSize(new Dimension(280, 20));
            appName.setFont(header);
            Label version = new Label("Version :" + versionMaven);
            version.setPreferredSize(new Dimension(280, 15));
            BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);

            panel.add(author);
            panel.add(name);
            panel.add(email);
            panel.add(email2);
            panel.add(appName);
            panel.add(version);

            getContentPane().add(panel, BorderLayout.CENTER);
            pack();

            setVisible(true);

        } catch (Exception e) {
            LOG.error("Cannot render AboutMeFrame", e);
            throw new UIRenderException("Cannot render AboutMeFrame");
        }
    }

    /**
     * Method loads maven.properties file that is generated via Maven build plugin
     * 
     * @return
     * @throws IOException
     */
    private Properties loadMavenProperties() throws IOException {
        java.io.InputStream is = getClass().getResourceAsStream("/maven.properties");
        java.util.Properties p = new Properties();

        p.load(is);
        return p;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent e) {
        setVisible(false);
        LOG.info("AboutMeFrame window closed");
        LogWrapperUIJFrame.getInstance().setVisible(true);

    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}

package cz.wincor.pnc.GUI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * Settings panel that includes message types to be included and ATM search
 * 
 * @author matej.bludsky
 *
 */
public class SettingsPanel extends JPanel implements ILogWrapperUIRenderer, ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SettingsPanel.class);

    public SettingsPanel() {
        LOG.info("Starting Settings Panel");
    }

    @Override
    public void renderUI(Object... parameters) throws UIRenderException {

        try {
            setPreferredSize(new Dimension(LogWrapperUIJFrame.WIDTH, 150));
            BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
            this.setLayout(layout);
            
            add(renderLeftPanel());
            add(renderRightPanel());
            LOG.debug("Drag and Drop panel rendered");
        } catch (Exception e) {
            LOG.error("Cannot render SettingsPanel", e);
            throw new UIRenderException("Cannot render SettingsPanel", e);
        }
    }

    /**
     * creates panel that is on left side
     * 
     * @return
     */
    private JPanel renderLeftPanel() {

        JPanel left = new JPanel();
        left.setPreferredSize(new Dimension(250, 150));

        GridLayout layout = new GridLayout(4, 1);

        left.add(getHeartbeat(), layout);
        left.add(getFreeJournal(), layout);
        left.add(getWorkstationChangeEvents(), layout);
        left.add(getCassCounters(), layout);

        left.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Include"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        return left;
    }

    /**
     * Creates panel that is on right side
     * 
     * @return
     */
    private JPanel renderRightPanel() {

        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(250, 150));

        GridLayout layout = new GridLayout(4, 1);
        right.add(getATMIDSelector(), layout);
        right.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Filter"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        return right;
    }

    /**
     * Creates text field for ATM seach
     * 
     * @return
     */
    private JTextField getATMIDSelector() {

        JTextField atmId = new JTextField();
        atmId.setUI(new HintTextField("Enter ATM ID REGEXP", true));
        atmId.setPreferredSize(new Dimension(200, 20));

        atmId.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField field = (JTextField) e.getSource();

                LogWrapperSettings.ATM_ID.setValue(field.getText());
                LOG.info("ATM ID changed to " + field.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

        return atmId;
    }

    /**
     * Radio button for heartbeat
     * 
     * @return
     */
    private JRadioButton getHeartbeat() {
        JRadioButton heartbeat = new JRadioButton("Heartbeat");
        heartbeat.setPreferredSize(new Dimension(220, 20));
        heartbeat.addActionListener(this);
        return heartbeat;
    }

    /**
     * Radio button for CassCounters
     * 
     * @return
     */
    private JRadioButton getCassCounters() {
        JRadioButton heartbeat = new JRadioButton("Cassette Counter Change");
        heartbeat.setPreferredSize(new Dimension(220, 20));
        heartbeat.addActionListener(this);
        return heartbeat;
    }

    /**
     * Radio button for FreeJournal
     * 
     * @return
     */
    private JRadioButton getFreeJournal() {
        JRadioButton freeJournal = new JRadioButton("Free Journal");
        freeJournal.setPreferredSize(new Dimension(220, 20));
        freeJournal.addActionListener(this);
        return freeJournal;
    }

    /**
     * Radio button for WorkstationChangeEvents
     * 
     * @return
     */
    private JRadioButton getWorkstationChangeEvents() {
        JRadioButton wkstChangeEvents = new JRadioButton("Workstation Events");
        wkstChangeEvents.setPreferredSize(new Dimension(220, 20));
        wkstChangeEvents.addActionListener(this);
        return wkstChangeEvents;
    }

    /**
     * Action performed implementation that reflects the values of radio buttons and text field into LogWrapperSettings
     * instance
     */
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();
        JRadioButton source = (JRadioButton) e.getSource();
        LOG.debug(command + " selected : " + source.isSelected());
        switch (command) {
        case "Heartbeat":
            LogWrapperSettings.HEARTBEAT.setValue(source.isSelected());
            break;

        case "Free Journal":
            LogWrapperSettings.FREEJOURNAL.setValue(source.isSelected());

        case "Workstation Events":
            LogWrapperSettings.WKSTCHANGEEVENTS.setValue(source.isSelected());
            break;

        case "Force ATM Tag":
            LogWrapperSettings.FORCEATMTAG.setValue(source.isSelected());
            break;

        case "Cassette Counter Change":
            LogWrapperSettings.CASSCOUNTER.setValue(source.isSelected());
            break;

        default:
            break;
        }

    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}

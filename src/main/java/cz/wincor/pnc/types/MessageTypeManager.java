package cz.wincor.pnc.types;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author matej.bludsky
 * 
 *         Class for messages type, determines color for each message
 * 
 */

public class MessageTypeManager {

    /**
     * Annonym enum for message type
     * 
     * @author matej.bludsky
     *
     */
    public enum MessageType {
        TRX, EVENT, FREEJOURNAL
    };

    /**
     * Annonym class for Message
     * 
     * @author matej.bludsky
     *
     */
    public static class Message {
        private String name;
        private Color background;
        private Color foreground;

        public Message(String name, Color background, Color foreground) {
            super();
            this.name = name;
            this.background = background;
            this.foreground = foreground;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Color getBackground() {
            return background;
        }

        public void setBackground(Color background) {
            this.background = background;
        }

        public Color getForeground() {
            return foreground;
        }

        public void setForeground(Color foreground) {
            this.foreground = foreground;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof String) {

                if (this.name.contains(obj.toString())) {
                    return true;
                }

            }
            return false;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

    }

    public static Set<Message> settings;

    public static Message defaults = new Message("*", Color.GREEN.brighter(), Color.BLACK);

    static {

        settings = new HashSet<>();
        settings.add(new Message("Login", Color.YELLOW.brighter(), Color.BLACK));
        settings.add(new Message("Logout", Color.YELLOW, Color.BLACK));
        settings.add(new Message("Event", Color.ORANGE, Color.BLACK));
        settings.add(new Message("Free", Color.MAGENTA, Color.BLACK));
        settings.add(new Message("Process", Color.LIGHT_GRAY, Color.BLACK));
        settings.add(new Message("Authorize", Color.CYAN, Color.BLACK));
        settings.add(new Message("Finalize", Color.RED, Color.BLACK));
        //for RSS
        settings.add(new Message("RSSCardAcctPartyRelInqRequest", Color.YELLOW, Color.BLACK));
       // settings.add(new Message("RSSCardAcctPartyRelInqResponse", Color.YELLOW, Color.BLACK));
        
        settings.add(new Message("RSSCreditAdviseRequest", Color.ORANGE, Color.BLACK));
       // settings.add(new Message("RSSCreditAdviseResponse", Color.MAGENTA, Color.BLACK));
        
        
    }

    /**
     * Determines Type from given message
     * 
     * @param name
     * @return
     */
    public static Message fromString(String name) {

        for (Iterator<Message> iterator = settings.iterator(); iterator.hasNext();) {
            Message message = (Message) iterator.next();
            if (name.contains(message.getName())) {
                return message;
            }
        }

        return defaults;

    }

}

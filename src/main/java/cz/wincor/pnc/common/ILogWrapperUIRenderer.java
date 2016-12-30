package cz.wincor.pnc.common;

import cz.wincor.pnc.error.UIRenderException;

/**
 * 
 * @author matej.bludsky
 *
 *         Interface for UI renderer component
 */
public interface ILogWrapperUIRenderer {

    /**
     * Each component uses this method to render Graphic content that should be displayed
     * 
     * @throws Exception
     */
    void renderUI(Object... parameters) throws UIRenderException;

    /**
     * repaints and set visible to true
     */
    void display();
}

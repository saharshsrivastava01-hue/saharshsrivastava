package com.goldtrader.ui.panels;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Simple DocumentListener that calls a single callback on any change.
 */
public class SimpleDocListener implements DocumentListener {
    private final Runnable callback;

    public SimpleDocListener(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void insertUpdate(DocumentEvent e) { callback.run(); }

    @Override
    public void removeUpdate(DocumentEvent e) { callback.run(); }

    @Override
    public void changedUpdate(DocumentEvent e) { callback.run(); }
}

package org.vpreportcorrector.diagram.components;

import javafx.embed.swing.SwingNode;

public class CustomSwingNode extends SwingNode {
    @Override
    public boolean isResizable() {
        return false;
    }
}

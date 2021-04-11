package com.github.ormfux.esi.ui.index;

import javafx.scene.control.TabPane;

public class IndexDetailsTabPane extends TabPane {

    public void addIndexDetailsTab(final IndexDetailsTab detailsTab) {
        getTabs().add(detailsTab);
    }
    
}

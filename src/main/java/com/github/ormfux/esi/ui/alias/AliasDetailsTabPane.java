package com.github.ormfux.esi.ui.alias;

import javafx.scene.control.TabPane;

public class AliasDetailsTabPane extends TabPane {

    public void addIndexDetailsTab(final AliasDetailsTab detailsTab) {
        getTabs().add(detailsTab);
    }
    
}

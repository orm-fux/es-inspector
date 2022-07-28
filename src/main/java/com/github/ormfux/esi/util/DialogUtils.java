package com.github.ormfux.esi.util;

import javafx.scene.control.Dialog;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DialogUtils {

    public static void configureForOperatingSystem(final Dialog<?> dialog) {
        makeResizableForNonWindows(dialog);
    }

    //Fix for dialogs displayed without content (https://bugs.openjdk.org/browse/JDK-8179073)
    private static void makeResizableForNonWindows(final Dialog<?> dialog) {
        final String operatingSystem = System.getProperty("os.name", "generic");

        if (!operatingSystem.contains("Windows")) {
            dialog.setResizable(true);
        }
    }

}

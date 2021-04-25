package com.github.ormfux.esi.ui.component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import lombok.Getter;

public class AsyncButton extends Button {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);
    
    @Getter
    private final Node runningIndicator;
    
    public AsyncButton(final String text) {
        this(text, createRunningIndicator());
    }

    public AsyncButton(final String text, final Node runningIndicator) {
        super(text);
        this.runningIndicator = runningIndicator;
        runningIndicator.setVisible(false);
    }
    
    public void setAction(final Runnable task) {
        setOnAction(e -> {
            THREAD_POOL.execute(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        runningIndicator.setVisible(true);
                        task.run();
                    } finally {
                        runningIndicator.setVisible(false);
                    }
                    
                    return null;
                }
            });
        });
    }
    
    public static ImageView createRunningIndicator() {
        return ImageRegistry.getImageView(ImageKey.RUNNING_TASK, 20, 20);
    }
    
}

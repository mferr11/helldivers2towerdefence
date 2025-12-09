package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.player.BuildComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class deckUI extends UIComponent {
    private Table table;
    private Entity playerRef;

    public deckUI(Entity player) {
        this.playerRef = player;
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        table = new Table();
        table.setFillParent(true);
        TextButton buildButton = new TextButton("Build Mode", skin);

        buildButton.addListener(
                    new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", !(playerRef.getComponent(BuildComponent.class).getBuildMode()));
          }
        });

        table.add(buildButton);

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }

}

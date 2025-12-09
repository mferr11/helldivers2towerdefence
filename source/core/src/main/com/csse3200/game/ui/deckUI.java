package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
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
        table.bottom();

        // Create a button for each tower type
        for (TowerType towerType : TowerType.values()) {
            TextButton towerButton = new TextButton(getTowerDisplayName(towerType), skin);
            
            towerButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    // Select this tower type
                    ServiceLocator.getGameAreaEvents().trigger("selectTowerType", towerType);
                    // Enter build mode
                    ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", true);
                }
            });
            
            table.add(towerButton).padBottom(10).padRight(5);
        }

        stage.addActor(table);
    }
    
    /**
     * Get a display-friendly name for the tower type
     */
    private String getTowerDisplayName(TowerType type) {
        switch (type) {
            case MACHINEGUN:
                return "Machine Gun Sentry";
            case RAILGUN:
                return "Railgun Sentry";
            case ROCKET:
                return "Rocket Sentry";
            default:
                return type.name();
        }
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
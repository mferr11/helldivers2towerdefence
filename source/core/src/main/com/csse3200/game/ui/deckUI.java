package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashMap;

public class deckUI extends UIComponent {
    private Table table;
    private Entity playerRef;
    private HashMap<TowerType, TextButton> towerButtons = new HashMap<>();

    public deckUI(Entity player) {
        this.playerRef = player;
    }

    @Override
    public void create() {
        super.create();
        addActors();
        
        // Listen for gold changes to update button states
        ServiceLocator.getGameAreaEvents().addListener("updateGold", this::updateButtonStates);
    }

    private void addActors() {
        table = new Table();
        table.setFillParent(true);
        table.bottom();

        // Create a button for each tower type
        for (TowerType towerType : TowerType.values()) {
            TowerConfig config = TowerFactory.getConfig(towerType);
            String buttonText = getTowerDisplayName(towerType) + " ($" + config.cost + ")";
            TextButton towerButton = new TextButton(buttonText, skin);
            
            towerButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    // Check if player can afford this tower
                    InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);
                    if (inventory.hasGold(config.cost)) {
                        // Select this tower type
                        ServiceLocator.getGameAreaEvents().trigger("selectTowerType", towerType);
                        // Enter build mode
                        ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", true);
                    } else {
                        System.out.println("Not enough gold! Need $" + config.cost + ", have $" + inventory.getGold());
                    }
                }
            });
            
            towerButtons.put(towerType, towerButton);
            table.add(towerButton).padBottom(10).padRight(5);
        }
        
        // Initial button state update
        updateButtonStates();
        
        stage.addActor(table);
    }
    
    /**
     * Update button enabled/disabled state based on player's gold
     */
    private void updateButtonStates() {
        InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);
        boolean canAffordAny = false;
        
        for (TowerType towerType : TowerType.values()) {
            TowerConfig config = TowerFactory.getConfig(towerType);
            TextButton button = towerButtons.get(towerType);
            
            if (button != null) {
                boolean canAfford = inventory.hasGold(config.cost);
                
                // Disable button if player can't afford it
                button.setDisabled(!canAfford);
                
                // Change button color when disabled
                if (button.isDisabled()) {
                    button.getLabel().setColor(0.5f, 0.5f, 0.5f, 1f);
                    button.setColor(0.5f, 0.5f, 0.5f, 1f); // Gray out
                } else {
                    button.getLabel().setColor(1f, 1f, 1f, 1f);
                    button.setColor(1f, 1f, 1f, 1f); // Normal color
                    canAffordAny = true;
                }
            }
        }
        
        // Exit build mode if player can't afford any towers
        if (!canAffordAny) {
            ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", false);
        }
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
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }
}
package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.events.listeners.EventListener1;

public class TowerActionsUI extends UIComponent {
    private Table table;
    private Entity selectedTower = null;
    private Entity playerRef;
    
    private Label towerInfoLabel;
    private TextButton upgradeButton;
    private TextButton sellButton;
    private TextButton closeButton;

    public TowerActionsUI(Entity player) {
        this.playerRef = player;
    }

    @Override
    public void create() {
        super.create();
        addActors();
        
        // Listen for tower clicks
        ServiceLocator.getGameAreaEvents().addListener("towerClicked", 
            (EventListener1<Entity>) this::showTowerActions);
    }

    private void addActors() {
        table = new Table();
        table.bottom().right();
        table.setFillParent(true);
        table.padBottom(10).padRight(10);
        
        // Create a container for the tower actions
        Table actionsTable = new Table(skin);
        
        // Tower info label
        towerInfoLabel = new Label("", skin);
        actionsTable.add(towerInfoLabel).colspan(2).padBottom(10);
        actionsTable.row();
        
        // Upgrade button
        upgradeButton = new TextButton("Upgrade", skin);
        upgradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                upgradeTower();
            }
        });
        actionsTable.add(upgradeButton).padRight(5);
        
        // Sell button
        sellButton = new TextButton("Sell", skin);
        sellButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sellTower();
            }
        });
        actionsTable.add(sellButton);
        actionsTable.row();
        
        // Close button
        closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideUI();
                ServiceLocator.getGameAreaEvents().trigger("deselectTower");
            }
        });
        actionsTable.add(closeButton).colspan(2).padTop(5).width(350);
        
        table.add(actionsTable);
        
        // Initially hidden
        table.setVisible(false);
        
        stage.addActor(table);
    }
    
    private void showTowerActions(Entity tower) {
        selectedTower = tower;
        
        // Get tower stats
        TowerStatsComponent stats = tower.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            System.err.println("Tower has no TowerStatsComponent!");
            return;
        }
        
        // Update UI with real tower info
        String towerName = getTowerDisplayName(stats.getTowerType());
        int upgradeLevel = stats.getUpgradeLevel();
        towerInfoLabel.setText(towerName + " (Level " + upgradeLevel + ")");
        
        // Get real costs
        int upgradeCost = stats.getUpgradeCost();
        int sellValue = stats.getSellValue();
        
        upgradeButton.setText("Upgrade ($" + upgradeCost + ")");
        sellButton.setText("Sell ($" + sellValue + ")");
        
        // Check if player can afford upgrade
        InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);
        upgradeButton.setDisabled(!inventory.hasGold(upgradeCost));
        if (upgradeButton.isDisabled()) {
            upgradeButton.getLabel().setColor(0.5f, 0.5f, 0.5f, 1f);
        } else {
            upgradeButton.getLabel().setColor(1f, 1f, 1f, 1f);
        }
        
        table.setVisible(true);
    }
    
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
    
    private void hideUI() {
        table.setVisible(false);
        selectedTower = null;
    }
    
    private void upgradeTower() {
        if (selectedTower == null) return;
        
        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) return;
        
        int upgradeCost = stats.getUpgradeCost();
        InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);
        
        // Check if player can afford it
        if (!inventory.hasGold(upgradeCost)) {
            System.out.println("Cannot upgrade: Not enough gold!");
            return;
        }
        
        // Deduct gold
        inventory.addGold(-upgradeCost);
        
        // Upgrade the tower
        stats.incrementUpgradeLevel();
        
        // TODO: Actually increase tower stats (damage, range, etc.)
        
        System.out.println("Upgraded tower to level " + stats.getUpgradeLevel());
        ServiceLocator.getGameAreaEvents().trigger("updateGold");
        
        // Refresh UI to show new upgrade cost and level
        showTowerActions(selectedTower);
    }
    
    private void sellTower() {
        if (selectedTower == null) return;
        
        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) return;
        
        int sellValue = stats.getSellValue();
        
        // Give player gold
        playerRef.getComponent(InventoryComponent.class).addGold(sellValue);
        
        // Remove tower from game
        ServiceLocator.getGameAreaEvents().trigger("sellTower", selectedTower);
        
        System.out.println("Sold tower for $" + sellValue);
        ServiceLocator.getGameAreaEvents().trigger("updateGold");
        hideUI();
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
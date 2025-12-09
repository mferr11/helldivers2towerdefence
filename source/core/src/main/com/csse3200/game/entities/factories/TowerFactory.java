package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.towers.TowerAttackComponent;
import com.csse3200.game.components.towers.TowerPreviewComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.entities.configs.TowerConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;

public class TowerFactory {
    private static final TowerConfigs configs = 
        FileLoader.readClass(TowerConfigs.class, "configs/towers.json");
    private static final float PREVIEW_OPACITY = 0.5f;
    
    public enum TowerType {
        MACHINEGUN("machinegun"),
        RAILGUN("railgun");
        
        private final String configKey;
        
        TowerType(String configKey) {
            this.configKey = configKey;
        }
        
        public String getConfigKey() {
            return configKey;
        }
    }
    
    /**
     * Create a tower of the specified type
     * @param type The type of tower to create
     * @return The tower entity
     */
    public static Entity createTower(TowerType type) {
        TowerConfig config = configs.towers.get(type.getConfigKey());
        
        if (config == null) {
            System.err.println("Failed to load tower config for: " + type.getConfigKey());
            config = new TowerConfig(); // Use defaults
        }
        
        return createTower(config);
    }
    
    /**
     * Create a tower from a config
     * @param config The tower configuration
     * @return The tower entity
     */
    private static Entity createTower(TowerConfig config) {
        CombatStatsComponent combatStats = new CombatStatsComponent(
            config.health, 
            config.baseAttack, 
            config.baseArmourRating
        );

        Entity tower = new Entity()
            .addComponent(new TextureRenderComponent(config.texturePath))
            .addComponent(new TowerAttackComponent(
                config.attackRadius, 
                config.attackCooldown, 
                combatStats
            ));

        return tower;
    }
    
    /**
     * Create a tower preview for the specified type
     */
    public static Entity createTowerPreview(TowerType type) {
        TowerConfig config = configs.towers.get(type.getConfigKey());
        
        if (config == null) {
            System.err.println("Failed to load tower config for preview: " + type.getConfigKey());
            config = new TowerConfig();
        }
        
        Entity towerPreview = new Entity()
            .addComponent(new TowerPreviewComponent())
            .addComponent(new TextureRenderComponentAlpha(config.texturePath, PREVIEW_OPACITY));

        return towerPreview;
    }
}
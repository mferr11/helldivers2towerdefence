package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.towers.RadiusDisplayComponent;
import com.csse3200.game.components.towers.TowerActionsComponent;
import com.csse3200.game.components.towers.TowerAttackComponent;
import com.csse3200.game.components.towers.TowerPreviewComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
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
    RAILGUN("railgun"),
    ROCKET("rocket");

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
   *
   * @param type The type of tower to create
   * @return The tower entity
   */
  public static Entity createTower(TowerType type) {
    TowerConfig config = configs.towers.get(type.getConfigKey());

    if (config == null) {
      System.err.println("Failed to load tower config for: " + type.getConfigKey());
      config = new TowerConfig(); // Use defaults
    }

    return createTower(config, type);
  }

  /**
   * Create a tower from a config
   *
   * @param config The tower configuration
   * @return The tower entity
   */
  private static Entity createTower(TowerConfig config, TowerType type) {
    CombatStatsComponent combatStats =
        new CombatStatsComponent(config.health, config.baseAttack, config.baseArmourRating);

    Entity tower =
        new Entity()
            .addComponent(new TextureRenderComponent(config.texturePath))
            .addComponent(
                new TowerAttackComponent(config.attackRadius, config.attackCooldown, combatStats))
            .addComponent(new TowerActionsComponent())
            .addComponent(new RadiusDisplayComponent(config.attackRadius, false))
            .addComponent(new TowerStatsComponent(type, config.cost));

    return tower;
  }

  /** Create a tower preview for the specified type */
  public static Entity createTowerPreview(TowerType type) {
    TowerConfig config = configs.towers.get(type.getConfigKey());

    if (config == null) {
      System.err.println("Failed to load tower config for preview: " + type.getConfigKey());
      config = new TowerConfig();
    }

    Entity towerPreview =
        new Entity()
            .addComponent(new TowerPreviewComponent())
            .addComponent(new TextureRenderComponentAlpha(config.texturePath, PREVIEW_OPACITY))
            .addComponent(new RadiusDisplayComponent(config.attackRadius));

    return towerPreview;
  }

  /** Get the config for a specific tower type */
  public static TowerConfig getConfig(TowerType type) {
    TowerConfig config = configs.towers.get(type.getConfigKey());
    if (config == null) {
      System.err.println("Failed to load tower config for: " + type.getConfigKey());
      return new TowerConfig();
    }
    return config;
  }
}

package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.EnemyClickableComponent;
import com.csse3200.game.components.enemy.EnemyComponent;
import com.csse3200.game.components.enemy.HealthBarComponent;
import com.csse3200.game.components.enemy.WaypointTrackerComponent;
import com.csse3200.game.components.enemy.abilities.CloakComponent;
import com.csse3200.game.components.enemy.abilities.NursingComponent;
import com.csse3200.game.components.enemy.abilities.PounceComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

/** Factory to create enemy entities with predefined components. */
public class EnemyFactory {
  private static final EnemyConfigs configs =
      FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

  public enum EnemyType {
    SCAVENGER("scavenger"),
    HUNTER("hunter"),
    STALKER("stalker"),
    NURSING("nursing"),
    BROODCOMMANDER("broodcommander");

    private final String configKey;

    EnemyType(String configKey) {
      this.configKey = configKey;
    }

    public String getConfigKey() {
      return configKey;
    }
  }

  /**
   * Creates an enemy of the specified type with the provided waypoints, starting at the first
   * waypoint.
   *
   * @param type The type of enemy to create
   * @param waypoints List of waypoint entities for the enemy to follow
   * @return A fully configured enemy entity
   */
  public static Entity createEnemy(EnemyType type, List<Entity> waypoints) {
    return createEnemy(type, waypoints, 0);
  }

  /**
   * Creates an enemy of the specified type with the provided waypoints, starting at a specific
   * waypoint.
   *
   * @param type The type of enemy to create
   * @param waypoints List of waypoint entities for the enemy to follow
   * @param startWaypointIndex The waypoint index to start from (0-based)
   * @return A fully configured enemy entity
   */
  public static Entity createEnemy(EnemyType type, List<Entity> waypoints, int startWaypointIndex) {
    EnemyConfig config = configs.enemies.get(type.getConfigKey());

    if (config == null) {
      System.err.println("Failed to load enemy config for: " + type.getConfigKey());
      config = new EnemyConfig();
    }

    Entity enemy = createEnemy(config, waypoints, startWaypointIndex);

    // ENEMY SPECIFIC COMPONENTS
    switch (type) {
      case HUNTER:
        if (config.pounce != null) {
          enemy.addComponent(
              new PounceComponent(
                  config.pounce.minCooldown,
                  config.pounce.maxCooldown,
                  config.pounce.duration,
                  config.pounce.minSpeedMultiplier,
                  config.pounce.maxSpeedMultiplier,
                  config.pounce.preparationDuration));
        } else {
          enemy.addComponent(new PounceComponent(3f, 7f, 0.5f, 2.5f, 4f, 0.5f));
        }
        break;
      case STALKER:
        if (config.cloak != null) {
          enemy.addComponent(new CloakComponent(config.cloak));
        }
        break;
      case NURSING:
        if (config.nursing != null) {
          enemy.addComponent(new NursingComponent(config.nursing));
        }
        break;
      case BROODCOMMANDER:
        if (config.alpha != null) {
          enemy.addComponent(new com.csse3200.game.components.enemy.abilities.AlphaComponent(config.alpha));
        }
        break;
      case SCAVENGER:
        // Scavenger has no special abilities
        break;
      default:
        break;
    }

    return enemy;
  }

  /**
   * Creates a base enemy entity from a config with specified waypoints. The enemy will follow the
   * provided waypoints in order, and can be clicked to take damage. When the enemy's health reaches
   * zero, it will be destroyed.
   *
   * @param config The enemy configuration
   * @param waypoints List of waypoint entities for the enemy to follow in sequence
   * @param startWaypointIndex The waypoint index to start from (0-based)
   * @return A fully configured enemy entity with physics, combat stats, AI, and event listeners
   */
  private static Entity createEnemy(
      EnemyConfig config, List<Entity> waypoints, int startWaypointIndex) {
    WaypointTrackerComponent waypointTracker = new WaypointTrackerComponent(waypoints);

    // Set the starting waypoint if not starting from the beginning
    if (startWaypointIndex > 0) {
      waypointTracker.setCurrentWaypoint(startWaypointIndex);
    }

    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(
                new ChaseTask(
                    waypointTracker.getCurrentWaypointEntity(),
                    waypointTracker.getCurrentPriority(),
                    100f,
                    100f));

    CombatStatsComponent combatStats =
        new CombatStatsComponent(config.health, config.baseAttack, config.baseArmourRating);

    Entity enemy =
        new Entity()
            .addComponent(new EnemyComponent())
            .addComponent(new EnemyClickableComponent(config.clickRadius))
            .addComponent(new HealthBarComponent())
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new HitboxComponent())
            .addComponent(combatStats)
            .addComponent(waypointTracker)
            .addComponent(aiComponent)
            .addComponent(new TextureRenderComponentAlpha(config.texturePath, 1.0f));

    enemy
        .getComponent(PhysicsMovementComponent.class)
        .setMaxSpeed(new Vector2(config.speed, config.speed));

    enemy
        .getEvents()
        .addListener(
            "updateHealth",
            (EventListener1<Integer>)
                (health) -> checkEnemyHealth(enemy, health, config.goldAmount));

    enemy
        .getEvents()
        .addListener("finishedChaseTask", () -> updateWaypointTarget(enemy, config.baseAttack));

    return enemy;
  }

  /**
   * Updates the enemy's target to the next waypoint in its path. Advances the waypoint tracker and
   * assigns a new chase task to the enemy's AI. If the enemy has reached the end of the waypoint
   * list, triggers the "enemyreachedbase" event.
   *
   * @param enemy The enemy entity to update
   * @param damage The damage value to apply when enemy reaches base
   */
  private static void updateWaypointTarget(Entity enemy, int damage) {
    WaypointTrackerComponent tracker = enemy.getComponent(WaypointTrackerComponent.class);

    if (tracker.advanceWaypoint()) {
      enemy
          .getComponent(AITaskComponent.class)
          .addTask(
              new ChaseTask(
                  tracker.getCurrentWaypointEntity(), tracker.getCurrentPriority(), 100, 100));
    } else {
      if (!tracker.getFinished()) {
        // Reached the end of the waypoint list
        ServiceLocator.getGameAreaEvents().trigger("enemyreachedbase", damage);
        tracker.setFinished(true);
      }
    }
  }

  /**
   * Checks if enemy health is depleted and destroys it if necessary.
   *
   * @param enemy The enemy entity to check
   * @param newHealth The enemy's current health
   * @param goldAmount The amount of gold to award when enemy dies
   */
  private static void checkEnemyHealth(Entity enemy, int newHealth, int goldAmount) {
    if (newHealth <= 0) {
      destroyEnemy(enemy, goldAmount);
    }
  }

  /**
   * Destroys an enemy entity and cleans up its resources. Awards gold to the player and disposes
   * the entity. The disposal is posted to run on the next frame to avoid concurrent modification
   * issues.
   *
   * @param enemy The enemy entity to destroy
   * @param goldAmount The amount of gold to award
   */
  private static void destroyEnemy(Entity enemy, int goldAmount) {
    ServiceLocator.getGameAreaEvents().trigger("enemyKilled", goldAmount);

    Gdx.app.postRunnable(() -> enemy.dispose());
  }

  /** Get the config for a specific enemy type */
  public static EnemyConfig getConfig(EnemyType type) {
    EnemyConfig config = configs.enemies.get(type.getConfigKey());
    if (config == null) {
      System.err.println("Failed to load enemy config for: " + type.getConfigKey());
      return new EnemyConfig();
    }
    return config;
  }

  /**
   * Deprecated: Use createEnemy(EnemyType, waypoints) instead. This method is kept for backward
   * compatibility but will create a default enemy type.
   */
  @Deprecated
  public static Entity createBaseEnemy(List<Entity> waypoints) {
    return createEnemy(EnemyType.SCAVENGER, waypoints);
  }
}

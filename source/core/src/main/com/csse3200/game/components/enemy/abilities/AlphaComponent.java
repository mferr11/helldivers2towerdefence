package com.csse3200.game.components.enemy.abilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.WaypointTrackerComponent;
import com.csse3200.game.entities.configs.EnemyConfig.AlphaConfig;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that periodically spawns smaller enemies while the entity is alive. Used for alpha
 * enemies that continuously produce minions.
 */
public class AlphaComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(AlphaComponent.class);

  private final AlphaConfig config;
  private Timer.Task spawnTask;
  private Timer.Task pauseTask;
  private boolean isAlive = true;
  private boolean isPaused = false;
  private Vector2 originalSpeed;

  public AlphaComponent(AlphaConfig config) {
    this.config = config;
  }

  @Override
  public void create() {
    super.create();

    // Store the original speed so we can restore it after pause
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null) {
      originalSpeed = movement.getMaxSpeed().cpy();
    }

    // Listen for health updates to detect death
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);

    // Start the spawn cycle
    scheduleNextSpawn();
  }

  /** Called when the entity's health changes. Stops spawning if health reaches 0. */
  private void onHealthUpdate(int newHealth) {
    if (newHealth <= 0 && isAlive) {
      isAlive = false;
      if (spawnTask != null) {
        spawnTask.cancel();
        spawnTask = null;
      }
      if (pauseTask != null) {
        pauseTask.cancel();
        pauseTask = null;
      }
      logger.debug("Alpha enemy died, spawn cycle stopped for entity {}", entity);
    }
  }

  /** Schedule the next spawn with random duration */
  private void scheduleNextSpawn() {
    if (!isAlive) {
      return;
    }

    float spawnDelay =
        config.minAlphaTime + (float) Math.random() * (config.maxAlphaTime - config.minAlphaTime);

    spawnTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                if (isAlive) {
                  pauseBeforeSpawn();
                }
              }
            },
            spawnDelay);

    logger.debug("Next spawn scheduled in {} seconds for entity {}", spawnDelay, entity);
  }

  /** Pause movement before spawning minions */
  private void pauseBeforeSpawn() {
    pauseMovement();

    pauseTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                if (isAlive) {
                  spawnMinions();
                  resumeMovement();
                  scheduleNextSpawn();
                }
              }
            },
            config.pauseTime);
  }

  /** Pause entity movement */
  private void pauseMovement() {
    isPaused = true;

    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null) {
      movement.setMaxSpeed(Vector2.Zero);
    }

    logger.debug("Alpha entity {} paused for spawning", entity);
  }

  /** Resume entity movement to original speed */
  private void resumeMovement() {
    isPaused = false;

    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null && originalSpeed != null) {
      movement.setMaxSpeed(originalSpeed.cpy());
    }

    logger.debug("Alpha entity {} resumed movement", entity);
  }

  /** Spawns a random number of minions at the entity's current position */
  private void spawnMinions() {
    int spawnCount =
        config.minSpawnCount
            + (int) (Math.random() * (config.maxSpawnCount - config.minSpawnCount + 1));

    Vector2 spawnPosition = entity.getPosition();
    WaypointTrackerComponent tracker = entity.getComponent(WaypointTrackerComponent.class);

    if (tracker == null) {
      logger.error("AlphaComponent requires WaypointTrackerComponent");
      return;
    }

    int waypointIndex = tracker.getCurrentWaypoint();

    logger.info(
        "Alpha enemy spawning {} minions at position {} from waypoint {}",
        spawnCount,
        spawnPosition,
        waypointIndex);

    // Trigger event for ForestGameArea to handle spawning
    ServiceLocator.getGameAreaEvents()
        .trigger("spawnAlphaEnemies", spawnCount, spawnPosition, waypointIndex);
  }

  /**
   * @return true if the entity is still alive and spawning
   */
  public boolean isAlive() {
    return isAlive;
  }

  /**
   * @return true if the entity is currently paused
   */
  public boolean isPaused() {
    return isPaused;
  }

  @Override
  public void dispose() {
    super.dispose();
    // Cancel any scheduled tasks to prevent memory leaks
    if (spawnTask != null) {
      spawnTask.cancel();
      spawnTask = null;
    }
    if (pauseTask != null) {
      pauseTask.cancel();
      pauseTask = null;
    }
  }
}

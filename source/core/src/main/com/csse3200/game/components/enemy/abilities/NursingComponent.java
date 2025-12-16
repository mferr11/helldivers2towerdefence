package com.csse3200.game.components.enemy.abilities;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.WaypointTrackerComponent;
import com.csse3200.game.entities.configs.EnemyConfig.NursingConfig;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that spawns smaller enemies when the entity dies. Used for nursing enemies that spawn
 * scavengers on death.
 */
public class NursingComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(NursingComponent.class);

  private final NursingConfig config;
  private boolean hasSpawned = false;

  public NursingComponent(NursingConfig config) {
    this.config = config;
  }

  @Override
  public void create() {
    super.create();

    // Listen for health updates to detect death
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
  }

  /** Called when the entity's health changes. Spawns scavengers if health reaches 0. */
  private void onHealthUpdate(int newHealth) {
    if (newHealth <= 0 && !hasSpawned) {
      hasSpawned = true;
      spawnScavengers();
    }
  }

  /** Triggers event to spawn 2-4 scavengers at the entity's death location. */
  private void spawnScavengers() {
    int spawnCount =
        config.minSpawnCount
            + (int) (Math.random() * (config.maxSpawnCount - config.minSpawnCount + 1));

    Vector2 deathPosition = entity.getPosition();
    WaypointTrackerComponent tracker = entity.getComponent(WaypointTrackerComponent.class);

    if (tracker == null) {
      logger.error("NursingComponent requires WaypointTrackerComponent");
      return;
    }

    int waypointIndex = tracker.getCurrentWaypoint();

    logger.info(
        "Nursing enemy died, requesting spawn of {} scavengers at position {} from waypoint {}",
        spawnCount,
        deathPosition,
        waypointIndex);

    // Trigger event for ForestGameArea to handle spawning
    ServiceLocator.getGameAreaEvents()
        .trigger("spawnNursingEnemies", spawnCount, deathPosition, waypointIndex);
  }
}

package com.csse3200.game.components.enemy.abilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.HealthBarComponent;
import com.csse3200.game.entities.configs.EnemyConfig.CloakConfig;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that makes an entity periodically invisible and invulnerable. Used for stealth enemies
 * like Stalkers.
 */
public class CloakComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CloakComponent.class);

  private final CloakConfig config;
  private boolean isCloaked = false;
  private boolean isPaused = false;
  private Timer.Task cloakTask;
  private Timer.Task idleTask;
  private Timer.Task pauseTask;
  private Vector2 originalSpeed;

  public CloakComponent(CloakConfig config) {
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

    // Start cloaked to simulate spawning anywhere on the path
    cloak();
    scheduleInitialUncloak();
  }

  /** Schedule the initial uncloak after a random cloak duration */
  private void scheduleInitialUncloak() {
    float cloakDuration =
        config.minCloakTime + (float) Math.random() * (config.maxCloakTime - config.minCloakTime);

    cloakTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                uncloak();
                schedulePostUncloakPause();
              }
            },
            cloakDuration);
  }

  /** Schedule the next cloak period with random duration */
  private void scheduleCloak() {
    float cloakDuration =
        config.minCloakTime + (float) Math.random() * (config.maxCloakTime - config.minCloakTime);

    cloakTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                uncloak();
                schedulePostUncloakPause();
              }
            },
            cloakDuration);

    cloak();
  }

  /** Schedule the pause period after uncloaking */
  private void schedulePostUncloakPause() {
    pauseTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                resumeMovement();
                scheduleIdle();
              }
            },
            config.pauseTime);

    pauseMovement();
  }

  /** Schedule the pause period before cloaking */
  private void schedulePreCloakPause() {
    pauseTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                resumeMovement();
                scheduleCloak();
              }
            },
            config.pauseTime);

    pauseMovement();
  }

  /** Schedule the idle period between cloaks with random duration */
  private void scheduleIdle() {
    float idleDuration =
        config.minIdleTime + (float) Math.random() * (config.maxIdleTime - config.minIdleTime);

    idleTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                schedulePreCloakPause();
              }
            },
            idleDuration);
  }

  /** Activate cloaking - makes entity semi-transparent and invulnerable */
  private void cloak() {
    isCloaked = true;

    // Make entity invisible
    TextureRenderComponentAlpha renderer = entity.getComponent(TextureRenderComponentAlpha.class);
    if (renderer != null) {
      renderer.setAlphaValue(0f);
    }

    // Hide health bar
    HealthBarComponent healthBar = entity.getComponent(HealthBarComponent.class);
    if (healthBar != null) {
      healthBar.setVisible(false);
    }

    // Increase speed while cloaked
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null && originalSpeed != null) {
      movement.setMaxSpeed(new Vector2(originalSpeed.x * 1.5f, originalSpeed.y * 1.5f));
    }

    // Trigger event so other components (like CombatStatsComponent) can respond
    entity.getEvents().trigger("cloakActivated");
    logger.debug("Entity {} cloaked", entity);
  }

  /** Deactivate cloaking - restores visibility and vulnerability */
  private void uncloak() {
    isCloaked = false;

    // Restore full opacity
    TextureRenderComponentAlpha renderer = entity.getComponent(TextureRenderComponentAlpha.class);
    if (renderer != null) {
      renderer.setAlphaValue(1.0f);
    }

    // Show health bar
    HealthBarComponent healthBar = entity.getComponent(HealthBarComponent.class);
    if (healthBar != null) {
      healthBar.setVisible(true);
    }

    entity.getEvents().trigger("cloakDeactivated");
    logger.debug("Entity {} uncloaked", entity);
  }

  /** Pause entity movement */
  private void pauseMovement() {
    isPaused = true;

    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null) {
      movement.setMaxSpeed(Vector2.Zero);
    }

    logger.debug("Entity {} paused", entity);
  }

  /** Resume entity movement to original speed */
  private void resumeMovement() {
    isPaused = false;

    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    if (movement != null && originalSpeed != null) {
      movement.setMaxSpeed(originalSpeed.cpy());
    }

    logger.debug("Entity {} resumed movement", entity);
  }

  /**
   * @return true if the entity is currently cloaked
   */
  public boolean isCloaked() {
    return isCloaked;
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
    if (cloakTask != null) {
      cloakTask.cancel();
    }
    if (idleTask != null) {
      idleTask.cancel();
    }
    if (pauseTask != null) {
      pauseTask.cancel();
    }
  }
}

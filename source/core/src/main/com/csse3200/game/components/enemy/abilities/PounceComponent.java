package com.csse3200.game.components.enemy.abilities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.ServiceLocator;

public class PounceComponent extends Component {
  private float minPounceCooldown;
  private float maxPounceCooldown;
  private float currentCooldown;
  private float pounceDuration;
  private float preparationDuration;
  private boolean isPreparing;
  private boolean isPouncing;
  private float pounceTimer;
  private float preparationTimer;
  private float minSpeedMultiplier;
  private float maxSpeedMultiplier;
  private float currentSpeedMultiplier;

  // Store the original speed
  private Vector2 originalSpeed;

  /** Creates a pounce component with fixed cooldown and speed multiplier */
  public PounceComponent(float pounceCooldown, float pounceDuration) {
    this(pounceCooldown, pounceCooldown, pounceDuration, 3f, 3f, 0.5f);
  }

  /** Creates a pounce component with fixed cooldown and speed multiplier */
  public PounceComponent(
      float pounceCooldown, float pounceDistance, float pounceDuration, float speedMultiplier) {
    this(pounceCooldown, pounceCooldown, pounceDuration, speedMultiplier, speedMultiplier, 0.5f);
  }

  /**
   * Creates a pounce component with randomized cooldown and speed multiplier
   *
   * @param minPounceCooldown Minimum time between pounces
   * @param maxPounceCooldown Maximum time between pounces
   * @param pounceDistance Distance of the pounce (currently unused)
   * @param pounceDuration How long the pounce lasts
   * @param minSpeedMultiplier Minimum speed boost multiplier
   * @param maxSpeedMultiplier Maximum speed boost multiplier
   * @param preparationDuration How long the hunter stops before pouncing
   */
  public PounceComponent(
      float minPounceCooldown,
      float maxPounceCooldown,
      float pounceDuration,
      float minSpeedMultiplier,
      float maxSpeedMultiplier,
      float preparationDuration) {
    this.minPounceCooldown = minPounceCooldown;
    this.maxPounceCooldown = maxPounceCooldown;
    this.pounceDuration = pounceDuration;
    this.preparationDuration = preparationDuration;
    this.minSpeedMultiplier = minSpeedMultiplier;
    this.maxSpeedMultiplier = maxSpeedMultiplier;

    // Start with a random cooldown
    this.currentCooldown = MathUtils.random(minPounceCooldown, maxPounceCooldown);
    this.isPreparing = false;
    this.isPouncing = false;
  }

  @Override
  public void create() {
    super.create();
    // Store the original speed when component is created
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    this.originalSpeed = movement.getMaxSpeed().cpy();
  }

  @Override
  public void update() {
    float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();

    if (isPreparing) {
      // Preparation phase - hunter is stopped
      preparationTimer -= deltaTime;
      if (preparationTimer <= 0) {
        endPreparation();
      }
    } else if (isPouncing) {
      // Pouncing phase - hunter is moving fast
      pounceTimer -= deltaTime;
      if (pounceTimer <= 0) {
        endPounce();
      }
    } else {
      // Cooldown phase - waiting to pounce
      currentCooldown -= deltaTime;
      if (currentCooldown <= 0) {
        startPreparation();
      }
    }
  }

  /** Start the preparation phase - hunter stops moving */
  private void startPreparation() {
    isPreparing = true;
    preparationTimer = preparationDuration;

    // Stop the hunter by setting speed to zero (don't disable movement)
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    movement.setMaxSpeed(Vector2.Zero);
  }

  /** End the preparation phase and start the pounce */
  private void endPreparation() {
    isPreparing = false;
    startPounce();
  }

  /** Start the pounce - hunter moves very fast */
  private void startPounce() {
    isPouncing = true;
    pounceTimer = pounceDuration;

    // Randomize the speed multiplier for this pounce
    currentSpeedMultiplier = MathUtils.random(minSpeedMultiplier, maxSpeedMultiplier);

    // Boost speed temporarily
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    Vector2 pounceSpeed = originalSpeed.cpy().scl(currentSpeedMultiplier);
    movement.setMaxSpeed(pounceSpeed);
  }

  /** End the pounce and return to normal */
  private void endPounce() {
    isPouncing = false;

    // Randomize the next cooldown
    currentCooldown = MathUtils.random(minPounceCooldown, maxPounceCooldown);

    // Reset to original speed
    PhysicsMovementComponent movement = entity.getComponent(PhysicsMovementComponent.class);
    movement.setMaxSpeed(originalSpeed.cpy());
  }

  public boolean isPouncing() {
    return isPouncing;
  }

  public boolean isPreparing() {
    return isPreparing;
  }

  public float getCurrentSpeedMultiplier() {
    return currentSpeedMultiplier;
  }
}

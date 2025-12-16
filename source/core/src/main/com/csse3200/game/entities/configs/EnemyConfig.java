package com.csse3200.game.entities.configs;

public class EnemyConfig {
  public int health = 30;
  public int baseAttack = 10;
  public float speed = 2f;
  public int baseArmourRating = 0;
  public int goldAmount = 250;
  public float clickRadius = 0.5f;
  public String texturePath = "images/ghost_1.png";

  // Pounce ability configuration
  public PounceConfig pounce;

  // Configuration for the pounce ability //
  public static class PounceConfig {
    public float minCooldown = 3f;
    public float maxCooldown = 7f;
    public float duration = 0.5f;
    public float minSpeedMultiplier = 2.5f;
    public float maxSpeedMultiplier = 4f;
    public float preparationDuration = 0.5f;
  }

  // Cloak ability configuration
  public CloakConfig cloak;

  // Configuration for the cloak ability //
  public static class CloakConfig {
    public float minCloakTime = 3f;
    public float maxCloakTime = 5f;
    public float minIdleTime = 4f;
    public float maxIdleTime = 7f;
    public float pauseTime = 1f;
  }

  // Nursing ability configuration
  public NursingConfig nursing;

  // Configuration for the nursing ability //
  public static class NursingConfig {
    public int minSpawnCount = 2;
    public int maxSpawnCount = 4;
  }
}

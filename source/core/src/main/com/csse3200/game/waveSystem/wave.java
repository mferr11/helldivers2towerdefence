package com.csse3200.game.waveSystem;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory.EnemyType;
import java.util.List;

/** Represents a wave of enemies with configurable composition and multiple possible paths. */
public class Wave {
  private final int waveNumber;
  private final boolean isBossWave;
  private final List<EnemyType> enemies;
  private final float spawnRate;
  private int currentEnemyIndex = 0; // Track which enemy to spawn next

  public Wave(
      int waveNumber,
      boolean isBossWave,
      float spawnRate,
      List<EnemyType> enemies,
      List<Entity> waypoints) {
    this.waveNumber = waveNumber;
    this.enemies = enemies;
    this.isBossWave = isBossWave;
    this.spawnRate = spawnRate;
  }

  /** Returns whether this wave is a boss wave. */
  public boolean isBossWave() {
    return isBossWave;
  }

  /** Gets the total number of enemies in this wave. */
  public int getTotalEnemies() {
    return enemies.size();
  }

  /** Gets the current wave number. */
  public int getWaveNumber() {
    return waveNumber;
  }

  /** Gets the wave spawn rate. */
  public float getSpawnRate() {
    return spawnRate;
  }

  /** Gets the next enemy type to spawn and advances the index. */
  public EnemyType getNextEnemy() {
    if (currentEnemyIndex < enemies.size()) {
      return enemies.get(currentEnemyIndex++);
    }
    return null;
  }

  /** Resets the spawn index (useful when starting a new wave). */
  public void reset() {
    currentEnemyIndex = 0;
  }
}

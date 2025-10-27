package com.csse3200.game.waveSystem;

import com.csse3200.game.entities.Entity;

import java.util.List;

/**
 * Represents a wave of enemies with configurable composition and multiple possible paths.
 */
public class Wave {
    private final int waveNumber;
    private final boolean isBossWave;
    private final int numberOfEnemies;
    private final float spawnRate; // Enemies per second
    private final List<Entity> waypoints;

    public Wave(int waveNumber, boolean isBossWave, int numberOfEnemies, float spawnRate, List<Entity> waypoints) {
        this.waveNumber = waveNumber;
        this.isBossWave = isBossWave;
        this.numberOfEnemies = numberOfEnemies;
        this.spawnRate = spawnRate;
        this.waypoints = waypoints;
    }

    /**
     * Returns whether this wave is a boss wave.
     */
    public boolean isBossWave() {
        return isBossWave;
    }

    /**
     * Gets the total number of enemies in this wave.
     */
    public int getTotalEnemies() {
        return numberOfEnemies;
    }

    /**
     * Gets the current wave number.
     */
    public int getWaveNumber() { 
        return waveNumber;
    }

    /**
     * Gets the wave spawn rate.
     */
    public float getSpawnRate() { 
        return spawnRate;
    }
}

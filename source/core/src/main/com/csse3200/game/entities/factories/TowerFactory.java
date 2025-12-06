package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.EnemyClickableComponent;
import com.csse3200.game.components.towers.TowerPreviewComponent;
import com.csse3200.game.components.towers.TowerRangeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;

/**
 * Factory class to create tower entities with predefined components and configurations.
 * This factory provides methods to instantiate towers with default values for combat,
 * rendering, and interaction components.
 */
public class TowerFactory {

    private static int DEFAULT_HEALTH = 50;
    private static int DEFAULT_DAMAGE = 3;
    private static int DEFAULT_ARMOUR_RATING = 0;
    private static float DEFAULT_TOWER_RADIUS = 4f;
    private static float DEFAULT_ATTACK_COOLDOWN = 0.2f;
    
    private static float DEFAULT_CLICK_RADIUS = 0.7f;
    private static String DEFAULT_TEXTURE_PATH = "images/tree.png";
    private static float PREVIEW_OPACITY = 0.5f;

    public static Entity createTowerPreview() {
        Entity towerPreview = new Entity()
        .addComponent(new TowerPreviewComponent())
        .addComponent(new TextureRenderComponentAlpha(DEFAULT_TEXTURE_PATH, PREVIEW_OPACITY));

        return towerPreview;
    }

    /**
     * Creates a basic tower entity with default components and statistics.
     * The tower includes:
     * <ul>
     *   <li>Texture rendering component</li>
     *   <li>Clickable component for user interaction</li>
     *   <li>Tower range component for attack behavior</li>
     *   <li>Combat statistics component</li>
     * </ul>
     *
     * @return a new Entity configured as a base tower
     */
    public static Entity createBaseTower() {
        CombatStatsComponent combatStats = new CombatStatsComponent(
            DEFAULT_HEALTH, 
            DEFAULT_DAMAGE, 
            DEFAULT_ARMOUR_RATING
        );

        Entity baseTower = new Entity()
            .addComponent(new TextureRenderComponent(DEFAULT_TEXTURE_PATH))
            //.addComponent(new EnemyClickableComponent(DEFAULT_CLICK_RADIUS))
            .addComponent(new TowerRangeComponent(
                DEFAULT_TOWER_RADIUS, 
                DEFAULT_ATTACK_COOLDOWN, 
                combatStats
            ));

        return baseTower;
    }
    
    /**
     * Gets the default health value for towers.
     *
     * @return the default health
     */
    public static int getDefaultHealth() {
        return DEFAULT_HEALTH;
    }
    
    /**
     * Sets the default health value for towers.
     *
     * @param health the new default health
     */
    public static void setDefaultHealth(int health) {
        DEFAULT_HEALTH = health;
    }
    
    /**
     * Gets the default damage value for towers.
     *
     * @return the default damage
     */
    public static int getDefaultDamage() {
        return DEFAULT_DAMAGE;
    }
    
    /**
     * Sets the default damage value for towers.
     *
     * @param damage the new default damage
     */
    public static void setDefaultDamage(int damage) {
        DEFAULT_DAMAGE = damage;
    }
    
    /**
     * Gets the default armour rating for towers.
     *
     * @return the default armour rating
     */
    public static int getDefaultArmourRating() {
        return DEFAULT_ARMOUR_RATING;
    }
    
    /**
     * Sets the default armour rating for towers.
     *
     * @param armourRating the new default armour rating
     */
    public static void setDefaultArmourRating(int armourRating) {
        DEFAULT_ARMOUR_RATING = armourRating;
    }
    
    /**
     * Gets the default tower attack radius.
     *
     * @return the default tower radius
     */
    public static float getDefaultTowerRadius() {
        return DEFAULT_TOWER_RADIUS;
    }
    
    /**
     * Sets the default tower attack radius.
     *
     * @param radius the new default tower radius
     */
    public static void setDefaultTowerRadius(float radius) {
        DEFAULT_TOWER_RADIUS = radius;
    }
    
    /**
     * Gets the default attack cooldown for towers.
     *
     * @return the default attack cooldown in seconds
     */
    public static float getDefaultAttackCooldown() {
        return DEFAULT_ATTACK_COOLDOWN;
    }
    
    /**
     * Sets the default attack cooldown for towers.
     *
     * @param cooldown the new default attack cooldown in seconds
     */
    public static void setDefaultAttackCooldown(float cooldown) {
        DEFAULT_ATTACK_COOLDOWN = cooldown;
    }
    
    /**
     * Gets the default click detection radius.
     *
     * @return the default click radius
     */
    public static float getDefaultClickRadius() {
        return DEFAULT_CLICK_RADIUS;
    }
    
    /**
     * Sets the default click detection radius.
     *
     * @param radius the new default click radius
     */
    public static void setDefaultClickRadius(float radius) {
        DEFAULT_CLICK_RADIUS = radius;
    }
    
    /**
     * Gets the default texture path for towers.
     *
     * @return the default texture path
     */
    public static String getDefaultTexturePath() {
        return DEFAULT_TEXTURE_PATH;
    }
    
    /**
     * Sets the default texture path for towers.
     *
     * @param path the new default texture path
     */
    public static void setDefaultTexturePath(String path) {
        DEFAULT_TEXTURE_PATH = path;
    }
}
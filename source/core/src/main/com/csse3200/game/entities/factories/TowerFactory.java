package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.ClickableComponent;
import com.csse3200.game.components.towers.TowerRangeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create tower entities with predefined components.
 */
public class TowerFactory {

    // Combat Stuff
    private static int DEFUALT_HEALTH = 50;
    private static int DEFAULT_DAMAGE = 5;
    private static int DEFAULT_ARMOUR_RATING = 0;
    private static float DEFAULT_TOWER_RADIUS = 4f;
    private static float DEFAULT_ATTACK_COOLDOWN = 0.2f;

    // Misc
    private static float DEFAULT_CLICK_RADIUS = 0.7f;
    private static String DEFAULT_TEXTURE_PATH = "images/tree.png";

    public static Entity createBaseTower() {
        CombatStatsComponent combatStats = new CombatStatsComponent(DEFUALT_HEALTH, DEFAULT_DAMAGE, DEFAULT_ARMOUR_RATING);

        Entity baseTower = new Entity()
        .addComponent(new TextureRenderComponent(DEFAULT_TEXTURE_PATH))
        .addComponent(new ClickableComponent(DEFAULT_CLICK_RADIUS))
        .addComponent(new TowerRangeComponent(DEFAULT_TOWER_RADIUS, DEFAULT_ATTACK_COOLDOWN, combatStats));

        return baseTower;
    }
}

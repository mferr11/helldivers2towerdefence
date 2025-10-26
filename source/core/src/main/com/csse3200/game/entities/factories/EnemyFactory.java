package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
//import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

import java.util.List;

/**
 * Factory to create enemy entities with predefined components.
 */
public class EnemyFactory {

    private static int DEFUALT_HEALTH = 50;
    private static int DEFAULT_DAMAGE = 10;
    private static float DEFAULT_CLICK_RADIUS = 0.7f;
    private static String DEFAULT_TEXTURE_PATH = "images/ghost_1.png";

    //public static Entity createBaseEnemy(List<Entity> waypoints, AnimationRenderComponent animations) {
    public static Entity createBaseEnemy(List<Entity> waypoints) {

        AITaskComponent aiComponent = new AITaskComponent()
            .addTask(new ChaseTask(waypoints.get(0), 1, 100f, 100f));

        Entity baseEnemy = new Entity()
            .addComponent(new clickable(DEFAULT_CLICK_RADIUS))
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new HitboxComponent())
            .addComponent(new CombatStatsComponent(DEFUALT_HEALTH, DEFAULT_DAMAGE))
            .addComponent(aiComponent)
            // TextureRenderComponent is placeholder until I implement animations
            .addComponent(new TextureRenderComponent(DEFAULT_TEXTURE_PATH));
            // .addComponent(animations);

    baseEnemy.getEvents().addListener("updateHealth", (EventListener1<Integer>) (health) -> takeDamage(baseEnemy, health));
    return baseEnemy;
    }

    private static void destroyEnemy(Entity enemy) {

        // Final thing to call
        Gdx.app.postRunnable(() -> {
            enemy.dispose();
        });
    }

    private static void takeDamage(Entity enemy, int newHealth) {
        System.out.println("New Health: " + newHealth);
        if (newHealth <= 0) {
            destroyEnemy(enemy);
        }
    }

}

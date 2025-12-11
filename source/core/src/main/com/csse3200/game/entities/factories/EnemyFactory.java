package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.WaypointTrackerComponent;
import com.csse3200.game.components.enemy.EnemyClickableComponent;
import com.csse3200.game.components.enemy.EnemyComponent;
import com.csse3200.game.components.enemy.HealthBarComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
//import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;

/**
 * Factory to create enemy entities with predefined components.
 */
public class EnemyFactory {

    // Combat Stuff
    private static int DEFUALT_HEALTH = 30;
    private static int DEFAULT_DAMAGE = 10;
    private static float DEFAULT_SPEED = 2f;
    private static int DEFAULT_ARMOUR_RATING = 0;
    private static int DEFAULT_GOLD_AMOUNT = 250;

    // Misc
    private static float DEFAULT_CLICK_RADIUS = 0f;
    private static String DEFAULT_TEXTURE_PATH = "images/ghost_1.png";

    /**
     * Creates a base enemy entity with default components and behavior.
     * The enemy will follow the provided waypoints in order, and can be clicked to take damage.
     * When the enemy's health reaches zero, it will be destroyed.
     * 
     * @param waypoints List of waypoint entities for the enemy to follow in sequence
     * @return A fully configured enemy entity with physics, combat stats, AI, and event listeners
     */
    public static Entity createBaseEnemy(List<Entity> waypoints) {
        WaypointTrackerComponent waypointTracker = new WaypointTrackerComponent(waypoints);

        AITaskComponent aiComponent = new AITaskComponent()
            .addTask(new ChaseTask(
                waypointTracker.getCurrentWaypointEntity(),
                waypointTracker.getCurrentPriority(),
                100f, 100f));

        Entity baseEnemy = new Entity()
            .addComponent(new EnemyComponent())
            .addComponent(new EnemyClickableComponent(DEFAULT_CLICK_RADIUS))
            .addComponent(new HealthBarComponent())
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new HitboxComponent())
            .addComponent(new CombatStatsComponent(DEFUALT_HEALTH, DEFAULT_DAMAGE, DEFAULT_ARMOUR_RATING))
            .addComponent(waypointTracker)
            .addComponent(aiComponent)
            // TextureRenderComponent is placeholder until I implement animations
            .addComponent(new TextureRenderComponent(DEFAULT_TEXTURE_PATH));

        baseEnemy.getComponent(PhysicsMovementComponent.class).setMaxSpeed(new Vector2(DEFAULT_SPEED, DEFAULT_SPEED));

        baseEnemy.getEvents().addListener("updateHealth", (EventListener1<Integer>) (health) -> takeDamage(baseEnemy, health));
        baseEnemy.getEvents().addListener("finishedChaseTask", () -> updateWaypointTarget(baseEnemy));
        return baseEnemy;
    }

    /**
     * Updates the enemy's target to the next waypoint in its path.
     * Advances the waypoint tracker and assigns a new chase task to the enemy's AI.
     * If the enemy has reached the end of the waypoint list, logs a message and does not add a new task.
     * 
     * @param baseEnemy The enemy entity to update
     */
    private static void updateWaypointTarget(Entity baseEnemy) {
        WaypointTrackerComponent tracker = baseEnemy.getComponent(WaypointTrackerComponent.class);
        
        if (tracker.advanceWaypoint()) {
            baseEnemy.getComponent(AITaskComponent.class)
                .addTask(new ChaseTask(
                    tracker.getCurrentWaypointEntity(), 
                    tracker.getCurrentPriority(), 
                    100, 100));
        } else {
            if (!tracker.getFinished()) {
                // Reached the end of the waypoint list, do stuff once here
                ServiceLocator.getGameAreaEvents().trigger("enemyreachedbase", DEFAULT_DAMAGE);
                tracker.setFinished(true); 
            }
        }
    }

    /**
     * Destroys an enemy entity and cleans up its resources.
     * The disposal is posted to run on the next frame to avoid concurrent modification issues.
     * 
     * @param enemy The enemy entity to destroy
     */
    private static void destroyEnemy(Entity enemy) {
        ServiceLocator.getGameAreaEvents().trigger("enemyKilled", DEFAULT_GOLD_AMOUNT);

        Gdx.app.postRunnable(() -> {
            enemy.dispose();
        });
    }

    /**
     * Handles damage taken by an enemy and checks if it should be destroyed.
     * Logs the new health value and destroys the enemy if health reaches zero or below.
     * 
     * @param enemy The enemy entity taking damage
     * @param newHealth The enemy's health after taking damage
     */
    private static void takeDamage(Entity enemy, int newHealth) {
        //System.out.println("New Health: " + newHealth);
        if (newHealth <= 0) {
            destroyEnemy(enemy);
        }
    }

}
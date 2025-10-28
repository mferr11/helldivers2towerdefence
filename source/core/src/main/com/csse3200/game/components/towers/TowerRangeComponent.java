package com.csse3200.game.components.towers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.EnemyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that handles tower attack range and target acquisition.
 * This component manages the tower's ability to detect, target, and attack enemies
 * within a specified range using various targeting strategies.
 */
public class TowerRangeComponent extends Component {
    
    private float attackRange;
    private float attackCooldown;
    private float currentCooldown;
    private Entity currentTarget;
    private CombatStatsComponent combatStat;
    private TargetingStrategy targetingStrategy;
    
    /**
     * Constructs a new TowerRangeComponent with specified attack parameters.
     *
     * @param attackRange the maximum distance at which the tower can attack
     * @param attackCooldown the time delay between attacks in seconds
     * @param combatStat the combat statistics component for damage calculations
     */
    public TowerRangeComponent(float attackRange, float attackCooldown, CombatStatsComponent combatStat) {
        this.combatStat = combatStat;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.currentCooldown = 0f;
        this.targetingStrategy = TargetingStrategy.CLOSEST;
    }
    
    /**
     * Updates the tower's targeting and attack behavior each frame.
     * Decrements the cooldown, validates the current target, acquires new targets,
     * and fires at targets when possible.
     */
    @Override
    public void update() {
        if (currentCooldown > 0) {
            currentCooldown -= ServiceLocator.getTimeSource().getDeltaTime();
        }
        
        if (currentTarget != null && !isValidTarget(currentTarget)) {
            currentTarget = null;
        }
        
        if (canAttack() && currentTarget == null) {
            currentTarget = acquireTarget();
        }
        
        if (currentTarget != null && canAttack()) {
            currentTarget = acquireTarget();
            fireAtTarget(currentTarget);
            resetCooldown();
        }
    }
    
    /**
     * Retrieves all enemy entities within the tower's attack range.
     * Uses squared distance calculations for performance optimization.
     *
     * @return an array of enemy entities within range
     */
    public Array<Entity> getEnemiesInRange() {
        Array<Entity> enemiesInRange = new Array<>();
        Vector2 towerPos = entity.getPosition();
        float rangeSquared = attackRange * attackRange;
        
        // Get all entities with enemy tag/component
        EntityService entityService = ServiceLocator.getEntityService();
        Array<Entity> allEnemies = entityService.getAllEntitiesWithComponent(EnemyComponent.class);
        
        for (Entity enemy : allEnemies) {
            float distSquared = towerPos.dst2(enemy.getPosition());
            if (distSquared <= rangeSquared) {
                enemiesInRange.add(enemy);
            }
        }
        
        return enemiesInRange;
    }
    
    /**
     * Acquires a target based on the current targeting strategy.
     * Returns null if no enemies are in range.
     *
     * @return the selected target entity, or null if no valid targets exist
     */
    private Entity acquireTarget() {
        Array<Entity> enemies = getEnemiesInRange();
        
        if (enemies.size == 0) {
            return null;
        }
        
        return switch (targetingStrategy) {
            case CLOSEST -> findClosestEnemy(enemies);
            case FIRST -> findFirstEnemy(enemies);
            case STRONGEST -> findStrongestEnemy(enemies);
            case WEAKEST -> findWeakestEnemy(enemies);
        };
    }
    
    /**
     * Finds the closest enemy from a list of enemies.
     * Uses squared distance for performance optimization.
     *
     * @param enemies array of enemy entities to search
     * @return the closest enemy entity
     */
    private Entity findClosestEnemy(Array<Entity> enemies) {
        Entity closest = null;
        float minDistSquared = Float.MAX_VALUE;
        Vector2 towerPos = entity.getPosition();
        
        for (Entity enemy : enemies) {
            float distSquared = towerPos.dst2(enemy.getPosition());
            if (distSquared < minDistSquared) {
                minDistSquared = distSquared;
                closest = enemy;
            }
        }
        
        return closest;
    }
    
    /**
     * Finds the first enemy in the provided array.
     * This strategy can be used for simple first-come-first-served targeting.
     *
     * @param enemies array of enemy entities
     * @return the first enemy in the array
     */
    private Entity findFirstEnemy(Array<Entity> enemies) {
        return enemies.first();
    }
    
    /**
     * Finds the enemy with the highest health from a list of enemies.
     *
     * @param enemies array of enemy entities to search
     * @return the enemy with the most health, or null if none found
     */
    private Entity findStrongestEnemy(Array<Entity> enemies) {
        Entity strongest = null;
        int maxHealth = 0;
        
        for (Entity enemy : enemies) {
            CombatStatsComponent combat = enemy.getComponent(CombatStatsComponent.class);
            if (combat != null && combat.getHealth() > maxHealth) {
                maxHealth = combat.getHealth();
                strongest = enemy;
            }
        }
        
        return strongest;
    }
    
    /**
     * Finds the enemy with the lowest health from a list of enemies.
     *
     * @param enemies array of enemy entities to search
     * @return the enemy with the least health, or null if none found
     */
    private Entity findWeakestEnemy(Array<Entity> enemies) {
        Entity weakest = null;
        int minHealth = Integer.MAX_VALUE;
        
        for (Entity enemy : enemies) {
            CombatStatsComponent combat = enemy.getComponent(CombatStatsComponent.class);
            if (combat != null && combat.getHealth() < minHealth) {
                minHealth = combat.getHealth();
                weakest = enemy;
            }
        }
        
        return weakest;
    }
    
    /**
     * Checks if a target entity is valid for attacking.
     * A target is valid if it exists, is not dead, and is within attack range.
     *
     * @param target the entity to validate
     * @return true if the target is valid, false otherwise
     */
    private boolean isValidTarget(Entity target) {
        if (target == null) return false;
        
        CombatStatsComponent combat = target.getComponent(CombatStatsComponent.class);
        if (combat == null || combat.isDead()) {
            return false;
        }
        
        float distSquared = entity.getPosition().dst2(target.getPosition());
        return distSquared <= attackRange * attackRange;
    }
    
    /**
     * Fires an attack at the specified target.
     * Applies damage using the tower's combat statistics.
     *
     * @param target the entity to attack
     */
    private void fireAtTarget(Entity target) {
        target.getComponent(CombatStatsComponent.class).hit(combatStat);
        //entity.getEvents().trigger("attackTarget", target);
    }
    
    /**
     * Checks if the tower is ready to attack (cooldown has expired).
     *
     * @return true if the tower can attack, false otherwise
     */
    private boolean canAttack() {
        return currentCooldown <= 0;
    }
    
    /**
     * Resets the attack cooldown to its initial value.
     */
    private void resetCooldown() {
        currentCooldown = attackCooldown;
    }
    
    /**
     * Gets the tower's attack range.
     *
     * @return the attack range
     */
    public float getAttackRange() { 
        return attackRange; 
    }
    
    /**
     * Sets the tower's attack range.
     *
     * @param range the new attack range
     */
    public void setAttackRange(float range) { 
        this.attackRange = range; 
    }
    
    /**
     * Gets the tower's attack cooldown duration.
     *
     * @return the attack cooldown in seconds
     */
    public float getAttackCooldown() {
        return attackCooldown;
    }
    
    /**
     * Sets the tower's attack cooldown duration.
     *
     * @param cooldown the new attack cooldown in seconds
     */
    public void setAttackCooldown(float cooldown) {
        this.attackCooldown = cooldown;
    }
    
    /**
     * Gets the remaining cooldown time before the next attack.
     *
     * @return the current cooldown in seconds
     */
    public float getCurrentCooldown() {
        return currentCooldown;
    }
    
    /**
     * Sets the current cooldown time.
     *
     * @param cooldown the cooldown time in seconds
     */
    public void setCurrentCooldown(float cooldown) {
        this.currentCooldown = cooldown;
    }
    
    /**
     * Gets the entity currently being targeted by the tower.
     *
     * @return the current target entity, or null if no target
     */
    public Entity getCurrentTarget() { 
        return currentTarget; 
    }
    
    /**
     * Sets the current target entity.
     *
     * @param target the entity to target
     */
    public void setCurrentTarget(Entity target) {
        this.currentTarget = target;
    }
    
    /**
     * Gets the combat statistics component used for attacks.
     *
     * @return the combat stats component
     */
    public CombatStatsComponent getCombatStat() {
        return combatStat;
    }
    
    /**
     * Sets the combat statistics component for attacks.
     *
     * @param combatStat the new combat stats component
     */
    public void setCombatStat(CombatStatsComponent combatStat) {
        this.combatStat = combatStat;
    }
    
    /**
     * Gets the current targeting strategy.
     *
     * @return the targeting strategy
     */
    public TargetingStrategy getTargetingStrategy() {
        return targetingStrategy;
    }
    
    /**
     * Sets the targeting strategy for enemy selection.
     *
     * @param strategy the new targeting strategy
     */
    public void setTargetingStrategy(TargetingStrategy strategy) { 
        this.targetingStrategy = strategy; 
    }
    
    /**
     * Enumeration of available targeting strategies for tower attacks.
     */
    public enum TargetingStrategy {
        /** Target the closest enemy */
        CLOSEST,
        
        /** Target the first enemy in the list */
        FIRST,
        
        /** Target the enemy with the most health */
        STRONGEST,
        
        /** Target the enemy with the least health */
        WEAKEST
    }
}
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
 */
public class TowerRangeComponent extends Component {
    
    private float attackRange;
    private float attackCooldown;
    private float currentCooldown;
    private Entity currentTarget;
    private CombatStatsComponent combatStat;
    private TargetingStrategy targetingStrategy;
    
    public TowerRangeComponent(float attackRange, float attackCooldown, CombatStatsComponent combatStat) {
        this.combatStat = combatStat;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.currentCooldown = 0f;
        this.targetingStrategy = TargetingStrategy.CLOSEST;
    }
    
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
     * Gets enemies within range by checking all enemies.
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
    
    private Entity findFirstEnemy(Array<Entity> enemies) {
        return enemies.first();
    }
    
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
    
    private boolean isValidTarget(Entity target) {
        if (target == null) return false;
        
        CombatStatsComponent combat = target.getComponent(CombatStatsComponent.class);
        if (combat == null || combat.isDead()) {
            return false;
        }
        
        float distSquared = entity.getPosition().dst2(target.getPosition());
        return distSquared <= attackRange * attackRange;
    }
    
    private void fireAtTarget(Entity target) {
        target.getComponent(CombatStatsComponent.class).hit(combatStat);
        //entity.getEvents().trigger("attackTarget", target);
    }
    
    private boolean canAttack() {
        return currentCooldown <= 0;
    }
    
    private void resetCooldown() {
        currentCooldown = attackCooldown;
    }
    
    public float getAttackRange() { return attackRange; }
    public void setAttackRange(float range) { this.attackRange = range; }
    public Entity getCurrentTarget() { return currentTarget; }
    public void setTargetingStrategy(TargetingStrategy strategy) { 
        this.targetingStrategy = strategy; 
    }
    
    public enum TargetingStrategy {
        CLOSEST, FIRST, STRONGEST, WEAKEST
    }
}
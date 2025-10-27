package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.rendering.RenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that renders a health bar above an entity.
 * The health bar shows current health as a percentage of max health.
 */
public class HealthBarComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HealthBarComponent.class);
    
    private static final float DEFAULT_WIDTH = 0.75f;
    private static final float DEFAULT_HEIGHT = 0.15f;
    private static final float DEFAULT_OFFSET_Y = 0.8f;
    
    private static final Color HEALTH_BAR_BACKGROUND = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    private static final Color HEALTH_BAR_FULL = new Color(0.2f, 0.8f, 0.2f, 0.9f);
    private static final Color HEALTH_BAR_MEDIUM = new Color(1.0f, 0.8f, 0.0f, 0.9f);
    private static final Color HEALTH_BAR_LOW = new Color(0.8f, 0.2f, 0.2f, 0.9f);
    
    private float width;
    private float height;
    private float offsetY;
    
    private CombatStatsComponent combatStats;
    private int maxHealth;
    private int currentHealth;
    private boolean isVisible = true;
    private ShapeRenderer shapeRenderer;
    
    public HealthBarComponent() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_OFFSET_Y);
    }
    
    public HealthBarComponent(float width, float height, float offsetY) {
        this.width = width;
        this.height = height;
        this.offsetY = offsetY;
    }
    
    @Override
    public void create() {
        super.create();
        
        shapeRenderer = new ShapeRenderer();
        
        combatStats = entity.getComponent(CombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HealthBarComponent requires CombatStatsComponent");
            return;
        }
        
        currentHealth = combatStats.getHealth();
        maxHealth = currentHealth;
        
        setMaxHealth(maxHealth);
        
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
    }
    
    private void onHealthUpdate(int newHealth) {
        currentHealth = newHealth;
    }
    

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        if (!isVisible || combatStats == null || maxHealth <= 0) {
            return;
        }
        
        float healthPercentage = Math.max(0f, Math.min(1f, (float) currentHealth / maxHealth));
        
        Vector2 entityPos = entity.getCenterPosition();
        if (entityPos == null) {
            return;
        }
        
        float barX = entityPos.x - width / 2f;
        float barY = entityPos.y + offsetY;
        
        batch.end();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(HEALTH_BAR_BACKGROUND);
        shapeRenderer.rect(barX, barY, width, height);
        
        Color healthColor = getHealthColor(healthPercentage);
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(barX, barY, width * healthPercentage, height);
        
        shapeRenderer.end();
        
        batch.begin();
    }
    
    private Color getHealthColor(float percentage) {
        if (percentage > 0.6f) {
            return HEALTH_BAR_FULL;
        } else if (percentage > 0.3f) {
            return HEALTH_BAR_MEDIUM;
        } else {
            return HEALTH_BAR_LOW;
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        logger.debug("HealthBarComponent disposed");
    }
}
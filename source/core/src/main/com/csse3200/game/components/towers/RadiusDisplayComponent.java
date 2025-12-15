package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

public class RadiusDisplayComponent extends RenderComponent { // Changed from Component
  private ShapeRenderer shapeRenderer;
  private boolean buildModeEnabled = false;
  private boolean cursorInBounds = true;
  private boolean isSelected = false;
  private float attackRadius;
  private static final Color PREVIEW_COLOR = new Color(1f, 1f, 1f, 0.2f);
  private static final Color SELECTED_COLOR = new Color(0f, 1f, 0f, 0.2f);
  private final boolean isPreview;

  // Constructor for preview towers
  public RadiusDisplayComponent(float initialRadius) {
    this.attackRadius = initialRadius;
    this.isPreview = true;
  }

  // Constructor for actual towers
  public RadiusDisplayComponent(float initialRadius, boolean isPreview) {
    this.attackRadius = initialRadius;
    this.isPreview = isPreview;
  }

  @Override
  public void create() {
    super.create();
    shapeRenderer = new ShapeRenderer();

    if (isPreview) {
      ServiceLocator.getGameAreaEvents()
          .addListener(
              "updateBuildMode",
              (EventListener1<Boolean>)
                  (newBuildMode) -> {
                    buildModeEnabled = newBuildMode;
                  });

      ServiceLocator.getGameAreaEvents()
          .addListener("updateTowerPreview", (EventListener1<TowerType>) this::updateRadius);
    } else {
      ServiceLocator.getGameAreaEvents()
          .addListener("towerClicked", (EventListener1<Entity>) this::handleTowerClick);
      ServiceLocator.getGameAreaEvents().addListener("deselectTower", () -> isSelected = false);
    }
  }

  private void updateRadius(TowerType towerType) {
    TowerConfig config = TowerFactory.getConfig(towerType);
    if (config != null) {
      attackRadius = config.attackRadius;
    }
  }

  private void handleTowerClick(Entity clickedTower) {
    isSelected = (clickedTower == entity);
  }

  public void updateCursorBounds(boolean inBounds) {
    cursorInBounds = inBounds;
  }

  @Override
  protected void draw(SpriteBatch batch) { // Changed signature to match RenderComponent
    boolean shouldDraw = false;
    Color colorToUse = PREVIEW_COLOR;

    if (isPreview) {
      shouldDraw = buildModeEnabled && cursorInBounds;
      colorToUse = PREVIEW_COLOR;
    } else {
      shouldDraw = isSelected;
      colorToUse = SELECTED_COLOR;
    }

    if (!shouldDraw) {
      return;
    }

    Camera camera = getCamera();
    if (camera == null) {
      return;
    }

    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    float centerX = position.x + (scale.x / 2);
    float centerY = position.y + (scale.y / 2);

    // End the SpriteBatch before using ShapeRenderer
    batch.end();

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(colorToUse);
    shapeRenderer.circle(centerX, centerY, attackRadius, 64);
    shapeRenderer.end();

    // Restart the SpriteBatch
    batch.begin();
  }

  private Camera getCamera() {
    Renderer renderer = Renderer.getCurrentRenderer();
    CameraComponent cam = renderer.getCamera();
    if (renderer != null && cam != null) {
      return cam.getCamera();
    }
    return null;
  }

  @Override
  public float getZIndex() {
    // Draw underneath textures by having a slightly lower z-index
    return -entity.getPosition().y - 0.1f;
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
    super.dispose();
  }
}

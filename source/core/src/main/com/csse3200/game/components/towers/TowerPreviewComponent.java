package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;
import com.csse3200.game.services.ServiceLocator;

public class TowerPreviewComponent extends Component {
  private boolean buildModeEnabled = false;
  private boolean cursorInBounds = true;
  private TextureRenderComponentAlpha alphaComp;
  private RadiusDisplayComponent radiusDisplay;

  @Override
  public void create() {
    super.create();
    alphaComp = entity.getComponent(TextureRenderComponentAlpha.class);
    radiusDisplay = entity.getComponent(RadiusDisplayComponent.class);
    updateVisibility();
    ServiceLocator.getGameAreaEvents()
        .addListener(
            "updateBuildMode",
            (EventListener1<Boolean>)
                (newBuildMode) -> {
                  buildModeEnabled = newBuildMode;
                  updateVisibility();
                });

    ServiceLocator.getGameAreaEvents()
        .addListener("updateTowerPreview", (EventListener1<TowerType>) this::updatePreviewTexture);
  }

  private void updatePreviewTexture(TowerType towerType) {
    // Get the config for the new tower type
    TowerConfig config = TowerFactory.getConfig(towerType);
    if (config != null && alphaComp != null) {
      alphaComp.setTexture(config.texturePath);
    }
  }

  private void updateVisibility() {
    // Only show preview if build mode is enabled AND cursor is in bounds
    if (buildModeEnabled && cursorInBounds) {
      alphaComp.setAlphaValue(0.2f);
    } else {
      alphaComp.setAlphaValue(0);
    }
  }

  @Override
  public void update() {
    int screenX = Gdx.input.getX();
    int screenY = Gdx.input.getY();

    Camera camera = getCamera();
    if (camera != null) {
      // Convert screen coordinates to world coordinates
      Vector3 worldClickPos = new Vector3(screenX, screenY, 0);
      camera.unproject(worldClickPos);

      // Update cursor bounds state
      boolean wasInBounds = cursorInBounds;
      cursorInBounds = worldClickPos.y >= 0;

      // Only update visibility if bounds state changed
      if (wasInBounds != cursorInBounds) {
        updateVisibility();
        if (radiusDisplay != null) {
          radiusDisplay.updateCursorBounds(cursorInBounds);
        }
      }

      entity.setPosition((int) worldClickPos.x, (int) worldClickPos.y);

      if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && buildModeEnabled && cursorInBounds) {
        GridPoint2 location = new GridPoint2((int) worldClickPos.x, (int) worldClickPos.y);
        ServiceLocator.getGameAreaEvents().trigger("towerPlacementClick", location);
        ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", false);
      }

      if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && buildModeEnabled) {
        ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", false);
      }
    }
  }

  private Camera getCamera() {
    Renderer renderer = Renderer.getCurrentRenderer();
    CameraComponent cam = renderer.getCamera();

    if (renderer != null && cam != null) {
      return cam.getCamera();
    }
    return null;
  }
}

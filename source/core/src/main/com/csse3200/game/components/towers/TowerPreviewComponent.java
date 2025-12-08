package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;
import com.csse3200.game.services.ServiceLocator;

public class TowerPreviewComponent extends Component {
    private Boolean shouldBeVisible = true;
    private TextureRenderComponentAlpha alphaComp;

    @Override
    public void create() {
        super.create();
        alphaComp = entity.getComponent(TextureRenderComponentAlpha.class);
        setVisibility(false);
    }

    public void setVisibility(boolean newVisibility) {
        if (shouldBeVisible) {
            if (newVisibility) {
                alphaComp.setAlphaValue(0.5f);
            } else {
                alphaComp.setAlphaValue(0);
            }
        } else {
            alphaComp.setAlphaValue(0);
            return;
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
            if (worldClickPos.y < 0) {
                setVisibility(false);
            } else {
                setVisibility(true);
            }

            entity.setPosition((int) worldClickPos.x, (int) worldClickPos.y);

            if (Gdx.input.justTouched()) {
                GridPoint2 location = new GridPoint2((int) worldClickPos.x, (int) worldClickPos.y);
                if (worldClickPos.y < 0) {
                    System.out.println("Tower placement out of bounds");
                    return;
                } else {
                    ServiceLocator.getGameAreaEvents().trigger("towerPlacementClick", location);
                }
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

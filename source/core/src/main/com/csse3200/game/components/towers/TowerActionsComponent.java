package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

public class TowerActionsComponent extends Component {
    private float clickRadius = 1f;
    
    // Static flag to track if any tower was clicked this frame
    private static boolean towerClickedThisFrame = false;

    public static void resetClickFlag() {
        towerClickedThisFrame = false;
    }
    
    public static boolean wasTowerClicked() {
        return towerClickedThisFrame;
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
            
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                Vector2 entityPos = entity.getPosition();
                Vector2 entityScale = entity.getScale();
                
                // Calculate center of the tower
                float centerX = entityPos.x + (entityScale.x / 2);
                float centerY = entityPos.y + (entityScale.y / 2);
                
                // Check distance from center
                float dx = worldClickPos.x - centerX;
                float dy = worldClickPos.y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance < clickRadius) {
                    System.out.println("Tower clicked!");
                    towerClickedThisFrame = true;
                    // Tower was clicked - trigger event with this tower entity
                    ServiceLocator.getGameAreaEvents().trigger("towerClicked", entity);
                } else {
                    ServiceLocator.getGameAreaEvents().trigger("deselectTower");
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
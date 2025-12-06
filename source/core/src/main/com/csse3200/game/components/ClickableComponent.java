package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

public class ClickableComponent extends Component{

    public ClickableComponent() {}
    
    @Override
    public void update() {
        if (Gdx.input.justTouched()) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
                        
            // Get camera from Renderer
            Camera camera = getCamera();
            if (camera != null) {
                // Convert screen coordinates to world coordinates
                Vector3 worldClickPos = new Vector3(screenX, screenY, 0);
                camera.unproject(worldClickPos);
                GridPoint2 location = new GridPoint2((int) worldClickPos.x * 2, (int) worldClickPos.y * 2);
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
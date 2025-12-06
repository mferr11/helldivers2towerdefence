package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.rendering.TextureRenderComponentAlpha;

public class TowerPreviewComponent extends Component {
    
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
                TextureRenderComponentAlpha alphaComp = entity.getComponent(TextureRenderComponentAlpha.class);
                alphaComp.setAlphaValue(0);
            } else {
                TextureRenderComponentAlpha alphaComp = entity.getComponent(TextureRenderComponentAlpha.class);
                alphaComp.setAlphaValue(0.5f);
            }

            entity.setPosition((int) worldClickPos.x, (int) worldClickPos.y);
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

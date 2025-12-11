package com.csse3200.game.components.towers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.TowerActionsUI;

public class DeselectHandlerComponent extends Component {
    @Override
    public void update() {
        TowerActionsComponent.resetClickFlag();
        
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
            
            boolean clickedOnUI = TowerActionsUI.isClickOnUI(screenX, screenY);
            boolean towerClicked = TowerActionsComponent.wasTowerClicked();
            
            if (!towerClicked && !clickedOnUI) {
                ServiceLocator.getGameAreaEvents().trigger("deselectTower");
            }
        }
    }
}
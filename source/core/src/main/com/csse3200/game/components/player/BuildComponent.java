package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;

public class BuildComponent extends Component {
    private boolean inBuildMode = false;

    public BuildComponent(Boolean inBuildMode) {
        setBuildMode(inBuildMode);
    }

    public void setBuildMode(Boolean newBuildMode) {
        this.inBuildMode = newBuildMode;
        System.out.println("New Build Mode: " + newBuildMode);
    }

    public Boolean getBuildMode() {
        return this.inBuildMode;
    }
}

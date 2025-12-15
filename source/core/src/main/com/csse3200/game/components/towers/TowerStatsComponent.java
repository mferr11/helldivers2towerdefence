package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;

public class TowerStatsComponent extends Component {
  private TowerType towerType;
  private int purchaseCost;
  private int upgradeLevel = 1;

  public TowerStatsComponent(TowerType type, int cost) {
    this.towerType = type;
    this.purchaseCost = cost;
  }

  public TowerType getTowerType() {
    return towerType;
  }

  public int getPurchaseCost() {
    return purchaseCost;
  }

  public int getUpgradeLevel() {
    return upgradeLevel;
  }

  public void incrementUpgradeLevel() {
    upgradeLevel++;
  }

  /** Calculate sell value (50% of purchase cost + upgrades) */
  public int getSellValue() {
    return (purchaseCost / 2) + (purchaseCost * (upgradeLevel - 1) / 4);
  }

  /** Calculate upgrade cost (increases with each level) */
  public int getUpgradeCost() {
    return purchaseCost * upgradeLevel / 2;
  }
}

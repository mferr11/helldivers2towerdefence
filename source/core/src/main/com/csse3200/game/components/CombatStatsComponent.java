package com.csse3200.game.components;

import com.csse3200.game.components.enemy.abilities.CloakComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);
  private int health;
  private int baseAttack;
  private int armourRating;

  public CombatStatsComponent(int health, int baseAttack, int armourRating) {
    setHealth(health);
    setBaseAttack(baseAttack);
    setArmourRating(armourRating);
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
   */
  public Boolean isDead() {
    return health == 0;
  }

  /**
   * Returns the entity's health.
   *
   * @return entity's health
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the entity's health. Health has a minimum bound of 0.
   *
   * @param health health
   */
  public void setHealth(int health) {
    if (health >= 0) {
      this.health = health;
    } else {
      this.health = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);
    }
  }

  /**
   * Adds to the player's health. The amount added can be negative.
   *
   * @param health health to add
   */
  public void addHealth(int health) {
    setHealth(this.health + health);
  }

  /**
   * Returns the entity's base attack damage.
   *
   * @return base attack damage
   */
  public int getBaseAttack() {
    return baseAttack;
  }

  /**
   * Sets the entity's attack damage. Attack damage has a minimum bound of 0.
   *
   * @param attack Attack damage
   */
  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Can not set base attack to a negative attack value");
    }
  }

  /**
   * Sets the entity's armour ratinge. Armour rating has a minimum bound of 0.
   *
   * @param Rating Armour rating
   */
  public void setArmourRating(int Rating) {
    if (Rating >= 0) {
      this.armourRating = Rating;
    } else {
      logger.error("Can not set armour rating to a negative attack value");
    }
  }

  public void hit(CombatStatsComponent attacker) {
    CloakComponent cloak = entity.getComponent(CloakComponent.class);
    if (cloak != null && cloak.isCloaked()) {
      return; // No damage while cloaked
    }

    if (this.armourRating == attacker.armourRating) {
      int newHealth = getHealth() - attacker.getBaseAttack();
      setHealth(newHealth);
      return;
    }

    if (this.armourRating < attacker.armourRating) {
      int newHealth = getHealth() - Math.round((attacker.getBaseAttack() * 1.5f));
      setHealth(newHealth);
      return;
    }

    if (this.armourRating > attacker.armourRating) {
      int newHealth = getHealth() - Math.max(1, Math.round((attacker.getBaseAttack() * 0.5f)));
      setHealth(newHealth);
      return;
    }
  }
}

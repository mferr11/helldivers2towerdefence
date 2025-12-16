package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.towers.DeselectHandlerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.EnemyFactory.EnemyType;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.TowerActionsUI;
import com.csse3200.game.ui.deckUI;
import com.csse3200.game.waveSystem.Wave;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static String levelName = "Test Level";

  private Entity playerRef;

  private List<Wave> waves;
  private Timer.Task spawnTask;
  private int waveEnemiesKilled = 0;
  private Wave currentWave;
  private int currentWaveIndex = 0;

  private final EventHandler events;

  private static java.util.List<Entity> waypointEntityList = new java.util.ArrayList<>();
  private static java.util.List<GridPoint2> waypointsGridPointList = new java.util.ArrayList<>();

  private static java.util.List<GridPoint2> towerPlacementList = new java.util.ArrayList<>();
  private TowerType selectedTowerType = TowerType.MACHINEGUN;

  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private static final String[] forestTextures = {
    "images/ghost_1.png",
    "images/grass_1.png",
    "images/grass_2.png",
    "images/grass_3.png",
    "images/tree.png",
    "images/box_boy_leaf.png"
  };
  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas"
  };

  private static final String[] forestSounds = {"sounds/Impact4.ogg"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;

  /**
   * Initialise this ForestGameArea to use the provided TerrainFactory.
   *
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
   * @requires terrainFactory != null
   */
  public ForestGameArea(TerrainFactory terrainFactory) {
    super();
    this.events = new EventHandler();
    this.terrainFactory = terrainFactory;
  }

  public EventHandler getEvents() {
    return events;
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
  @Override
  public void create() {
    towerPlacementList.clear();
    ServiceLocator.registerGameAreaEvents(this.getEvents());
    ServiceLocator.getGameAreaEvents()
        .addListener("sellTower", (EventListener1<Entity>) this::removeTower);
    ServiceLocator.getGameAreaEvents()
        .addListener("enemyreachedbase", (EventListener1<Integer>) this::damagebase);

    this.getEvents()
        .addListener("enemyKilled", (EventListener1<Integer>) (gold) -> checkEnemyKills(gold));
    this.getEvents()
        .addListener(
            "towerPlacementClick",
            (EventListener1<GridPoint2>) (location) -> tryTowerPlacement(location));

    ServiceLocator.getGameAreaEvents()
        .addListener("selectTowerType", (EventListener1<TowerType>) this::setSelectedTowerType);

    loadAssets();

    initialiseWaypoints();
    spawnTerrain(waypointsGridPointList);

    spawnTowerPreview();

    initialiseWaves();
    startWaveSpawning();

    this.playerRef = spawnPlayer();
    displayUI();
  }

  private void damagebase(Integer damage) {
    playerRef.getComponent(CombatStatsComponent.class).addHealth(damage * -1);

    if (playerRef.getComponent(CombatStatsComponent.class).getHealth() <= 0) {
      System.out.println("GAME OVER!");
    }
  }

  private void removeTower(Entity tower) {
    Vector2 vecpos = tower.getPosition();
    GridPoint2 gridpos = new GridPoint2((int) vecpos.x, (int) vecpos.y);

    towerPlacementList.remove(gridpos);
    tower.dispose();
  }

  private void setSelectedTowerType(TowerType towerType) {
    this.selectedTowerType = towerType;
    System.out.println("Selected tower type: " + towerType);

    // Update the preview to show the selected tower
    ServiceLocator.getGameAreaEvents().trigger("updateTowerPreview", towerType);
  }

  private void tryTowerPlacement(GridPoint2 location) {
    if (towerPlacementList.contains(location)) {
      System.out.println("Tower already at this location.");
    } else {
      spawnTower(location);
      towerPlacementList.add(location);
    }
  }

  private void spawnTowerPreview() {
    Entity towerPreview = TowerFactory.createTowerPreview(this.selectedTowerType);
    spawnEntity(towerPreview);
  }

  private void initialiseWaves() {
    waves = new ArrayList<>();

    // Test Wave
    waves.add(new Wave(0, false, 1f, List.of(EnemyType.STALKER), waypointEntityList));

    // Wave 1
    waves.add(
        new Wave(
            1,
            false,
            1f,
            List.of(
                EnemyType.SCAVENGER,
                EnemyType.SCAVENGER,
                EnemyType.SCAVENGER,
                EnemyType.SCAVENGER,
                EnemyType.HUNTER),
            waypointEntityList));

    // Wave 2
    waves.add(
        new Wave(
            2,
            false,
            0.75f,
            List.of(
                EnemyType.SCAVENGER,
                EnemyType.HUNTER,
                EnemyType.SCAVENGER,
                EnemyType.HUNTER,
                EnemyType.HUNTER,
                EnemyType.SCAVENGER,
                EnemyType.HUNTER,
                EnemyType.SCAVENGER,
                EnemyType.HUNTER,
                EnemyType.SCAVENGER),
            waypointEntityList));
  }

  private void startWaveSpawning() {
    currentWaveIndex = 0;
    currentWave = waves.get(currentWaveIndex);
    final int[] enemiesSpawned = {0};

    spawnTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                // Safety check before doing anything
                if (ServiceLocator.getPhysicsService() == null) {
                  this.cancel();
                  return;
                }
                if (enemiesSpawned[0] < currentWave.getTotalEnemies()) {
                  spawnEnemy();
                  enemiesSpawned[0]++;
                } else {
                  // Stop the timer when all enemies are spawned
                  this.cancel();
                  spawnTask = null;
                }
              }
            },
            0,
            currentWave.getSpawnRate());
  }

  public void checkEnemyKills(int gold) {
    waveEnemiesKilled++;
    System.out.println(waveEnemiesKilled);
    InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);
    inventory.addGold(gold);

    ServiceLocator.getGameAreaEvents().trigger("updateGold");

    if (waveEnemiesKilled >= currentWave.getTotalEnemies()) {
      // All enemies in current wave are dead
      if (currentWaveIndex >= waves.size() - 1) {
        // This is the final wave
        System.out.println("Victory!");
      } else {
        // There are more waves, wait 5 seconds before starting the next one
        System.out.println(
            "Wave " + (currentWaveIndex + 1) + " complete! Next wave in 5 seconds...");
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                startNextWave();
              }
            },
            5f); // 5 second delay
      }
    }
  }

  private void startNextWave() {
    currentWaveIndex++;
    if (currentWaveIndex < waves.size()) {
      currentWave = waves.get(currentWaveIndex);
      currentWave.reset();
      waveEnemiesKilled = 0; // Reset the counter for the new wave

      System.out.println("Starting wave " + (currentWaveIndex + 1) + "!");

      final int[] enemiesSpawned = {0};

      spawnTask =
          Timer.schedule(
              new Timer.Task() {
                @Override
                public void run() {
                  // Safety check before doing anything
                  if (ServiceLocator.getPhysicsService() == null) {
                    this.cancel();
                    return;
                  }
                  if (enemiesSpawned[0] < currentWave.getTotalEnemies()) {
                    spawnEnemy();
                    enemiesSpawned[0]++;
                  } else {
                    // Stop the timer when all enemies are spawned
                    this.cancel();
                    spawnTask = null;
                  }
                }
              },
              0,
              currentWave.getSpawnRate());
    }
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay(levelName));
    ui.addComponent(new deckUI(playerRef));
    ui.addComponent(new TowerActionsUI(playerRef));
    ui.addComponent(new DeselectHandlerComponent());
    spawnEntity(ui);
  }

  private void spawnTerrain(List<GridPoint2> waypoints) {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO, waypoints);
    spawnEntity(new Entity().addComponent(terrain));
  }

  private void initialiseWaypoints() {
    waypointsGridPointList.clear();
    waypointEntityList.clear();

    waypointsGridPointList.add(new GridPoint2(0, 5));
    waypointsGridPointList.add(new GridPoint2(2, 5));
    waypointsGridPointList.add(new GridPoint2(2, 2));
    waypointsGridPointList.add(new GridPoint2(7, 2));
    waypointsGridPointList.add(new GridPoint2(7, 5));
    waypointsGridPointList.add(new GridPoint2(10, 5));
    waypointsGridPointList.add(new GridPoint2(10, 7));
    waypointsGridPointList.add(new GridPoint2(14, 7));
    waypointsGridPointList.add(new GridPoint2(14, 2));
    waypointsGridPointList.add(new GridPoint2(20, 2));

    for (GridPoint2 wp : waypointsGridPointList) {
      Entity waypointEntity = new Entity();
      waypointEntity.setPosition(wp.x, wp.y);
      waypointEntityList.add(waypointEntity);
    }

    // Add the path to list of tiles where towers cannot be placed
    for (int i = 0; i < waypointsGridPointList.size() - 1; i++) {
      GridPoint2 start = waypointsGridPointList.get(i);
      GridPoint2 end = waypointsGridPointList.get(i + 1);

      int dx = Integer.signum(end.x - start.x);
      int dy = Integer.signum(end.y - start.y);

      int x = start.x;
      int y = start.y;

      while (x != end.x || y != end.y) {
        towerPlacementList.add(new GridPoint2(x, y));
        x += dx;
        y += dy;
      }
    }
    towerPlacementList.add(waypointsGridPointList.get(waypointsGridPointList.size() - 1));
  }

  public List<Entity> getWaypointEntityList() {
    return waypointEntityList;
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

  private Entity spawnTower(GridPoint2 location) {
    // Get the cost of the selected tower
    TowerConfig config = TowerFactory.getConfig(selectedTowerType);
    InventoryComponent inventory = playerRef.getComponent(InventoryComponent.class);

    // Check if player can afford it
    if (!inventory.hasGold(config.cost)) {
      System.out.println("Cannot place tower: Not enough gold!");
      return null;
    }

    // Deduct the cost
    inventory.addGold(-config.cost);
    System.out.println(
        "Placed "
            + selectedTowerType
            + " tower for $"
            + config.cost
            + ". Remaining gold: $"
            + inventory.getGold());

    // Create and spawn the tower
    Entity newTower = TowerFactory.createTower(selectedTowerType);
    spawnEntityAt(newTower, location, false, false);

    // Trigger event to update UI
    ServiceLocator.getGameAreaEvents().trigger("updateGold");

    // Exit build mode after placing
    ServiceLocator.getGameAreaEvents().trigger("updateBuildMode", false);

    return newTower;
  }

  private void spawnEnemy() {
    GridPoint2 spawnPos = new GridPoint2(-5, 5);

    // Get the next enemy type from the current wave
    EnemyType enemyType = currentWave.getNextEnemy();

    if (enemyType != null) {
      Entity enemy = EnemyFactory.createEnemy(enemyType, getWaypointEntityList());
      spawnEntityAt(enemy, spawnPos, true, true);
    }
  }

  // private void playMusic() {
  //   Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
  //   music.setLooping(true);
  //   music.setVolume(0.3f);
  //   music.play();
  // }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(forestTextures);
    resourceService.loadTextureAtlases(forestTextureAtlases);
    resourceService.loadSounds(forestSounds);
    resourceService.loadMusic(forestMusic);

    while (!resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(forestTextures);
    resourceService.unloadAssets(forestTextureAtlases);
    resourceService.unloadAssets(forestSounds);
    resourceService.unloadAssets(forestMusic);
  }

  @Override
  public void dispose() {
    super.dispose();

    if (spawnTask != null) {
      spawnTask.cancel();
      spawnTask = null;
    }

    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }
}

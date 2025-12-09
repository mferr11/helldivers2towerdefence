package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.waveSystem.Wave;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.entities.factories.TowerFactory.TowerType;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.deckUI;
import com.badlogic.gdx.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static String levelName = "Test Level";

  private Entity playerRef;

  private List<Wave> waves;
  private Timer.Task spawnTask;
  private int waveEnemiesKilled = 0;
  private Wave currentWave;

  private final EventHandler events;

  private static java.util.List<Entity> waypointEntityList = new java.util.ArrayList<>();
  private static java.util.List<GridPoint2> waypointsGridPointList = new java.util.ArrayList<>();

  private static java.util.List<GridPoint2> towerPlacementList = new java.util.ArrayList<>();

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

    this.getEvents().addListener("enemyKilled", this::checkEnemyKills);
    this.getEvents().addListener("towerPlacementClick", (EventListener1<GridPoint2>) (location) -> tryTowerPlacement(location));

    loadAssets();

    initialiseWaypoints();
    spawnTerrain(waypointsGridPointList);

    spawnTowerPreview();

    initialiseWaves();
    startWaveSpawning();

    //spawnTrees();
    this.playerRef = spawnPlayer();
    displayUI();

    //spawnGhosts();
    //spawnGhostKing();

    //playMusic();
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
    Entity towerPreview = TowerFactory.createTowerPreview(TowerType.MACHINEGUN);
    spawnEntity(towerPreview);
  }

  private void initialiseWaves() {
    waves = new ArrayList<>();

    waves.add(new Wave(0, false, 5, 1f, waypointEntityList));
  }

  private void startWaveSpawning() {
    currentWave = waves.get(0);
    final int[] enemiesSpawned = {0};
      
    spawnTask = Timer.schedule(new Timer.Task() {
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
    }, 0, currentWave.getSpawnRate());  // Start immediately (0 delay), repeat every spawnRate seconds
  }

  public void checkEnemyKills() {
    waveEnemiesKilled++;
    System.out.println(waveEnemiesKilled);
    if (waveEnemiesKilled >= currentWave.getTotalEnemies()) {
      System.out.println("Victory!");
    }
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay(levelName));
    ui.addComponent(new deckUI(playerRef));
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

    for (GridPoint2 wp : waypointsGridPointList) {
      Entity waypointEntity = new Entity();
      waypointEntity.setPosition(wp.x, wp.y);
      waypointEntityList.add(waypointEntity);
    }

    // Add the path to list of tiles where towers cannot be placed
    for (int i = 0; i <waypointsGridPointList.size() - 1; i++) {
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
    Entity newTower = TowerFactory.createTower(TowerType.MACHINEGUN);
    spawnEntityAt(newTower, location, false, false);
    return newTower;
  }

  private void spawnEnemy() {
    GridPoint2 spawnPos = new GridPoint2(-5, 5);
    Entity enemy = EnemyFactory.createBaseEnemy(getWaypointEntityList());
    spawnEntityAt(enemy, spawnPos, true, true);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
    music.setLooping(true);
    music.setVolume(0.3f);
    music.play();
  }

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

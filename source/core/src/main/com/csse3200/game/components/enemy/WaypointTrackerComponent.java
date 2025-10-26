package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import java.util.List;

/**
 * Component for tracking an enemy's progress through waypoints.
 * Stores the current waypoint index and priority for pathfinding tasks.
 */
public class WaypointTrackerComponent extends Component {
    private int currentWaypoint;
    private int currentPriority;
    private List<Entity> waypoints;

    /**
     * Creates a waypoint tracker with default starting values.
     * 
     * @param waypoints List of waypoint entities for the enemy to follow
     */
    public WaypointTrackerComponent(List<Entity> waypoints) {
        this.waypoints = waypoints;
        this.currentWaypoint = 0;
        this.currentPriority = 1;
    }

    /**
     * Gets the current waypoint index.
     * 
     * @return current waypoint index
     */
    public int getCurrentWaypoint() {
        return currentWaypoint;
    }

    /**
     * Gets the current priority level.
     * 
     * @return current priority
     */
    public int getCurrentPriority() {
        return currentPriority;
    }

    /**
     * Gets the list of waypoints.
     * 
     * @return waypoint list
     */
    public List<Entity> getWaypoints() {
        return waypoints;
    }

    /**
     * Advances to the next waypoint and increments priority.
     * 
     * @return true if successfully advanced, false if at end of waypoint list
     */
    public boolean advanceWaypoint() {
        currentWaypoint++;
        currentPriority++;
        return currentWaypoint < waypoints.size();
    }

    /**
     * Gets the current waypoint entity.
     * 
     * @return current waypoint entity, or null if at end of list
     */
    public Entity getCurrentWaypointEntity() {
        if (currentWaypoint < waypoints.size()) {
            return waypoints.get(currentWaypoint);
        }
        return null;
    }
}
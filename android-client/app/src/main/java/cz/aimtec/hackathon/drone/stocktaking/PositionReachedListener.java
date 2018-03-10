package cz.aimtec.hackathon.drone.stocktaking;

import cz.aimtec.hackathon.drone.models.Point3D;
import cz.aimtec.hackathon.drone.models.Position;

/**
 * Created by Jan Klik on 10.3.2018.
 */

interface PositionReachedListener {
    public void onPositionReached(Position position, Point3D point, int positionIndex);
}

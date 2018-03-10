package cz.aimtec.hackathon.drone.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class Position {

    public static final int POINTS_COUNT = 4;
    private int id;
    private String name;
    private List<Point3D> points;
    private Point3D centerPoint;

    public Position(SevioModel m) {
        id = m.getId();
        name = m.getName();
        points = new ArrayList<>(4);

        if (m.getVertices() != null && !m.getVertices().isEmpty()) {
            String[] offsets = m.getVertices().split(",");
            for (int pointIndex = 0; pointIndex < POINTS_COUNT * 3 - 1; pointIndex+=3) {
                points.add(new Point3D( Float.valueOf(offsets[pointIndex]),
                        Float.valueOf(offsets[pointIndex + 1]),
                        Float.valueOf(offsets[pointIndex + 2])));
            }

            centerPoint = new Point3D(  (points.get(0).getX() + points.get(2).getX()) / 2,
                    (points.get(0).getY() + points.get(2).getY()) / 2,
                    (points.get(0).getZ() + points.get(2).getZ()) / 2);

        }
    }

    public Point3D getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Point3D centerPoint) {
        this.centerPoint = centerPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

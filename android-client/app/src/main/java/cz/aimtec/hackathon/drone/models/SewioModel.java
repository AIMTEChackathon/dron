package cz.aimtec.hackathon.drone.models;

/**
 * Created by klin on 10. 3. 2018.
 */

public class SewioModel {
    private int id;
    private String name;
    private String vertices;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVertices() {
        return vertices;
    }

    public void setVertices(String vertices) {
        this.vertices = vertices;
    }
}

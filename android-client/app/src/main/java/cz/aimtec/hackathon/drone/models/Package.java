package cz.aimtec.hackathon.drone.models;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class Package {
    private String labelNumber;
    private String positionName;
    private int positionId;

    public Package(String labelNumber, String positionName, int positionId) {
        this.labelNumber = labelNumber;
        this.positionName = positionName;
        this.positionId = positionId;
    }

    public String getLabelNumber() {
        return labelNumber;
    }

    public void setLabelNumber(String labelNumber) {
        this.labelNumber = labelNumber;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }
}

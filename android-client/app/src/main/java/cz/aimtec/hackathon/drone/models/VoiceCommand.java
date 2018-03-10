package cz.aimtec.hackathon.drone.models;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class VoiceCommand {
    private String command;

    public VoiceCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}

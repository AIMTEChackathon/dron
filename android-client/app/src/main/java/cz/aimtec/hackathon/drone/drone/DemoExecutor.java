package cz.aimtec.hackathon.drone.drone;


import cz.aimtec.hackathon.drone.activities.DemoActivity;

/**
 * Demo executor that
 * <p>
 * Created by pavd on 02.03.2018.
 */
public class DemoExecutor implements IBitmapResolverListener
{
    /**
     * Life state of drone
     */
    public enum EXECUTION_STATE
    {
        //initialized
        READY,
        //ready for start, we got QR
        FOR_EXECUTE,
        //demo running
        RUNNING,
        // end
        FINISHED
    }

    private BebopDrone drone;
    private EXECUTION_STATE state;
    //notify tokens
    //extra for take off + land
    private Object flyingNotifyToken;
    //for relative moveToRelativePosition
    private Object moveNotifyToken;

    //demoActivity of parent activity
    private DemoActivity demoActivity;

    public DemoExecutor(BebopDrone drone, DemoActivity activity)
    {
        this.drone = drone;
        this.state = EXECUTION_STATE.READY;

        this.flyingNotifyToken = new Object();
        this.moveNotifyToken = new Object();

        this.demoActivity = activity;
    }

    /**
     * Runs set of commands by using SDK relative moves.
     * At first, it takes off and waits on QR code "DO THE SHOW"
     * <p>
     * On every command, it blocks its thread and waits until being notified by drone event.
     */
    public void run()
    {
        this.state = EXECUTION_STATE.RUNNING;

        takeOff();

        //active waiting for QR received
        while (this.state != EXECUTION_STATE.FOR_EXECUTE)
        {
            sleep(500);
        }

        //setting notify token for RELATIVE_MOVE_ENDED
        drone.setMoveNotifyToken(moveNotifyToken);
        //count of parts for progressbar
        int parts = 6;

        demoActivity.updateProgressBar(0, parts);

        drone.moveUp(0.5);
        demoActivity.updateProgressBar(1, parts);

        drone.moveLeft(0.5);
        demoActivity.updateProgressBar(2, parts);

        drone.moveRight(0.5);
        demoActivity.updateProgressBar(3, parts);

        drone.turnLeft(6.2);
        demoActivity.updateProgressBar(4, parts);

        drone.turnRight(6.2);
        demoActivity.updateProgressBar(5, parts);

        land();
        demoActivity.updateProgressBar(6, parts);

        this.state = EXECUTION_STATE.FINISHED;
    }

    /**
     * Runs set of commands without using relative moves (just changing ROLL, GAZ, YAW parameters) and waiting
     * At first, it takes off and waits on QR code "DO THE SHOW"
     */
    public void run2advanced()
    {
        //count of parts for progressbar
        int parts = 6;
        this.state = EXECUTION_STATE.RUNNING;

        drone.takeOff();
        sleep(750);

        //active waiting for QR received
        while (this.state != EXECUTION_STATE.FOR_EXECUTE)
        {
            sleep(500);
        }

        demoActivity.updateProgressBar(0, parts);

        moveUp();
        demoActivity.updateProgressBar(1, parts);

        moveLeft();
        demoActivity.updateProgressBar(2, parts);

        moveRight();
        demoActivity.updateProgressBar(3, parts);

        turnLeft();
        demoActivity.updateProgressBar(4, parts);

        turnRight();
        demoActivity.updateProgressBar(5, parts);

        drone.land();
        demoActivity.updateProgressBar(6, parts);

        this.state = EXECUTION_STATE.FINISHED;
    }

    /**
     * Just test, whether disarming flying token cannot help
     * */
    private void land()
    {
        drone.setFlyingNotifyToken(flyingNotifyToken);
        drone.land();
        drone.setFlyingNotifyToken(null);
    }

    /**
     * Just test, whether disarming flying token cannot help
     * */
    private void takeOff()
    {
        drone.setFlyingNotifyToken(flyingNotifyToken);
        drone.takeOff();
        drone.setFlyingNotifyToken(null);
    }

    /**
     * Turns drone by its Z axe. Cca 180 degrees right hand.
     */
    private void turnRight()
    {
        System.out.println("TURN RIGHT");

        drone.setYaw((byte) 100);
        drone.setFlag((byte) 1);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Turns drone by its Z axe. Cca 180 degrees left hand.
     */
    private void turnLeft()
    {
        System.out.println("TURN LEFT");

        drone.setYaw((byte) -100);
        drone.setFlag((byte) 1);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Reset gaz, roll, yaw and flag to ZERO
     */
    public void resetGRYF()
    {
        drone.setGaz((byte) 0);
        drone.setYaw((byte) 0);
        drone.setRoll((byte) 0);
        drone.setFlag((byte) 0);
        sleep(1000);
    }

    /**
     * Moves drone right a bit
     */
    private void moveRight()
    {
        System.out.println("MOVE RIGHT");

        drone.setRoll((byte) 50);
        drone.setFlag((byte) 1);
        sleep(750);
        resetGRYF();
    }

    /**
     * Moves drone left a bit
     */
    private void moveLeft()
    {
        System.out.println("MOVE LEFT");

        drone.setRoll((byte) -50);
        drone.setFlag((byte) 1);
        sleep(750);
        resetGRYF();
    }

    /**
     * Moves drone up a bit
     */
    private void moveUp()
    {
        System.out.println("MOVE UP");

        drone.setGaz((byte) 50);
        drone.setFlag((byte) 1);
        sleep(600);
        resetGRYF();
    }

    /**
     * Moves drone down a bit
     */
    private void moveDown()
    {
        System.out.println("MOVE DOWN");

        drone.setGaz((byte) -50);
        drone.setFlag((byte) 1);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Sleep for a "i in millis" while
     */
    private void sleep(int i)
    {
        try
        {
            Thread.sleep(i);
        } catch (InterruptedException e)
        {
        }
    }

    /**
     * Reaction on QR code being resolved
     */
    @Override
    public void qrResolved(String result)
    {
        if (result.compareTo("DO THE SHOW") == 0)
        {
            this.state = EXECUTION_STATE.FOR_EXECUTE;
        }
    }

    /**
     * Getter of current execution state
     */
    public EXECUTION_STATE getState()
    {
        return state;
    }
}

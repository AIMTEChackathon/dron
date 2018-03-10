package cz.aimtec.hackathon.drone.drone;


import cz.aimtec.hackathon.drone.activities.StorageScanActivity;

/**
 * Demo executor that ...
 * <p>
 * Created by pavd on 02.03.2018.
 */
public class StorageScanExecutor implements IBitmapResolverListener
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

    //parentActivity of parent activity
    private StorageScanActivity parentActivity;

    public StorageScanExecutor(BebopDrone drone, StorageScanActivity activity)
    {
        this.drone = drone;
        this.state = EXECUTION_STATE.READY;

        this.flyingNotifyToken = new Object();
        this.moveNotifyToken = new Object();

        this.parentActivity = activity;
    }

    /**
     * Runs set of commands without using relative moves (just changing ROLL, GAZ, YAW parameters) and waiting
     * At first, it takes off and waits on QR code "DO THE SHOW"
     */
    public void run()
    {
        //180 per second
        drone.setMaxRotationSpeed(180);
        //3 m per sec
        drone.setMaxVerticalSpeed(3);
        //20 angles tilt
        drone.setMaxTilt(20);

        //count of parts for progressbar
        int parts = 6;
        this.state = EXECUTION_STATE.RUNNING;

        drone.takeOff();
        sleep(1000);
        resetGRYF();
        moveUp2();


        sleep(1000);

        drone.land();

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
        drone.setFlag((byte) 0);
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
        drone.setFlag((byte) 0);
        sleep(1000);
        resetGRYF();
    }

    private void turnLeftFlag()
    {
        System.out.println("TURN LEFT");

        drone.setYaw((byte) -100);
        drone.setFlag((byte) 0);
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
        sleep(1750);
    }

    /**
     * Moves drone right a bit
     */
    private void moveRight()
    {
        System.out.println("MOVE RIGHT");

        drone.setRoll((byte) 50);
        drone.setFlag((byte) 1);
        sleep(800);
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
        sleep(800);
        resetGRYF();
    }

    /**
     * Moves drone up a bit
     */
    private void moveUp()
    {
        System.out.println("MOVE UP");

        drone.setGaz((byte) 50);
        drone.setFlag((byte) 0);
        sleep(600);
        resetGRYF();
    }

    private void moveUp2()
    {
        System.out.println("MOVE UP");

        drone.setGaz((byte) 50);
        drone.setFlag((byte) 0);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Moves drone down a bit
     */
    private void moveDown()
    {
        System.out.println("MOVE DOWN");

        drone.setGaz((byte) -50);
        drone.setFlag((byte) 0);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Moves drone forwad
     */
    private void moveForward()
    {
        System.out.println("MOVE FORWARD");

        drone.setPitch((byte) 50);
        drone.setFlag((byte) 1);
        sleep(1000);
        resetGRYF();
    }

    /**
     * Moves drone forward
     */
    private void moveBackward()
    {
        System.out.println("MOVE BACKWARD");

        drone.setPitch((byte) -50);
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
            e.printStackTrace();
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

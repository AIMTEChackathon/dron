package cz.aimtec.hackathon.drone.drone;

/**
 * Interface for image resolution events
 *
 * Created by pavd on 02.03.2018.
 */

public interface IBitmapResolverListener
{
    /**
     * Reaction on QR code being resolved
     * */
    void qrResolved(String result);
}

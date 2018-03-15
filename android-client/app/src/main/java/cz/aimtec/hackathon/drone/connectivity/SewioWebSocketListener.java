package cz.aimtec.hackathon.drone.connectivity;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by Manas on 10.03.2018.
 */

public class SewioWebSocketListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final String API_KEY = "171555a8fe71148a165392904";
    private static final String TAG_ID = "16";

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("{\"headers\":{\"X-ApiKey\":\"" + API_KEY + "\"},\"method\":\"subscribe\", " +
                "\"resource\":\"/feeds/" + TAG_ID + "\"}");
    }
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        //Log.d("output", text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.send("{\"headers\":{\"X-ApiKey\":\"" + API_KEY + "\"},\"method\":\"unsubscribe\"," +
                "\"resource\":\"/feeds/" + TAG_ID + "\"}");
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d("output","Closing " + code + " / " + reason);
        System.out.println("### Closing " + code + " / " + reason);
    }
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e("output", "Error : " + t.getMessage());
        System.out.println("### Error : " + t.getMessage());
    }
}

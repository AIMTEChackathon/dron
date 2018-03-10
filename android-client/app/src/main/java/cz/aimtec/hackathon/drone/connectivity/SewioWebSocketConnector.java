package cz.aimtec.hackathon.drone.connectivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by klin on 10.03.2018.
 */
public class SewioWebSocketConnector extends WebSocketListener {

    private static final String WEBSOCKET_URI = "ws://192.168.90.54:8080";

    public void connect(SewioWebSocketListener listener){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(WEBSOCKET_URI).build();
        WebSocket ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }
}

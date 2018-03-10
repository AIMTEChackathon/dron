package cz.aimtec.hackathon.drone.connectivity;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Jan Klik on 10.3.2018.
 */
public class AsyncHttpResponseHandlerEmpty extends AsyncHttpResponseHandler {
    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        // do nothing
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        // do nothing
    }
}
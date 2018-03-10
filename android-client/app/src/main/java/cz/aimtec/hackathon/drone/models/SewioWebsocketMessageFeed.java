package cz.aimtec.hackathon.drone.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class SewioWebsocketMessageFeed {

    private Body body;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Point3D getPoint() {
        Map<String, String> points = getBody().getDatastreams().stream()
            .filter(ds -> ds.getId().startsWith("pos"))
            .collect(Collectors.toMap(  SewioWebsocketMessageFeed.DataStream::getId,
                                        SewioWebsocketMessageFeed.DataStream::getCurrent_value));
        return new Point3D( Float.valueOf(points.get("posX")),
                            Float.valueOf(points.get("posY")),
                            0);
    }

    public static class Body {
        private String id;
        private List<DataStream> datastreams;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<DataStream> getDatastreams() {
            return datastreams;
        }

        public void setDatastreams(List<DataStream> datastreams) {
            this.datastreams = datastreams;
        }
    }
    public static class DataStream {
        private String id;
        private String current_value;
        private String at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCurrent_value() {
            return current_value;
        }

        public void setCurrent_value(String current_value) {
            this.current_value = current_value;
        }

        public String getAt() {
            return at;
        }

        public void setAt(String at) {
            this.at = at;
        }
    }
}

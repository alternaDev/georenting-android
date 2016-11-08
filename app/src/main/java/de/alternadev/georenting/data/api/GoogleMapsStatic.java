package de.alternadev.georenting.data.api;

import de.alternadev.georenting.data.api.model.GeoFence;
import hugo.weaving.DebugLog;

/**
 * Created by jhbruhn on 15.05.16.
 */
public class GoogleMapsStatic {


    private static final double MAXIMUM_WIDTH = 640;
    private static final double MAXIMUM_HEIGHT = 640;
    private static final String BASE_URL = "http://maps.google.com/maps/api/staticmap";

    private static final String STYLE = "&style=visibility:on" +
            "&style=element:geometry%7Cvisibility:simplified" +
            "&style=element:labels.icon%7Cvisibility:simplified" +
            "&style=element:labels.text.fill%7Ccolor:0x616161" +
            "&style=element:labels.text.stroke%7Ccolor:0xf5f5f5" +
            "&style=feature:administrative%7Celement:labels.text.fill%7Ccolor:0x444444" +
            "&style=feature:administrative.country%7Cvisibility:off" +
            "&style=feature:administrative.land_parcel%7Celement:labels%7Cvisibility:off" +
            "&style=feature:administrative.land_parcel%7Celement:labels.text.fill%7Ccolor:0xbdbdbd" +
            "&style=feature:administrative.locality%7Cvisibility:off" +
            "&style=feature:landscape.man_made%7Cvisibility:on" +
            "&style=feature:landscape.man_made%7Celement:labels.text%7Cvisibility:on" +
            "&style=feature:landscape.natural%7Cvisibility:on" +
            "&style=feature:poi%7Celement:geometry%7Ccolor:0xeeeeee" +
            "&style=feature:poi%7Celement:labels.text.fill%7Ccolor:0x757575" +
            "&style=feature:poi.park%7Celement:geometry%7Ccolor:0xe5e5e5" +
            "&style=feature:poi.park%7Celement:geometry.fill%7Ccolor:0xb4e0b7%7Cvisibility:on" +
            "&style=feature:poi.park%7Celement:labels.text.fill%7Ccolor:0x9e9e9e" +
            "&style=feature:poi.school%7Cvisibility:on" +
            "&style=feature:poi.sports_complex%7Cvisibility:on" +
            "&style=feature:road%7Celement:geometry%7Ccolor:0xffffff" +
            "&style=feature:road%7Celement:geometry.fill%7Ccolor:0xffc107" +
            "&style=feature:road.arterial%7Celement:geometry.fill%7Ccolor:0xffd75e" +
            "&style=feature:road.arterial%7Celement:labels%7Cvisibility:on" +
            "&style=feature:road.arterial%7Celement:labels.icon%7Cvisibility:off" +
            "&style=feature:road.arterial%7Celement:labels.text.fill%7Ccolor:0x757575" +
            "&style=feature:road.highway%7Cvisibility:simplified" +
            "&style=feature:road.highway%7Celement:geometry%7Ccolor:0xdadada" +
            "&style=feature:road.highway%7Celement:geometry.fill%7Ccolor:0xffc107" +
            "&style=feature:road.highway%7Celement:labels.icon%7Cvisibility:off" +
            "&style=feature:road.highway%7Celement:labels.text.fill%7Ccolor:0x616161" +
            "&style=feature:road.local%7Celement:geometry.fill%7Ccolor:0xffd75e" +
            "&style=feature:road.local%7Celement:labels%7Cvisibility:off" +
            "&style=feature:road.local%7Celement:labels.text.fill%7Ccolor:0x9e9e9e" +
            "&style=feature:transit%7Cvisibility:on" +
            "&style=feature:transit.line%7Celement:geometry%7Ccolor:0xe5e5e5" +
            "&style=feature:water%7Ccolor:0x46bcec" +
            "&style=feature:water%7Celement:geometry%7Ccolor:0xc9c9c9" +
            "&style=feature:water%7Celement:geometry.fill%7Ccolor:0x46bcec" +
            "&style=feature:water%7Celement:labels.text.fill%7Ccolor:0x666666";


    @DebugLog
    public String getFenceThumbnailMapUrl(GeoFence fence, int width, int height) {

        double wRatio = 1, hRatio = 1;

        if (width >= height) {
            if (width > MAXIMUM_WIDTH || height > MAXIMUM_HEIGHT) {
                wRatio = MAXIMUM_WIDTH / width;
                hRatio = MAXIMUM_HEIGHT / height;
            }
        } else {
            if (width > MAXIMUM_WIDTH || height > MAXIMUM_HEIGHT) {
                wRatio = MAXIMUM_HEIGHT / width;
                hRatio = MAXIMUM_WIDTH / height;
            }
        }

        double ratio = Math.min(wRatio, hRatio);

        width = (int) Math.round(width * ratio);
        height = (int) Math.round(height * ratio);


        String path = "&path=fillcolor:0x2196F3FF|weight:0";
        int r = 6371;
        double lat = (fence.centerLat * Math.PI) / 180;
        double lon = (fence.centerLon * Math.PI) / 180;
        double d = fence.radius / 1000.0 / r;

        for (int i = 0; i <= 360; i += 15) {
            double brng = i * Math.PI / 180;
            double pLat = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(brng));
            double pLon = ((lon + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat), Math.cos(d) - Math.sin(lat) * Math.sin(pLat))) * 180) / Math.PI;
            pLat = (pLat * 180) / Math.PI;
            path += "|" + pLat + "," + pLon;
        }


        return BASE_URL + "?center=" + fence.centerLat + "," + fence.centerLon +
                "&zoom=14" +
                "&size=" + width + "x" + height + path + "" +
                "&sensor=false" +
                "&scale=2" + STYLE;
    }
}

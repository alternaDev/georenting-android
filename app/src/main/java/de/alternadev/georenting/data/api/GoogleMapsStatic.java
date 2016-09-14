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

    private static final String[] STYLES = {
            "feature:administrative|element:labels.text.fill|color:0x444444",
            "feature:road|element:geometry.fill|color:0xffc107",
            "feature:water|element:all|visibility:on|color:0x46bcec",
            "feature:administrative.country|element:all|visibility:off",
            "feature:administrative.locality|element:all|visibility:off",
            "feature:landscape.man_made|element:geometry.fill|visibility:on",
            "feature:landscape.natural|element:geometry.fill|visibility:on",
            "feature:poi.park|element:geometry.fill|visibility:on",
            "feature:poi.school|element:geometry.fill|visibility:on",
            "feature:poi.sports_complex|element:geometry.fill|visibility:on",
            "feature:road.highway|element:all|visibility:simplified",
            "feature:road.highway|element:labels.icon|visibility:off",
            "feature:road.arterial|element:labels|visibility:on",
            "feature:road.arterial|element:labels.icon|visibility:off",
            "feature:transit|element:all|visibility:off",
            "feature:transit.line|element:geometry.fill|visibility:on",
            "feature:transit.station|element:geometry.fill|visibility:on",
    };


    @DebugLog
    public String getFenceThumbnailMapUrl(GeoFence fence, int width, int height) {

        double wRatio = 1, hRatio = 1;

        if(width >= height) {
            if(width > MAXIMUM_WIDTH || height > MAXIMUM_HEIGHT) {
                wRatio = MAXIMUM_WIDTH / width;
                hRatio = MAXIMUM_HEIGHT / height;
            }
        } else {
            if(width > MAXIMUM_WIDTH || height > MAXIMUM_HEIGHT) {
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


        String url = BASE_URL + "?center=" + fence.centerLat + "," + fence.centerLon +
                "&zoom=14&size=" + width + "x" + height + path + "&sensor=false&scale=2";

        for (String styling : STYLES) {
            url += "&style=" + styling;
        }

        return url;
    }
}

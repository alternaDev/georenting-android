package de.alternadev.georenting.data.api;

import de.alternadev.georenting.data.api.model.GeoFence;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by jhbruhn on 15.05.16.
 */
public class GoogleMapsStatic {

    private static final String BASE_URL = "http://maps.google.com/maps/api/staticmap";

    @DebugLog
    public String getFenceThumbnailMapUrl(GeoFence fence, int width, int height) {
        String path = "&path=color:0x0000ff|weight:5";
        int r = 6371;
        double lat = (fence.centerLat * Math.PI) / 180;
        double lon = (fence.centerLon * Math.PI) / 180;
        double d = fence.radius / 1000.0 / r;

        for (int i = 0; i <= 360; i += 8) {
            double brng = i * Math.PI / 180;
            double pLat = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(brng));
            double pLon = ((lon + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat), Math.cos(d) - Math.sin(lat) * Math.sin(pLat))) * 180) / Math.PI;
            pLat = (pLat * 180) / Math.PI;
            path += "|" + pLat + "," + pLon;
        }


        return BASE_URL + "?center=" + fence.centerLat + "," + fence.centerLon +
                "&zoom=15&size=" + width + "x" + height + path + "&sensor=false";
    }
}

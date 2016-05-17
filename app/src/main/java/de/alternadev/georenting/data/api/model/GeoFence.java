package de.alternadev.georenting.data.api.model;

@org.parceler.Parcel
public class GeoFence {
    public String id;
    public double centerLat;
    public double centerLon;
    public int radius;
    public String name;
    public int owner;

    public GeoFence() {}
    public GeoFence(double lat, double lon) {
        this.centerLat = lat;
        this.centerLon = lon;
    }
}

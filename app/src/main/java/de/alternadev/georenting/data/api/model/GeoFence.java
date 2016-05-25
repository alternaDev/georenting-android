package de.alternadev.georenting.data.api.model;

import java.util.Date;

import de.alternadev.georenting.data.api.DateAdapter;

@org.parceler.Parcel
public class GeoFence {
    public String id;
    public double centerLat;
    public double centerLon;
    public int radius;
    public long ttl;
    //@DateAdapter.DateType
    //public Date diesAt; //TODO: FIX
    public double rentMultiplier;
    public String name;
    public int owner;

    public GeoFence() {}
    public GeoFence(double lat, double lon) {
        this.centerLat = lat;
        this.centerLon = lon;
    }
}

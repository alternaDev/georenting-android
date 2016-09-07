package de.alternadev.georenting.data.api.model;

import java.util.Date;

import com.squareup.moshi.Json;

@org.parceler.Parcel
public class GeoFence {
    @Json(name = "id")
    public String id;

    @Json(name = "center_lat")
    public double centerLat;

    @Json(name = "center_lon")
    public double centerLon;

    @Json(name = "radius")
    public int radius;

    @Json(name = "ttl")
    public long ttl;

    @Json(name = "dies_at")
    public Date diesAt;

    @Json(name = "rent_multiplier")
    public double rentMultiplier;

    @Json(name = "name")
    public String name;

    @Json(name = "owner")
    public int owner;

    @Json(name = "owner_name")
    public String owner_name;

    @Json(name = "total_visitors")
    public int totalVisitors;

    @Json(name = "total_earnings")
    public double totalEarnings;

    @Json(name = "cost")
    public double cost;

    public GeoFence() {}
    public GeoFence(double lat, double lon) {
        this.centerLat = lat;
        this.centerLon = lon;
    }
    public GeoFence(double lat, double lon, int radius, String name, String id) {
        this.centerLat = lat;
        this.centerLon = lon;
        this.radius = radius;
        this.name = name;
        this.id = id;
    }
}

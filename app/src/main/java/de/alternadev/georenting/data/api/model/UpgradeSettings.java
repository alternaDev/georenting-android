package de.alternadev.georenting.data.api.model;


import com.squareup.moshi.Json;

import java.util.List;

public class UpgradeSettings {
    public List<Integer> radius;
    public List<Double> rent;

    @Json(name = "max_ttl")
    public long maxTtl;

    @Json(name = "max_radius")
    public int maxRadius;

    @Json(name = "min_radius")
    public int minRadius;
}

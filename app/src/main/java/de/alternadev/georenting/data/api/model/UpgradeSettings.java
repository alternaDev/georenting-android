package de.alternadev.georenting.data.api.model;


import com.squareup.moshi.Json;

import java.util.List;

public class UpgradeSettings {
    List<Integer> radius;
    List<Double> rent;

    @Json(name = "max_ttl")
    long maxTtl;

    @Json(name = "max_radius")
    int maxRadius;

    @Json(name = "min_radius")
    int minRadius;
}

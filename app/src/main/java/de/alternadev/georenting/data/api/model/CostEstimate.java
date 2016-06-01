package de.alternadev.georenting.data.api.model;

import com.squareup.moshi.Json;

public class CostEstimate {
    @Json(name = "cost")
    public double cost;

    @Json(name = "can_afford")
    public boolean canAfford;
}

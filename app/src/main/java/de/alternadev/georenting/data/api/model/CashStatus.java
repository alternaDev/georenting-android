package de.alternadev.georenting.data.api.model;

import com.squareup.moshi.Json;

/**
 * Created by jhbruhn on 05.06.16.
 */
public class CashStatus {
    @Json(name = "earnings_rent_7d")
    public float earningsRentSevenDays;
    @Json(name = "earnings_rent_all")
    public float earningsRentAllTime;
    @Json(name = "expenses_rent_7d")
    public float expensesRentSevenDays;
    @Json(name = "expenses_rent_all")
    public float expensesRentAllTime;
    @Json(name = "expenses_geofence_7d")
    public float expensesGeoFenceSevenDays;
    @Json(name = "expenses_geofence_all")
    public float expensesGeoFenceAllTime;

    public float getSevenDaysTotal() {
        return earningsRentSevenDays - expensesRentSevenDays - expensesGeoFenceSevenDays;
    }

    public float getAllTimeTotal() {
        return earningsRentAllTime - expensesRentAllTime - expensesGeoFenceAllTime;
    }
}

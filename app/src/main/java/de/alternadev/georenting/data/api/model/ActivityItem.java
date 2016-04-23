package de.alternadev.georenting.data.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jhbruhn on 23.04.16.
 */
public class ActivityItem {
    public String verb;

    public long time;

    public String fenceName;
    public int fenceId;
    public String ownerName;
    public int ownerId;
    public String visitorName;
    public int visitorId;

    public float rent;
}

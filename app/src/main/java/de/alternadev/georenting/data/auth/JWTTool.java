package de.alternadev.georenting.data.auth;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jhbruhn on 28.04.16.
 */
public class JWTTool {
    public static int getExpiration(String token) {
        String[] parts = token.split("\\.");

        String header = parts[0];
        String headerJson = new String(Base64.decode(header, Base64.DEFAULT));
        JSONObject headerObject = null;
        try {
            headerObject = new JSONObject(headerJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (headerObject != null) {
                return headerObject.getInt("exp");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }
}

package com.example.krzysiek.carmas;
import android.location.Location;


public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
}


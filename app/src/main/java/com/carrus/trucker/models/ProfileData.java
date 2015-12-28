package com.carrus.trucker.models;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saurbhv on 11/20/15.
 */
public class ProfileData {

    public String _id;
    public Integer driverId;
    public String driverName;
    public String phoneNumber;
    public String address;
    public String stateDl;
    public String createdAt;
    public CurrentCoordinates currentCoordinates;
    public float totalRating;
    public Integer noOfPeopleRating;
    public float rating;
    public Integer loginCount;
    public Integer radius;
    public Location location;
    public ProfilePicture profilePicture;
    public Boolean isBlocked;
    public List<FleetOwner> fleetOwner = new ArrayList<FleetOwner>();
    public String userType;
    public Integer _v;
    public DrivingLicense drivingLicense;
    public VoterId VoterId;
    public AdharCard adharCard;
    public String lastLogin;
    public DeviceDetails deviceDetails;
    
}

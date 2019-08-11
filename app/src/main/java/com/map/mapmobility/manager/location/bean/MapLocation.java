package com.map.mapmobility.manager.location.bean;

/**
 *  这是定位回调的实体基类
 *
 * @author mingjiezuo
 */
public class MapLocation {

    double latitude;
    double longitude;
    String provider;

    float bearing;
    float speed;

    String address;
    String name;
    String city;
    String cityCode;


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getProvider() {
        return provider;
    }

    public float getBearing() {
        return bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCityCode() {
        return cityCode;
    }
}

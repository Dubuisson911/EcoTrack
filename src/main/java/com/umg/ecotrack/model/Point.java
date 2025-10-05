package com.umg.ecotrack.model;

public class Point {
    private int id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String manager;

    public Point() {}
    public Point(int id, String name, String address, Double latitude, Double longitude, String manager) {
        this.id = id; this.name = name; this.address = address; this.latitude = latitude; this.longitude = longitude; this.manager = manager;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    @Override public String toString() { return name; } // útil en ComboBox
}

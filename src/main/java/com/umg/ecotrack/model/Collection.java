package com.umg.ecotrack.model;

import java.time.LocalDate;

public class Collection {
    private int id;
    private int pointId;
    private LocalDate date;
    private String type; // Plástico/Vidrio/Papel
    private double weightKg;

    public Collection() {}
    public Collection(int id, int pointId, LocalDate date, String type, double weightKg) {
        this.id = id; this.pointId = pointId; this.date = date; this.type = type; this.weightKg = weightKg;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPointId() { return pointId; }
    public void setPointId(int pointId) { this.pointId = pointId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
}

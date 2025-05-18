package com.example.assignment3.model;

import java.io.Serializable;

public class FirebaseMovie implements Serializable {
    private String id;  // Firestore document ID
    private String title;
    private String studio;
    private double criticsRating;
    private String posterUrl;
    private String year;
    private String plot;

    // Empty constructor needed for Firestore
    public FirebaseMovie() {}

    public FirebaseMovie(String title, String studio, String plot, double criticsRating, String posterUrl, String year) {
        this.title = title;
        this.studio = studio;
        this.criticsRating = criticsRating;
        this.posterUrl = posterUrl;
        this.year = year;
        this.plot = plot;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getStudio() { return studio; }
    public double getCriticsRating() { return criticsRating; }
    public String getPosterUrl() { return posterUrl; }
    public String getYear() { return year; }

    public String getPlot() { return plot; }

    public static class Rating implements Serializable {
        private static final long serialVersionUID = 1L;
        private String source;
        private String value;

        public Rating(String source, String value) {
            this.source = source;
            this.value = value;
        }

        public String getSource() {
            return source;
        }

        public String getValue() {
            return value;
        }
    }

}
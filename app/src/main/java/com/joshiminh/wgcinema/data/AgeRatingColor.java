package com.joshiminh.wgcinema.data;
import java.awt.Color;

public class AgeRatingColor {
    private static final String[] RATINGS = {"PG", "PG-13", "PG-16", "R"};
    private static final Color[] COLORS = {
        new Color(102, 255, 102), // Bright Green
        new Color(0, 128, 255),   // Bright Blue
        Color.ORANGE,             // Orange
        Color.RED                 // Red
    };

    /**
     * Returns the appropriate color for a given age rating.
     * 
     * @param ageRating The age rating string (e.g., "PG", "PG-13", "R")
     * @return Color representing the age rating
     */
    public static Color getColorForRating(String ageRating) {
        for (int i = 0; i < RATINGS.length; i++) {
            if (RATINGS[i].equals(ageRating)) {
                return COLORS[i];
            }
        }
        return Color.WHITE; // Default White
    }

    /**
     * Returns the array of supported age ratings.
     * 
     * @return String array of age ratings
     */
    public static String[] getRatings() {
        return RATINGS.clone();
    }
}

package com.joshiminh.wgcinema.utils;

import javax.swing.*;
import java.awt.Image;
import java.net.URL;

/**
 * Utility class for loading resources such as images and other assets.
 */
public class ResourceUtil {
    // Reference to the base class for resource loading
    private static final Class<?> BASE_CLASS = com.joshiminh.wgcinema.App.class;

    /**
     * Retrieves a resource as a URL based on the given path.
     *
     * @param path The relative path to the resource.
     * @return The URL of the resource, or null if not found.
     */
    public static URL getResource(String path) {
        return BASE_CLASS.getResource(path);
    }

    /**
     * Loads an image from the specified resource path.
     *
     * @param path The relative path to the image resource.
     * @return The loaded Image object, or null if the resource is not found.
     */
    public static Image loadImage(String path) {
        URL resource = getResource(path);
        return resource != null ? new ImageIcon(resource).getImage() : null;
    }

    /**
     * Loads the application's default icon image.
     *
     * @return The application's icon Image object, or null if not found.
     */
    public static Image loadAppIcon() {
        return loadImage("/images/icon.png");
    }
}
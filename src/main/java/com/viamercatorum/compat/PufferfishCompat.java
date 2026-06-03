package com.viamercatorum.compat;

public class PufferfishCompat {
    private boolean loaded;

    public PufferfishCompat() {
        try {
            Class.forName("pufferfishskills.PufferfishSkills");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
    }

    public boolean isLoaded() { return loaded; }
}

package com.cels.photofun;

/**
 * Created by Sasha on 02.11.2016.
 */
public class Cluster {
    private final int id;
    private int pixelCount;
    private int red;
    private int green;
    private int blue;
    private int reds;
    private int greens;
    private int blues;

    public Cluster(int id, int pixel) {

        this.id = id;
        addPixel(pixel);
    }

    int getId() {
        return id;
    }

    int getRGB() {
        int r = reds / pixelCount;
        int g = greens / pixelCount;
        int b = blues / pixelCount;
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    void addPixel(int pixel) {

        reds += (pixel >> 16) & 0xff;
        greens += (pixel >> 8) & 0xff;
        blues += pixel & 0xff;
        pixelCount++;
        red = reds / pixelCount;
        green = greens / pixelCount;
        blue = blues / pixelCount;
    }

    void removePixel(int pixel) {
        reds -= (pixel >> 16) & 0xff;
        greens -= (pixel >> 8) & 0xff;
        blues -= pixel & 0xff;
        pixelCount--;
        red = reds / pixelCount;
        green = greens / pixelCount;
        blue = blues / pixelCount;
    }

    int distance(int pixel) {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = pixel & 0xff;
        int dr = Math.abs(red - r);
        int dg = Math.abs(green - g);
        int db = Math.abs(blue - b);
        return (dr + dg + db) / 3;
    }
}

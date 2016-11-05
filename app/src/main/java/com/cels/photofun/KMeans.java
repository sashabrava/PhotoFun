package com.cels.photofun;

import android.graphics.Bitmap;

import java.util.Arrays;

/**
 * Created by Sasha on 02.11.2016.
 */
public class KMeans {
    private final Bitmap image;
    private Cluster[] clusters;
    private final int amountClusters;

    public KMeans(Bitmap image, int amountClusters) {
        this.image = image;
        this.amountClusters = amountClusters;
    }

    public Bitmap getResult() {

        int width = image.getWidth();
        int height = image.getHeight();
        clusters = createClusters(image, amountClusters);
        int[] idArray = new int[width * height];
        Arrays.fill(idArray, -1);
        boolean pixelChangedCluster = true;
        int iteration = 0;
        while (pixelChangedCluster) {
            pixelChangedCluster = false;
            iteration++;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getPixel(x, y);
                    Cluster cluster = findMinimalCluster(pixel);
                    if (idArray[x + width * y] != cluster.getId()) {

                        if (idArray[x + width * y] != -1) {

                            clusters[idArray[x + width * y]].removePixel(pixel);
                        }

                        cluster.addPixel(pixel);
                        pixelChangedCluster = true;
                        idArray[x + width * y] = cluster.getId();
                    }
                }
            }

        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int clusterId = idArray[width * y + x];
                result.setPixel(x, y, clusters[clusterId].getRGB());
            }
        }

        return result;
    }

    private Cluster findMinimalCluster(int rgb) {
        Cluster cluster = null;
        int min = Integer.MAX_VALUE;
        for (Cluster cluster1 : clusters) {
            int distance = cluster1.distance(rgb);
            if (distance < min) {
                min = distance;
                cluster = cluster1;
            }
        }
        return cluster;
    }

    private Cluster[] createClusters(Bitmap image, int k) {

        Cluster[] result = new Cluster[k];
        int x = 0;
        int y = 0;
        int dx = image.getWidth() / k;
        int dy = image.getHeight() / k;
        for (int i = 0; i < k; i++) {
            result[i] = new Cluster(i, image.getPixel(x, y));
            x += dx;
            y += dy;
        }
        return result;
    }
}

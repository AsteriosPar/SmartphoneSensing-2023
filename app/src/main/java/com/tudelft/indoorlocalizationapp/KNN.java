package com.tudelft.indoorlocalizationapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNN {

    // Define a method to calculate the Euclidean distance between two points
    private double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0.0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    // Define the KNN algorithm
    public int classify(double[][] jumping, double[][] walking,double [][] standing, double[] testData, int k) {
        List<Double[]> distances = new ArrayList<Double[]>();
        for (int i = 0; i < jumping.length; i++) {
            Double[] pair = {euclideanDistance(testData, jumping[i]), (double)1};
            distances.add(pair);
        }
        for (int i = 0; i < walking.length; i++) {
            Double[] pair = {euclideanDistance(testData, walking[i]), (double)2};
            distances.add(pair);
        }
        for (int i = 0; i < standing.length; i++) {
            Double[] pair = {euclideanDistance(testData, standing[i]), (double)3};
            distances.add(pair);
        }
        Collections.sort(distances, (a, b) -> a[0].compareTo(b[0]));
        Map<Double, Integer> classCounts = new HashMap<Double, Integer>();
        for (int i = 0; i < k; i++) {
            Double label = distances.get(i)[1];
            if (classCounts.containsKey(label)) {
                classCounts.put(label, classCounts.get(label) + 1);
            } else {
                classCounts.put(label, 1);
            }
        }
        Double prediction = 0.0;
        int maxCount = 0;
        for (Map.Entry<Double, Integer> entry : classCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                prediction = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return prediction.intValue();
    }

    // Test the KNN algorithm
//    public static void main(String[] args) {
//        // Sample data for demonstration
//        double[][] trainingData = {{2.0, 4.0}, {4.0, 2.0}, {4.0, 4.0}, {4.0, 6.0}, {6.0, 2.0}, {6.0, 4.0}};
//        int[] trainingLabels = {1, 1, 1, 2, 2, 2};
//        double[] testData = {6.0, 6.0};
//        int k = 3;
//
//        KNN knn = new KNN();
//        String prediction = knn.classify(trainingData, trainingLabels, testData, k);
//        System.out.println("Predicted label: " + prediction);
//    }
}
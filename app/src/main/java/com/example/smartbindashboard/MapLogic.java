package com.example.smartbindashboard;


import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;


public class MapLogic {

    public static class RoutePath {
        private String name;
        private List<Double> coordinates;

        public RoutePath(){}

        public RoutePath(String name, List<Double> coordinates) {
            this.name = name;
            this.coordinates = coordinates;
        }

        public String getName() {
            return name;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }
    }




    private static Integer[] slice(Integer[] array, int start, int end) {
        if(start >= array.length) return null;
        Integer[] slice = new Integer[end-start];
        for (int i = 0; i < slice.length; i++) slice[i] = array[start + i];

        return slice;
    }

    private static Integer[] concat(Integer[] A, Integer[] B) {
        if(B == null && A == null) {
            return null;
        }else if(A == null) {
            return B;
        } else if(B == null) {
            return A;
        }

        Integer[] concatArray = new Integer[A.length + B.length];
        int position = 0;
        for (Integer value : A) {
            concatArray[position] = value;
            position++;
        }

        for (Integer integer : B) {
            concatArray[position] = integer;
            position++;
        }

        return concatArray;
    }

    private ArrayList<Point> createDestinationPoints(ArrayList<Double[]> coordinates) {
        ArrayList<Point> points = new ArrayList<Point>();

        for(Double[] coordinateArray: coordinates) {
            points.add(Point.fromLngLat(coordinateArray[1], coordinateArray[0]));
        }

        return points;
    }

    private ArrayList<Double[]> getDurationMatrix(ArrayList<Double[]> coordinates, ArrayList<String> identifications, String accessToken) throws IOException {

        MapboxMatrix client = MapboxMatrix.builder()
                .user(DirectionsCriteria.PROFILE_DEFAULT_USER)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .coordinates(createDestinationPoints(coordinates))
                .addAnnotations(DirectionsCriteria.ANNOTATION_DURATION)
                .accessToken(accessToken)
                .build();

        Response<MatrixResponse> response = client.executeCall();

        if(response.code() == 200) {
            assert response.body() != null;
            return (ArrayList<Double[]>) response.body().durations();
        }

        return null;
    }

    private static ArrayList<Integer[]> permutations(ArrayList<Integer> vertex) {
        int n = vertex.size();

        Integer[] indices = new Integer[n];
        for(int i=0; i<n; i++) {
            indices[i] = i;
        }

        Integer[] cycles = new Integer[n];
        for(int i = n; i>0; i--) {
            cycles[n-i] = i;
        }

        ArrayList<Integer[]> results = new ArrayList<>();
        Integer[] res = new Integer[n];
        for(int i = 0; i< n; i++) {
            res[i] = vertex.get(indices[i]);
        }
        results.add(res);

        boolean broken  = false;
        while(n>0) {
            for(int i = n -1; i>=0; i--) {
                cycles[i]--;
                if(cycles[i] == 0) {
                    indices = concat(slice(indices, 0, i), concat(slice(indices, i+1, n), slice(indices, i, i+1)));

                    cycles[i] = n-i;
                    broken = false;
                } else {
                    int j = cycles[i];
                    int x = indices[i];
                    indices[i] = indices[n - j];
                    indices[n - j] = x;
                    Integer[] res1 = new Integer[n];
                    for(int k = 0; k< n; k++) {
                        res1[k] = vertex.get(indices[k]);
                    }
                    results.add(res1);
                    broken = true;
                    break;
                }
            }
            if(!broken) {
                break;
            }
        }
        return results;

    }

    private ArrayList<Integer> TTP(ArrayList<Double[]> graph, int V) {
        ArrayList<Integer> vertex = new ArrayList<>();
        for(int i=0; i<V; i++) {
            if(i != 0) {
                vertex.add(i);
            }
        }

        ArrayList<Integer> routePath = new ArrayList<>();
        double minPath = 1000000000.0;
        ArrayList<Integer[]> nextPermutations = permutations(vertex);

        for(Integer[] permutation: nextPermutations) {
            double currentPathweight = 0.0;
            ArrayList<Integer> currentPath = new ArrayList<>();

            int k = 0;
            for(Integer element: permutation) {
                currentPath.add(element);
                currentPathweight += graph.get(k)[element];
                k = element;
            }

            currentPathweight += graph.get(k)[0];

            if(currentPathweight < minPath) {
                minPath = currentPathweight;
                routePath = currentPath;
            }
        }
        return routePath;

    }


    public ArrayList<RoutePath> optimalPath(ArrayList<Double[]> coordinates, ArrayList<String> identifications, String accessToken) throws IOException {
        ArrayList<Double[]> graph = getDurationMatrix(coordinates, identifications, accessToken);
        if(graph == null){ return null; }


        ArrayList<RoutePath> routePaths = new ArrayList<>();
        ArrayList<Integer> optimalPath = TTP(graph, coordinates.size());


        for(int i=0; i<coordinates.size()-1; i++) {
            routePaths.add(new RoutePath(identifications.get(optimalPath.get(i)), Arrays.asList(coordinates.get(optimalPath.get(i)))));
        }

        return routePaths;

    }



}

package com.example.smartbindashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


class Data {
    private String name;
    private Double latitude;
    private Double longitude;
    private String state;
    private ArrayList<Double> coordinates;
    private ArrayList<Data> objectData;


    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    public ArrayList<Data> getObjectData() {
        return objectData;
    }
}




public class Map extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    // General variables
    DrawerLayout drawerLayout;
    ImageView imageViewFunctionsDrawer;
    LinearLayout linearLayoutMainMap, linearLayoutLocation, linearLayoutRubbishBin, linearLayoutClearPoint, linearLayoutRoute,
            linearLayoutNavigation, linearLayoutDashboard, linearLayoutCloud;
    MapView mapView;
    MapboxMap mapboxMap;
    PermissionsManager permissionsManager;
    Style mapStyle;
    NavigationMapRoute navigationMapRoute;

    ArrayList<String> navigationLayers = new ArrayList<String>() {
        {
            add("mapbox-navigation-arrow-shaft-casing-layer");
            add("mapbox-navigation-arrow-head-casing-layer");
            add("mapbox-navigation-route-shield-layer");
            add("mapbox-navigation-route-layer");
            add("mapbox-navigation-arrow-shaft-layer");
            add("mapbox-navigation-arrow-head-layer");
            add("mapbox-navigation-waypoint-layer");
        }
    };


    ArrayList<String> navigationSources = new ArrayList<String>() {
        {
            add("mapbox-navigation-waypoint-source");
            add("mapbox-navigation-route-source");
            add("mapbox-navigation-arrow-shaft-source");
            add("mapbox-navigation-arrow-head-source");
        }
    };

    // Location activity variables
    LocationComponent myLocation;

    // Rubbish bin variables
    GeoJsonSource rubbishBinGeoJsonSource;
    String rubbishBinName;
    Point rubbishBinLocation;


    // Route variables
    DirectionsRoute currentRoute;
    ArrayList<Data> routeData;
    SharedPreferences routePreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        // Variable initialization
        drawerLayout = findViewById(R.id.drawer_layout);
        imageViewFunctionsDrawer = findViewById(R.id.functionsDrawer);
        linearLayoutMainMap = findViewById(R.id.mainMap);

        linearLayoutLocation = findViewById(R.id.getLocation);
        linearLayoutRubbishBin = findViewById(R.id.rubbishBin);
        linearLayoutClearPoint = findViewById(R.id.clearPoint);
        linearLayoutRoute = findViewById(R.id.route);
        linearLayoutNavigation = findViewById(R.id.navigation);
        linearLayoutDashboard = findViewById(R.id.dashboard);
        linearLayoutCloud = findViewById(R.id.cloud);


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Fuck off

        routePreferences = getSharedPreferences("routeData",Context.MODE_PRIVATE);
        Date currentTime = Calendar.getInstance().getTime();
        long differenceInHours = 0;

        if(!routePreferences.getString("routes", "").equals("")) {

            // If user has acquired route data before, get the hours since the last retrieval
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            try {
                Date storedTime = timeFormat.parse(routePreferences.getString("Time", ""));
                differenceInHours = TimeUnit.MILLISECONDS.toHours(currentTime.getTime() - storedTime.getTime()) % 60;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // If the hours since the last retrieval exceed the 5 hour limit, delete route data
            if(differenceInHours > 5) {
                SharedPreferences.Editor editor = routePreferences.edit();
                editor.putString("routes", "");
            }
        }

        // Drawer icon
        imageViewFunctionsDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        // Map field
        linearLayoutMainMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close drawer
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // Location field
        linearLayoutLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressWarnings({"MissingPermission"})
            public void onClick(View view) {
                // If there is no marker for the user's location
                if (myLocation == null) {
                    enableLocationComponent();
                } else {
                    myLocation.setLocationComponentEnabled(false);
                    myLocation = null;
                    Toast.makeText(Map.this, "User location disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Cloud field
        linearLayoutCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user location is set and route data doesn't exist
                if(myLocation != null) {
                    if(routePreferences.getString("routes", "").equals("")) {
                        // Get route data
                        getData();
                    } else {
                        // Let user decide if new route data should be acquired
                        AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
                        builder.setMessage("Route data already exists. Do you want to get new data?");
                        builder.setTitle("Warning !");
                        builder.setCancelable(false);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getData();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                } else {
                    Toast.makeText(Map.this, "Set user location", Toast.LENGTH_LONG).show();
                }


            }
        });

        // Rubbish bin field
        linearLayoutRubbishBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myLocation != null) {

                    // If there is no marker for a rubbish bin in the map
                    if (rubbishBinGeoJsonSource == null) {

                        // If there are rubbish bins in the map
                        if(!routePreferences.getString("routes", "").equals("")) {
                            getRubbishBin();
                        } else {
                            Toast.makeText(Map.this, "No rubbish bin available", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        // Remove the rubbish bin
                        mapStyle.removeLayer("icon-layer-id");
                        mapStyle.removeImage("icon-icon-id");
                        mapStyle.removeSource("icon-source-id");
                        rubbishBinGeoJsonSource = null;
                        Toast.makeText(Map.this, "Rubbish bin disabled", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Map.this, "User location is not set", Toast.LENGTH_LONG).show();
                }
            }
        });


        // Clear point field
        linearLayoutClearPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If there is already a rubbish bin location set in the map
                if (rubbishBinGeoJsonSource != null) {
                    // Remove it
                    clearRubbishBinPoint();
                } else {
                    Toast.makeText(Map.this, "No rubbish bin selected", Toast.LENGTH_LONG).show();
                }

            }
        });


        // Route field
        linearLayoutRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user location and rubbish bin location are set
                if (myLocation != null && rubbishBinLocation != null) {
                    // If there is not a route
                    if(currentRoute == null) {
                        // Get route
                        getRoute();
                    } else {
                        // Remove route from map
                        navigationMapRoute.updateRouteArrowVisibilityTo(false);
                        navigationMapRoute.updateRouteVisibilityTo(false);
                        removeLayers(mapStyle.getLayers());
                        removeSources(mapStyle.getSources());
                        navigationMapRoute = null;
                        currentRoute = null;

                        Toast.makeText(Map.this, "Route disabled", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(Map.this, "Source/destination are not set", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Navigation field
        linearLayoutNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start navigation
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .waynameChipEnabled(true)
                        .shouldSimulateRoute(true)
                        .build();

                NavigationLauncher.startNavigation(Map.this, options);
                linearLayoutNavigation.setEnabled(true);
            }
        });

        // Back field
        linearLayoutDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exit prompt
                AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
                builder.setMessage("Do you want to exit?");
                builder.setTitle("Warning !");
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Map.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private interface getDataInterface {
        // Post Request
        @POST("/get_data")
        // Initialize getData call
        Call<Data> getData(@Body Data data);
    }

    private interface updateDataInterface {
        // Post request
        @POST("/update_data")
        // Initialize postData call
        Call<Data> postData(@Body Data data);
    }

    // Remove the route layers from map
    private void removeLayers(List<Layer> layers) {
        for(int i=0; i<navigationLayers.size(); i++) {
            mapStyle.removeLayer(navigationLayers.get(i));
        }
    }

    // Remove the route sources from map
    private void removeSources(List<Source> sources) {
        for(int i=0; i<navigationSources.size(); i++) {
            mapStyle.removeSource(navigationSources.get(i));
        }
    }


    // Save the route data to the shared preferences
    private void saveData(ArrayList<Data> data) {

        // Get current time
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        String time = timeFormat.format(currentTime);


        SharedPreferences.Editor editor = routePreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(data);

        editor.putString("routes", json);
        editor.putString("Time", time);
        editor.commit();
    }

    // Load the route data from the shared preferences
    private ArrayList<Data> loadData() {
        Gson gson = new Gson();

        String json = routePreferences.getString("routes", "");

        Type type = new TypeToken<ArrayList<Data>>() {}.getType();

        ArrayList<Data> routeData = gson.fromJson(json, type);

        return routeData;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapboxMap.getUiSettings().setLogoEnabled(false);
                mapboxMap.getUiSettings().setAttributionEnabled(false);
                mapStyle = style;
            }
        });
    }

    // Get user location
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent() {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, mapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            myLocation = locationComponent;

            Toast.makeText(this, "User location enabled", Toast.LENGTH_LONG).show();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }



    // Get route to destination
    private void getRoute() {

        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .origin(Point.fromLngLat(myLocation.getLastKnownLocation().getLongitude(), myLocation.getLastKnownLocation().getLatitude()))
                .destination(rubbishBinLocation)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null || response.body().routes().size() < 1) {
                            Toast.makeText(Map.this, "No routes found", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Get current route
                        currentRoute = response.body().routes().get(0);


                        // Draw route on map
                        navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        navigationMapRoute.addRoute(currentRoute);

                        Toast.makeText(Map.this, "Route enabled", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast.makeText(Map.this, "Error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Add marker to the map
    private void addRubbishBinSymbol() {
        mapStyle.addImage("icon-icon-id", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_red_marker, null)));

        mapStyle.addLayer(new SymbolLayer("icon-layer-id", "icon-source-id").withProperties(PropertyFactory.iconImage("icon-icon-id"),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(new Float[] {0f, -9f})));
        Toast.makeText(this, "Rubbish bin added", Toast.LENGTH_LONG).show();
    }

    // Get nearest rubbish bin from shared preferences
    private void getRubbishBin() {
        routeData = loadData();

        // Get rubbish bin name
        rubbishBinName = routeData.get(0).getName();

        // Add rubbish bin source location and symbol
        rubbishBinGeoJsonSource = new GeoJsonSource("icon-source-id", FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(routeData.get(0).getCoordinates().get(1), routeData.get(0).getCoordinates().get(0)))}));
        mapStyle.addSource(rubbishBinGeoJsonSource);
        rubbishBinLocation = Point.fromLngLat(routeData.get(0).getCoordinates().get(1), routeData.get(0).getCoordinates().get(0));
        addRubbishBinSymbol();
    }


    // Remove marker(rubbish bin) from map, shared preferences and update database
    private void clearRubbishBinPoint() {
        // Initialize retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.2.56:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize interface
        Map.updateDataInterface inter = retrofit.create(Map.updateDataInterface.class);

        // Pass input values
        Data data = new Data();
        data.setName(rubbishBinName);
        Call<Data> call = inter.postData(data);
        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                // Check condition
                if(response.isSuccessful() && response.body() != null && response.body().getState().equals("Success")) {
                    // Reset rubbish bin status
                    mapStyle.removeLayer("icon-layer-id");
                    mapStyle.removeImage("icon-icon-id");
                    mapStyle.removeSource("icon-source-id");
                    rubbishBinGeoJsonSource = null;
                    rubbishBinName = null;
                    rubbishBinLocation = null;
                    currentRoute = null;
                    routeData.remove(0);

                    // If route data is not empty, update the route data from the shared preferences
                    if(routeData.size() >= 1) {
                        saveData(routeData);
                    } else {
                        // Remove routes so that the user can receive new route data
                        SharedPreferences.Editor editor = routePreferences.edit();

                        editor.putString("routes", "");
                        editor.commit();
                    }


                    Toast.makeText(Map.this, "Rubbish bin removed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) { Toast.makeText(Map.this, "Failed To Remove", Toast.LENGTH_SHORT).show(); }
        });
    }



    // Get route data from mapbox service
    private void getData() {
        // Initialize retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.2.56:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize interface
        Map.getDataInterface inter = retrofit.create(Map.getDataInterface.class);


        // Pass input values
        Data userLocation = new Data();
        userLocation.setLatitude(myLocation.getLastKnownLocation().getLatitude());
        userLocation.setLongitude(myLocation.getLastKnownLocation().getLongitude());

        Call<Data> call = inter.getData(userLocation);

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if(response.isSuccessful() && response.body() != null) {
                    // Get list of rubbish bins
                    Data objectData = response.body();
                    ArrayList<Data> data = objectData.getObjectData();

                    // If route data is not empty, save it to the shared preferences
                    if(data.size() >= 1) {
                        Toast.makeText(Map.this, "Data acquired", Toast.LENGTH_SHORT).show();
                        saveData(data);

                    } else {
                        Toast.makeText(Map.this, "No data available", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Toast.makeText(Map.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Permission Is Needed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
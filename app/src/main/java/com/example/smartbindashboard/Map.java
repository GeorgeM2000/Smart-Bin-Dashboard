package com.example.smartbindashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Map extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    DrawerLayout drawerLayout;
    ImageView imageViewFunctionsDrawer;
    LinearLayout linearLayoutMainMap, linearLayoutLocation, linearLayoutRubbishBin, linearLayoutClearPoint, linearLayoutRoute,
            linearLayoutNavigation, linearLayoutDashboard, linearLayoutOptimalPath;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private Style mapStyle;
    private NavigationMapRoute navigationMapRoute;
    private DatabaseReference databaseReference;

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
    private LocationComponent myLocation;

    // Rubbish bin variables
    GeoJsonSource rubbishBinGeoJsonSource;
    String rubbishBinName, rubbishBinRegion;
    Point rubbishBinLocation;


    // Route variables
    private DirectionsRoute currentRoute;
    private SharedPreferences routePreferences;
    private ArrayList<Login.Region> regions;

    // Update database variables
    boolean updatePermission;
    private ValueEventListener routesValueEventListener;
    private ChildEventListener devicesChildEventListener;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
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
        linearLayoutOptimalPath = findViewById(R.id.optimal_path);


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        routePreferences = getApplicationContext().getSharedPreferences("Route_Preferences", Context.MODE_PRIVATE);
        regions = loadRegions();

        // Listener for the routes location
        routesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()) {
                    SharedPreferences.Editor editor = routePreferences.edit();
                    editor.putString("Routes", "");
                    editor.putInt("Current_Route_Index", -1);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };


        // Listener for the devices location
        devicesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(updatePermission) {
                        if(validateRegions(snapshot.getKey())) {
                            Thread thread = new Thread(() -> setOptimalPath(Double.valueOf(routePreferences.getString("User_Location_Latitude", "")), Double.valueOf(routePreferences.getString("User_Location_Longitude", ""))));

                            thread.start();

                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        updatePermission = true;
                    }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        // Set route listener
        routeListener();

        // Set device listener
        devicesListener();


        // Drawer icon
        imageViewFunctionsDrawer.setOnClickListener(view -> {
            // Open drawer
            drawerLayout.openDrawer(GravityCompat.START);
        });


        // Map field
        linearLayoutMainMap.setOnClickListener(view -> {
            // Close drawer
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        // Location field
        linearLayoutLocation.setOnClickListener(view -> {
            if (myLocation == null) {
                enableLocationComponent();
            } else {
                // Disable location marker
                myLocation.setLocationComponentEnabled(false);
                myLocation = null;
                Toast.makeText(Map.this, "User location disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Set optimal path field
        linearLayoutOptimalPath.setOnClickListener(view -> {
            if(routePreferences.getString("Routes", "").equals("")) {
                if (routePreferences.getBoolean("User_Location_Set", false)) {
                    setOptimalPath(Double.valueOf(routePreferences.getString("User_Location_Latitude", "")), Double.valueOf(routePreferences.getString("User_Location_Longitude", "")));
                } else {
                    Toast.makeText(getApplicationContext(), "Set location", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Let user decide if new route data should be acquired
                AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
                builder.setMessage("Route data already exists. Do you want to get new data?");
                builder.setTitle("Warning !");
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    if (routePreferences.getBoolean("User_Location_Set", false)) {
                        setOptimalPath(Double.valueOf(routePreferences.getString("User_Location_Latitude", "")), Double.valueOf(routePreferences.getString("User_Location_Longitude", "")));
                    } else {
                        Toast.makeText(getApplicationContext(), "Set location", Toast.LENGTH_SHORT).show();
                    }
                    dialogInterface.cancel();
                });

                builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // Rubbish bin field
        linearLayoutRubbishBin.setOnClickListener(view -> {
            if (myLocation != null) {
                if (rubbishBinGeoJsonSource == null) {
                    if (routePreferences.getString("Routes", "").equals("true")) {
                        getRubbishBin();
                    } else {
                        Toast.makeText(Map.this, "No rubbish bin available", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // Remove rubbish bin marker
                    mapStyle.removeLayer("icon-layer-id");
                    mapStyle.removeImage("icon-icon-id");
                    mapStyle.removeSource("icon-source-id");
                    rubbishBinGeoJsonSource = null;
                    Toast.makeText(Map.this, "Rubbish bin disabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Map.this, "User location is not set", Toast.LENGTH_LONG).show();
            }
        });


        // Clear point field
        linearLayoutClearPoint.setOnClickListener(view -> {
            if(routePreferences.getString("Routes", "").equals("true")) {
                if (rubbishBinGeoJsonSource != null) {
                    removeRubbishBin();
                } else {
                    Toast.makeText(Map.this, "No rubbish bin selected", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No rubbish bin available", Toast.LENGTH_LONG).show();
            }

        });


        // Route field
        linearLayoutRoute.setOnClickListener(view -> {
            if (myLocation != null) {
                if(rubbishBinLocation != null) {
                    if (currentRoute == null) {
                        getRoute();
                    } else {
                        // Remove route from map
                        navigationMapRoute.updateRouteArrowVisibilityTo(false);
                        navigationMapRoute.updateRouteVisibilityTo(false);
                        removeLayers();
                        removeSources();
                        navigationMapRoute = null;
                        currentRoute = null;

                        Toast.makeText(Map.this, "Route disabled", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Map.this, "Rubbish bin location is not set", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Map.this, "User location is not set", Toast.LENGTH_LONG).show();
            }
        });

        // Navigation field
        linearLayoutNavigation.setOnClickListener(view -> {
            if(currentRoute != null) {
                // Start navigation
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .waynameChipEnabled(true)
                        .shouldSimulateRoute(true)
                        .build();

                NavigationLauncher.startNavigation(Map.this, options);
                linearLayoutNavigation.setEnabled(true);
            } else {
                Toast.makeText(this, "Route is not set", Toast.LENGTH_LONG).show();
            }
        });

        // Back field
        linearLayoutDashboard.setOnClickListener(view -> {
            // Exit prompt
            AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
            builder.setMessage("Do you want to exit?");
            builder.setTitle("Warning !");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                Intent intent = new Intent(Map.this, Dashboard.class);
                startActivity(intent);
                finish();
            });

            builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }


    // Remove the route layers from map
    private void removeLayers() {
        for (int i = 0; i < navigationLayers.size(); i++) {
            mapStyle.removeLayer(navigationLayers.get(i));
        }
    }

    // Remove the route sources from map
    private void removeSources() {
        for (int i = 0; i < navigationSources.size(); i++) {
            mapStyle.removeSource(navigationSources.get(i));
        }
    }


    private void devicesListener() {
        databaseReference
                .child("Devices")
                .child("Kozani")
                .addChildEventListener(devicesChildEventListener);
    }


    // Load the regions from the shared preferences
    private ArrayList<Login.Region> loadRegions() {
        Gson gson = new Gson();

        String json = routePreferences.getString("Regions", "");

        Type type = new TypeToken<ArrayList<Login.Region>>() {}.getType();

        return gson.fromJson(json, type);
    }

    // Validate regions
    private boolean validateRegions(String childKey) {
        for(Login.Region region: regions){
            if(region.getName().equals(childKey)) return true;
        }
        return false;
    }


    // Remove rubbish bin
    private void removeRubbishBin() {
        updatePermission = false;
        databaseReference.child("Devices").child("Kozani").child(rubbishBinRegion).child(rubbishBinName).updateChildren(new HashMap<String, Object>(){{put("state", 0);}});

        databaseReference
                .child("Routes")
                .child("Kozani")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(String.valueOf(routePreferences.getInt("Current_Route_Index", 0))).removeValue().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // Remove rubbish bin from the map
                        mapStyle.removeLayer("icon-layer-id");
                        mapStyle.removeImage("icon-icon-id");
                        mapStyle.removeSource("icon-source-id");
                        rubbishBinGeoJsonSource = null;
                        rubbishBinName = null;
                        rubbishBinLocation = null;
                        currentRoute = null;

                        SharedPreferences.Editor editor = routePreferences.edit();
                        editor.putInt("Current_Route_Index", routePreferences.getInt("Current_Route_Index", 0) + 1);
                        editor.apply();

                        Toast.makeText(Map.this, "Rubbish bin removed", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Map.this, "Failed to remove rubbish bin", Toast.LENGTH_LONG).show();
                    }
                });
    }


    // Save route data
    private void saveRouteData(List<MapLogic.RoutePath> routePaths) {
       databaseReference
                .child("Routes")
                .child("Kozani")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .setValue(routePaths)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Route data acquired", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = routePreferences.edit();
                        editor.putString("Routes", "true");
                        editor.putInt("Current_Route_Index", 0);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to acquire route data", Toast.LENGTH_LONG).show();
                    }
                });


    }

    // Add marker to the map
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addRubbishBinSymbol() {
        mapStyle.addImage("icon-icon-id", Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_red_marker, null))));

        mapStyle.addLayer(new SymbolLayer("icon-layer-id", "icon-source-id").withProperties(PropertyFactory.iconImage("icon-icon-id"),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(new Float[]{0f, -9f})));

        Toast.makeText(this, "Rubbish bin added", Toast.LENGTH_LONG).show();
    }

    // Get rubbish bin
    private void getRubbishBin() {
        databaseReference
                .child("Routes")
                .child("Kozani")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(String.valueOf(routePreferences.getInt("Current_Route_Index", 0)))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        rubbishBinName = Objects.requireNonNull(task.getResult().child("name").getValue()).toString();
                        rubbishBinRegion = Objects.requireNonNull(task.getResult().child("region").getValue()).toString();

                        // Add rubbish bin source location and icon
                        rubbishBinGeoJsonSource = new GeoJsonSource("icon-source-id", FeatureCollection.fromFeatures(new Feature[] {
                                Feature.fromGeometry(Point.fromLngLat(Double.parseDouble(Objects.requireNonNull(task.getResult().child("coordinates").child("1").getValue()).toString()), Double.parseDouble(Objects.requireNonNull(task.getResult().child("coordinates").child("0").getValue()).toString())))}));
                        mapStyle.addSource(rubbishBinGeoJsonSource);
                        rubbishBinLocation = Point.fromLngLat(Double.parseDouble(Objects.requireNonNull(task.getResult().child("coordinates").child("1").getValue()).toString()), Double.parseDouble(Objects.requireNonNull(task.getResult().child("coordinates").child("0").getValue()).toString()));
                        addRubbishBinSymbol();
                    } else {
                        Toast.makeText(this, "Failed to get rubbish bin location", Toast.LENGTH_LONG).show();
                    }

                });
    }


    // Route listener
    private void routeListener() {
        databaseReference
                .child("Routes")
                .child("Kozani")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addValueEventListener(routesValueEventListener);
    }


    private void getOptimalPath(DataSnapshot snapshot, Double userLat, Double userLong) {
        ArrayList<Double[]> coordinates = new ArrayList<>();
        ArrayList<String> identifications = new ArrayList<>();
        HashMap<String, String> rubbishBinNames = new HashMap<>();
        MapLogic mapLogic = new MapLogic();

        identifications.add("user");
        coordinates.add(new Double[]{userLat, userLong});

        for(Login.Region region: regions) {
            for (DataSnapshot dataSnapshot: snapshot.child(region.getName()).getChildren()) {
                if(Objects.requireNonNull(dataSnapshot.child("state").getValue()).toString().equals("1")) {
                    coordinates.add(new Double[]{Double.valueOf(Objects.requireNonNull(dataSnapshot.child("latitude").getValue()).toString()), Double.valueOf(Objects.requireNonNull(dataSnapshot.child("longitude").getValue()).toString())});
                    identifications.add(dataSnapshot.getKey());
                    rubbishBinNames.put(dataSnapshot.getKey(), region.getName());
                }
            }
        }

        if(coordinates.size() >= 1) {
            try {
                List<MapLogic.RoutePath> routePaths = mapLogic.optimalPath(coordinates, identifications, getString(R.string.mapbox_access_token), rubbishBinNames);
                if (routePaths == null) {
                    Toast.makeText(getApplicationContext(), "Failed to retrieve route data", Toast.LENGTH_LONG).show();
                } else {
                    saveRouteData(routePaths);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No rubbish bins available", Toast.LENGTH_LONG).show();
        }

    }


    // Set optimal path
    public void setOptimalPath(Double userLat, Double userLong) {
        databaseReference
                .child("Devices")
                .child("Kozani")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Thread currentThread = new Thread(() -> getOptimalPath(task.getResult(), userLat, userLong));

                        currentThread.start();

                        try {
                            currentThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(Map.this, "Failed to retrieve route data", Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            mapboxMap.getUiSettings().setLogoEnabled(false);
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapStyle = style;
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

            // Store initial user location coordinates
            if(!routePreferences.getBoolean("User_Location_Set", false)) {
                SharedPreferences.Editor editor = routePreferences.edit();
                editor.putString("User_Location_Latitude", String.valueOf(Objects.requireNonNull(myLocation.getLastKnownLocation()).getLatitude()));
                editor.putString("User_Location_Longitude", String.valueOf(myLocation.getLastKnownLocation().getLongitude()));
                editor.putBoolean("User_Location_Set", true);
                editor.apply();
            }

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
                .origin(Point.fromLngLat(Objects.requireNonNull(myLocation.getLastKnownLocation()).getLongitude(), myLocation.getLastKnownLocation().getLatitude()))
                .destination(rubbishBinLocation)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        if (response.body() == null || response.body().routes().size() < 1) {
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
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                        Toast.makeText(Map.this, "Failed to get route", Toast.LENGTH_SHORT).show();
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
    protected void onSaveInstanceState(@NonNull Bundle outState){
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
        databaseReference.child("Devices").child("Kozani").removeEventListener(devicesChildEventListener);
        databaseReference.child("Routes").child("Kozani").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).removeEventListener(routesValueEventListener);
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
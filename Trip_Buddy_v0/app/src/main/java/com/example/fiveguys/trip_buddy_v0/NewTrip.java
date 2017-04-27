package com.example.fiveguys.trip_buddy_v0;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.R.id.url;

public class NewTrip extends FragmentActivity
        implements OnMapReadyCallback,
                    GoogleMap.OnMyLocationButtonClickListener,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    public GoogleApiClient mGoogleApiClient;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private static final String TAG = NewTrip.class.getSimpleName();
    private Location mLastKnownLocation;
    private LatLng cLocation = new LatLng(0, 0);
    private LatLng startLocation = new LatLng(0, 0);
    private LatLng sLocation = new LatLng(0, 0);;
    private CharSequence sPlace="None";
    private FirebaseUser user;
    private String uid;
    private Uri photoUrl;
    private DatabaseReference Users;
    private StorageReference mStorageRef;
    private CharSequence sAddress;
    private String cAddress, startAddress;
    private LatLngBounds sBound, cBound;
    private String cPlaceNames, cPlaceid, cPlaceAddresses,cPlaceAttributions;
    private LatLng cPlaceLatLngs;
    private boolean DesLayout;

    private String sCity;
    private String sId;
    private TextView Destination, Start;
    private Button Go, Next;
    private ImageView PlaceImage;
    Typeface athletic_font;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        athletic_font = Typeface.createFromAsset(getAssets(), "fonts/athletic.ttf");
        if (savedInstanceState != null) {
            DesLayout = savedInstanceState.getBoolean("DesLayout");
            startAddress = savedInstanceState.getString("cAddress");
            startLocation = new LatLng(savedInstanceState.getDouble("startLat"),savedInstanceState.getDouble("startLong"));
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        if (user != null) {
            uid = user.getUid();
        }
        Users = FirebaseDatabase.getInstance().getReference().child("users");

        if(DesLayout) {
           loadDesLayout();
        }else {
            setContentView(R.layout.activity_new_trip_start);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
            mGoogleApiClient.connect();
            DesLayout = false;
            Start = (TextView) findViewById(R.id.start);

            Next = (Button) findViewById(R.id.Next);
            Next.setTypeface(athletic_font);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            Next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DesLayout = true;
                    recreate();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("DesLayout", DesLayout);
        outState.putString("cAddress", cAddress);
        outState.putDouble("startLong", cLocation.longitude);
        outState.putDouble("startLat", cLocation.latitude);
        super.onSaveInstanceState(outState);
    }

    private void loadDesLayout(){
        setContentView(R.layout.activity_new_trip);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
        Destination = (TextView)findViewById(R.id.destination);
        Go = (Button) findViewById(R.id.Go);
        Go.setTypeface(athletic_font);
        PlaceImage = (ImageView) findViewById(R.id.placeImage);
        //getActionBar().hide();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sId == null){
                    Toast toast = Toast.makeText(getApplicationContext(), "Search For a Destination", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    //placePhotosTask();
                    String uid = user.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DateFormat df = new SimpleDateFormat("MM_dd_yy");
                    Date dateobj = new Date();
                    String timestamp = df.format(dateobj);
                    startAddress = startAddress.replace("\n", ", ");
                    DatabaseReference newTrip = database.getReference("/trips/"+sId);
                    //placePhotosTask();
                    newTrip.child(startAddress).child(uid).setValue(true);
                    DatabaseReference trip = database.getReference("/users/"+uid+"/trips/"+sId+"/"+timestamp);
                    trip.child("startAddress").setValue(startAddress);
                    trip.child("startLocation").setValue(startLocation);
                    trip.child("destinationName").setValue(sPlace);
                    trip.child("destinationAddress").setValue(sAddress);
                    trip.child("destinationLocation").setValue(sLocation);
                    trip.child("photoUrl").setValue(photoUrl.toString());
                    trip.child("activity").setValue(true);
                    Toast toast = Toast.makeText(getApplicationContext(), "Your Jouney Begins", Toast.LENGTH_SHORT);
                    toast.show();
                    DesLayout = false;
//                    Intent intent = new Intent(getApplicationContext(), Main.class);
//                    startActivity(intent);
                    finish();

                } else {
                    DesLayout = false;
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                if(DesLayout) {
                    autocompleteFragment.setFilter(new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                            .build());
                }
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override

            public void onPlaceSelected(Place place) {
                if(DesLayout) {
                    sPlace = place.getName();
                    sLocation = place.getLatLng();
                    sId = place.getId();
                    sAddress = place.getAddress();
                    sLocation = place.getLatLng();
                    sBound = place.getViewport();
                    showLocation();
                    Log.i(TAG, "Place: " + place.getName());
                }else{
                    cLocation = place.getLatLng();
                    cBound = place.getViewport();
                    StringBuffer address = new StringBuffer();
                    StringBuffer address1 = new StringBuffer();
                    StringBuffer address2 = new StringBuffer();
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = gcd.getFromLocation(cLocation.latitude, cLocation.longitude, 1);

                        if (addresses.size() > 0)
                            System.out.println(addresses.get(0).getLocality());
                        address.append(addresses.get(0).getAddressLine(1)).append("\n")
                                .append(addresses.get(0).getAddressLine(2));
                        address1.append(addresses.get(0).getAddressLine(1));
                        address2.append(addresses.get(0).getAddressLine(2));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cAddress = address.toString();
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .title(address1.toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin))
                            .position(cLocation)
                            .snippet(address2.toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(cBound,10));
                    Start.setText(address1);
                }
            }
            @Override
            public void onError(Status status) {
                Context context = getApplicationContext();
                CharSequence text = "place not found";
                Toast toast = Toast.makeText(context,text,Toast.LENGTH_SHORT);
                toast.show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

            @Override
            public void onConnectionFailed(@NonNull ConnectionResult result) {
                // Refer to the reference doc for ConnectionResult to see what error codes might
                // be returned in onConnectionFailed.
                Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                        + result.getErrorCode());
            }

            /**
             * Handles suspension of the connection to the Google Play services client.
             */
            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "Play services connection suspended");
            }


            /**
             * Manipulates the map when it's available.
             * This callback is triggered when the map is ready to be used.
             */
            @Override
            public void onMapReady(GoogleMap map) {
                mMap = map;
                mMap.setPadding(0,150,5,200);
                mMap.setOnMyLocationButtonClickListener(this);
                getDeviceLocation();
                updateLocationUI();
                // showCurrentPlace();
            }

    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }
        if (mLastKnownLocation != null) {
            showCurrentPlace();
        }else{
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            StringBuffer address = new StringBuffer();
            StringBuffer address1 = new StringBuffer();
            StringBuffer address2 = new StringBuffer();
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);

                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                address.append(addresses.get(0).getAddressLine(1)).append("\n")
                        .append(addresses.get(0).getAddressLine(2));
                address1.append(addresses.get(0).getAddressLine(1));
                address2.append(addresses.get(0).getAddressLine(2));

            } catch (IOException e) {
                e.printStackTrace();
            }

            cAddress = address.toString();

            cLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .title(address1.toString())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin))
                    .position(cLocation)
                    .snippet(address2.toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cLocation, DEFAULT_ZOOM));
            if(!DesLayout) {
                Start.setText(address1);
            }
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
        }
    }

    private void placePhotosTask() {
        mStorageRef.child("placePhoto/"+sId+".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                photoUrl = uri;
//                Toast toast = Toast.makeText(getApplicationContext(),"found",Toast.LENGTH_SHORT);
//                toast.show();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                new PhotoTask(R.dimen.photo_height*2, R.dimen.photo_height) {
                    @Override
                    protected void onPreExecute() {
                        // Display a temporary image to show while bitmap is loading.
                       // PlaceImage.setImageResource(R.drawable.logo_white_outline);
                    }

                    @Override
                    protected void onPostExecute(AttributedPhoto attributedPhoto) {
                        if (attributedPhoto != null) {
                            // Photo has been loaded, display it.

                           // PlaceImage.setImageBitmap(attributedPhoto.bitmap);
                            try {
                                FileOutputStream fos = openFileOutput(sId + ".png", Context.MODE_PRIVATE);

                                attributedPhoto.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                String mypath = NewTrip.this.getFilesDir().getAbsolutePath() + "/" + sId + ".png";

                                Uri file = Uri.fromFile(new File(mypath));
                                StorageReference profilesRef = mStorageRef.child("placePhoto/" + sId + ".png");

                                profilesRef.putFile(file)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // Get a URL to the uploaded content
                                                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                                // Users.child("trips").child(sId).child("photoUrl").setValue(downloadUrl);
                                                photoUrl = downloadUrl;
                                            }
                                        })

                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle unsuccessful uploads
                                                // ...
                                            }
                                        });
                                fos.close();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                    // Display the attribution as HTML content if set.
//                    if (attributedPhoto.attribution == null) {
//                        mText.setVisibility(View.GONE);
//                    } else {
//                        mText.setVisibility(View.VISIBLE);
//                        mText.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
//                    }

                        }
                    }
                }.execute(sId);
            }
        });
    }

    abstract class PhotoTask extends AsyncTask<String, Void, com.example.fiveguys.trip_buddy_v0.NewTrip.PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }
        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected com.example.fiveguys.trip_buddy_v0.NewTrip.PhotoTask.AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            com.example.fiveguys.trip_buddy_v0.NewTrip.PhotoTask.AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new com.example.fiveguys.trip_buddy_v0.NewTrip.PhotoTask.AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

            /**
             * Holder for an image and its attribution.
             */
            class AttributedPhoto {

                public final CharSequence attribution;

                public final Bitmap bitmap;

                public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                    this.attribution = attribution;
                    this.bitmap = bitmap;
                }
            }

    }

        private void showLocation(){
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                .title((String)sPlace)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_orange_arrow))
                .position(sLocation)
                .snippet((String)sAddress));
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(sBound,10));
            Destination.setText(sAddress);
            placePhotosTask();
    }
    public boolean onMyLocationButtonClick() {
        getDeviceLocation();
       // showCurrentPlace();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

}

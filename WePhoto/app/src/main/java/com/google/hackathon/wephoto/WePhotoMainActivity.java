package com.google.hackathon.wephoto;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WePhotoMainActivity extends FragmentActivity implements
        ActionBar.TabListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = WePhotoMainActivity.class.getSimpleName();
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private String currentEvent;
    private Date eventTime;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_we_photo);

        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        String[] tabs = {getString(R.string.takePhotos), getString(R.string.gallery),
                getString(R.string.previousEvents)};
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        /**
         * Set up google drive connection.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("", connectionResult.toString());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    /**
     * Create a new file and save it to Drive.
     */
    public void saveImageToDrive(final Bitmap imageData, final String filePath) {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(
                new ResultCallback<DriveApi.ContentsResult>() {
            @Override
            public void onResult(DriveApi.ContentsResult result) {
                // If the operation was not successful, we cannot do anything and must fail.
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Failed to create new contents.");
                    return;
                }
                // Otherwise, we can write our data to the new contents.
                Log.i(TAG, "New contents created.");
                // Get an output stream for the contents.
                OutputStream outputStream = result.getContents().getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                imageData.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                try {
                    outputStream.write(bitmapStream.toByteArray());
                } catch (IOException e1) {
                    Log.e(TAG, "Unable to write file contents.");
                }
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("image/png").setTitle(filePath).build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialContents(result.getContents())
                        .build(mGoogleApiClient);
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "Failed to launch file chooser.");
                }
            }
        });
    }

    /**
     * Create a new folder in the root folder on drive with a default callback.
     */
    public void createDriveFolder(String folderName) {
        createDriveFolder(folderName, new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while trying to create the folder");
                    return;
                }
                Log.i(TAG, "Created a folder: " + result.getDriveFolder().getDriveId());
            }
        });
    }

    /**
     * Creates a new folder in the root folder with a specified callback.
     */
    public void createDriveFolder(
            String folderName, ResultCallback<DriveFolder.DriveFolderResult> resultCallback) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName).build();
        Future<DriveFolder.DriveFolderResult> result;
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet)
                .setResultCallback(resultCallback);
    }

    private boolean hasCurrentEventBeenSetInLastTwoHours() {
        if (currentEvent == null) return false;

        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.HOUR, -2);
        Date twoHoursAgo = c.getTime();
        return eventTime.compareTo(twoHoursAgo) < 0;
    }

    private boolean modifiedLessThanThirtyMinutesAgo(Date modified) {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(modified);
        c.add(Calendar.MINUTE, 30);
        Date thirtyMinutesLater = c.getTime();
        return now.compareTo(thirtyMinutesLater) > 0;
    }

    private ArrayList<DriveFolder> getSetOfDirectories() {
        return new ArrayList<DriveFolder>(); // TODO: actually query docs. (jakemac?)
    }

    public void updateCurrentEvent() {
        if (!hasCurrentEventBeenSetInLastTwoHours()) {
            currentEvent = null;
            //   display "looking for event" // in gallery fragment code..
            //   get all events. Find the one that is closest(or create one). Then display all the pictures IN that event.
            for (final DriveFolder d : getSetOfDirectories()) {
                // TODO: fire up another thread for this.
                d.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                    @Override
                    public void onResult(DriveResource.MetadataResult metadataResult) {
                        if (metadataResult.getStatus().getStatusCode() != CommonStatusCodes.TIMEOUT) {
                            Date dateModified = metadataResult.getMetadata().getModifiedDate();
                            if (modifiedLessThanThirtyMinutesAgo(dateModified)) {
                                currentEvent = metadataResult.getMetadata().getTitle();
                                eventTime = new Date();
                                // TODO: within a certain range of geolocation

                            }
                        } else {
                            // No recent events. Create a new event!
                            currentEvent = "geographic location here";
                            eventTime = new Date();
                            MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                            builder.setTitle(currentEvent);
                            d.createFolder(mGoogleApiClient, builder.build());
                        }
                    }
                }, 1, TimeUnit.MINUTES);
            }

            listRootFolderContents(new EventsAdapter(this));
        }
    }

    /**
     * List contents of the root drive folder and append them to resultsAdaptor.
     */
    public void listRootFolderContents(final DataBufferAdapter<Metadata> resultsAdaptor) {
        listDriveFolderContents(
                Drive.DriveApi.getRootFolder(mGoogleApiClient).getDriveId(), resultsAdaptor);
    }

    /**
     * List contents of a folder in your google drive and append them to resultsAdaptor.
     */
    public void listDriveFolderContents(
            DriveId folderId, final DataBufferAdapter<Metadata> resultsAdaptor) {
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, folderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("MyNewFolder").build();
        folder.listChildren(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Problem while retrieving files");
                            return;
                        }
                        resultsAdaptor.append(result.getMetadataBuffer());
                        Log.i(TAG, "Successfully listed files.");
                    }
                });
    }
}
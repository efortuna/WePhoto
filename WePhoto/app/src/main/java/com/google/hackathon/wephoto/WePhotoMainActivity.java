package com.google.hackathon.wephoto;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.query.Filter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    public DriveId currentEvent;
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
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

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
     * Creates a new folder in the root folder with a specified callback.
     */
    public PendingResult<DriveFolder.DriveFolderResult> createDriveFolder(String folderName) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
        return Drive.DriveApi.getRootFolder(mGoogleApiClient)
                .createFolder(mGoogleApiClient, changeSet);
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
                                currentEvent = metadataResult.getMetadata().getDriveId();
                                eventTime = new Date();
                                // TODO: within a certain range of geolocation

                            }
                        } else {
                            // No recent events. Create a new event!
                            String currentEventName = "geographic location here";
//                            eventTime = new Date();
                            createDriveFolder(currentEventName).setResultCallback(
                                    new ResultCallback<DriveFolder.DriveFolderResult>() {
                                @Override
                                public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                                    if (!driveFolderResult.getStatus().isSuccess()) {
                                        Log.e(TAG, "Failed to create event!");
                                        return;
                                    }
                                    currentEvent = driveFolderResult.getDriveFolder().getDriveId();
                                }
                            });
                        }
                    }
                }, 1, TimeUnit.MINUTES);
            }

            // TODO: update the PreviousEventsFragment somehow with this.
//            listDriveFolderContents(new EventsAdapter(this));
        }
    }

    /**
     * List contents of a folder in your google drive and append them to resultsAdaptor.
     */
    public PendingResult<DriveApi.MetadataBufferResult> listDriveFolderContents(String pageToken) {
        return listDriveFolderContents(new ArrayList<Filter>(), pageToken);
    }

    /**
     * List contents of a folder in your google drive and append them to resultsAdaptor.
     */
    public PendingResult<DriveApi.MetadataBufferResult> listDriveFolderContents(
            Iterable<Filter> filters, String pageToken) {
        Query.Builder queryBuilder = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .addFilter(Filters.and(filters));
        if (pageToken != null) {
            queryBuilder.setPageToken(pageToken);
        }
        Query query = queryBuilder.build();
        return Drive.DriveApi.query(mGoogleApiClient, query);
//        return Drive.DriveApi.getRootFolder(mGoogleApiClient).listChildren(mGoogleApiClient)
    }

    /**
     * Opens a file on drive.
     */
    public PendingResult<DriveApi.ContentsResult> getDriveFileContents(DriveId fileId) {
        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, fileId);
        return file.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null);
    }

    /**
     * Gets the drive id for a folder by id string.
     */
    public PendingResult<DriveApi.DriveIdResult> getDriveId(String id) {
        return Drive.DriveApi.fetchDriveId(mGoogleApiClient, id);
    }
}
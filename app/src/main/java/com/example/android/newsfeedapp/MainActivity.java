package com.example.android.newsfeedapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsList>>, SwipeRefreshLayout.OnRefreshListener {
    /**
     * URL for news list data from The Guardian OpenPlatform API using specific queries
     */
    private static final String REQUEST_URL = "https://content.guardianapis.com/search?page-size=20&show-tags=contributor&show-fields=all&api-key=test";
    private LoaderManager loaderManager;
    private ListView listView;
    private NewsListAdapter adapter;
    private TextView emptyStateTextView;
    private ProgressBar mLoader;
    private Button searchButton = null;
    private EditText searchField = null;
    private String query = null;
    private SwipeRefreshLayout listRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // This add a logo in ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //this allows to refresh the list of news by swiping
        listRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        listRefresh.setOnRefreshListener(this);
        //Set the Awesome Font
        Typeface fontAW = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        searchButton = (Button) findViewById(R.id.search);
        searchButton.setTypeface(fontAW);
        //this finds the id of search field
        searchField = (EditText) findViewById(R.id.et_search);
        //Find the id for the following views
        listView = (ListView) findViewById(R.id.list);
        emptyStateTextView = (TextView) findViewById(R.id.empty_view);
        //this loads a circle progress bar as loading bar
        mLoader = (ProgressBar) findViewById(R.id.loading_indicator);
        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();
        if (isConnected()) { //if there is internet connection
            // Initialize the loader. Pass in the int an ID constant and pass in null for the bundle. Pass in this activity for the LoaderCallbacks parameter.
            loaderManager.initLoader(1, null, this);
        } else {
            //hide the loading bar
            mLoader.setVisibility(View.GONE);
            //display that there is no internet connection
            String message = getString(R.string.no_internet);
            new AlertDialog.Builder(this).setMessage(message).show();
        }
        // set the view to show when the list is empty
        listView.setEmptyView(emptyStateTextView);
        //Shows the items list using an ListView with a custom adapter
        adapter = new NewsListAdapter(this, new ArrayList<NewsList>());
        listView.setAdapter(adapter);
        // Set a click listener to open an activity for the respective position
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //load the buy link in the Browser
                NewsList itemsList = (NewsList) adapterView.getAdapter().getItem(position);
                Uri webpage;
                String title = itemsList.getWebTitle();
                if (itemsList.getWebUrl() != null) {
                    webpage = Uri.parse(itemsList.getWebUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else { //if the items have no buy link, a toast message is displayed
                    Toast.makeText(getApplicationContext(), "\"" + title + "\" " + getString(R.string.not_available), Toast.LENGTH_LONG).show();
                }
            }
        });
        // The code in this method will be executed when the button search on keyboard is clicked on.
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });
        // The code in this method will be executed when the button search is clicked on.
        searchButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    private void search() {
        //determine if connection is active after the search button is clicked
        if (isConnected()) {
            query = searchField.getText().toString();
            mLoader.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("");
            //restart the loader with the new data
            loaderManager.restartLoader(1, null, this);
        } else {
            String message = getString(R.string.no_internet);
            new AlertDialog.Builder(this).setMessage(message).show();
        }
    }

    @Override
    public Loader<List<NewsList>> onCreateLoader(int i, Bundle bundle) {
        //this call the connection on server in base of preference
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //here get the preferences
        String maxResults = sharedPrefs.getString(
                getString(R.string.settings_max_results_key),
                getString(R.string.settings_max_results_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );
        //get the URL of the Guardian server
        Uri baseUri = Uri.parse(REQUEST_URL);
        //this is used to build the URL with the query parameters
        Uri.Builder uriBuilder = baseUri.buildUpon();
        if (query != null && !query.isEmpty()) {
            uriBuilder.appendQueryParameter("q", query); //this adds the query in the search box
        }
        uriBuilder.appendQueryParameter("page-size", maxResults); //this adds the max news listed
        uriBuilder.appendQueryParameter("order-by", orderBy.toLowerCase()); //here defines the order of the list
        if (!section.equals("all")) {
            uriBuilder.appendQueryParameter("section", section.toLowerCase()); //this filter the results for section
        }
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override //when the loading is finished
    public void onLoadFinished(Loader<List<NewsList>> loader, List<NewsList> data) {
        if (isConnected()) {
            // Clear the adapter of previous data
            adapter.clear();
            //hide the loading bar
            mLoader.setVisibility(View.GONE);
            // Set empty state text to display "No books found."
            String message = getString(R.string.no_found, query);
            emptyStateTextView.setText(message);
            // If there is a valid list of news, it add them to the adapter's data set.
            if (data != null && !data.isEmpty()) {
                adapter.addAll(data);
            }
        } else {
            mLoader.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsList>> loader) {
        // Loader reset, clears out the existing data.
        adapter.clear();
    }

    //this allows to open menu of search preference
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        if (isConnected()) {
            loaderManager.restartLoader(1, null, this);
        } else {
            String message = getString(R.string.no_internet);
            new AlertDialog.Builder(this).setMessage(message).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listRefresh.setRefreshing(false);
            }
        }, 3000);
    }

    //determine if connection is active
    private Boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}

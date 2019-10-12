package com.example.movies.popularmovies.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.popularmovies.Adapters.MovieAdapter;

import com.example.movies.popularmovies.Database.MovieViewModel;
import com.example.movies.popularmovies.Database.ViewModelFactory;
import com.example.movies.popularmovies.Model.Movie;
import com.example.movies.popularmovies.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity{
    private List<Movie> movies;
    private MovieAdapter adapter;
    public static final String TAG = MainActivity.class.getName();
    public static final String LIFE_CYCLE = "LIFE CYCLE";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN = 1;
    public static final String ANONYMOUS = "anonymous";
    public ActionBar actionBar;
    private String mUserID;
    RecyclerView recyclerView;
    private Parcelable mMoviesRecyclerViewState;
    public static final String STATE_KEY="positionKey";




    //widget
    SharedPreferences shared;
    List<String> titlesList;
    public static String SHARED_PREFS = "widgetMovieTitles";
    public static final String WIDGET_TITLE = "widgetTitle";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.getDisplayOptions();
            actionBar.setTitle(getSortValue());
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Log.d(LIFE_CYCLE,"onCreate");

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: " +user.getDisplayName());
                    mUserID = user.getUid();
                    //if user is signed in generate Movies and pass in the username
                    generateMovies(mUserID);
                }else  {
                    onSignedOutCleanUp();
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                          new AuthUI.IdpConfig.GoogleBuilder().build(),
                                          new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(), RC_SIGN_IN);
                }
            }
        };
        //when the device is rotated the adapter will be null so we have to init in onCreate
        adapter = new MovieAdapter(getApplicationContext(),movies);
        if (savedInstanceState != null){
            mMoviesRecyclerViewState = savedInstanceState.getParcelable(STATE_KEY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
             if (resultCode == RESULT_OK){
                 Toast.makeText(MainActivity.this,R.string.sign_in,Toast.LENGTH_SHORT).show();
             } else if (resultCode == RESULT_CANCELED){
                 Toast.makeText(MainActivity.this,R.string.sign_in_can,Toast.LENGTH_SHORT).show();
                 finish(); }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_sorting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;

            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
        }
        return super.onOptionsItemSelected(item);

    }

    private String getSortValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("MainActivity","the preference is "+sharedPreferences.getString(getString(R.string.sort_by_key), getString(R.string.sort_popularity)));
        return sharedPreferences.getString(getString(R.string.sort_by_key), getString(R.string.sort_popularity));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LIFE_CYCLE, "onRestart ");
        if (movies != null){
            movies.clear();
        }

        getSortValue();
        mUserID = FirebaseAuth.getInstance().getUid();

        generateMovies(mUserID);
        actionBar.setTitle(getSortValue());

    }


    private void onSignedOutCleanUp(){
        mUserID = ANONYMOUS;
        if (movies!= null){
            movies.clear();
        }



    }


    private void generateMovies(String mUserID){
        Log.d(LIFE_CYCLE,"GENERATE ");
        movies = new ArrayList<>();
        final ViewModelFactory modelFactory = new ViewModelFactory(mUserID);
        MovieViewModel viewModel = ViewModelProviders.of(this,modelFactory).get(MovieViewModel.class);
        if (getSortValue().equals(getString(R.string.sort_by_favorites))) {
            if (!viewModel.getLiveData().hasActiveObservers()) viewModel.getLiveData().observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(DataSnapshot dataSnapshot) {
                    Log.d("DATASNAPPP",dataSnapshot.toString());
                    movies.clear();
                    for (DataSnapshot itemSnapShot : dataSnapshot.getChildren()) {
                        //Movie movie = itemSnapShot.getValue(Movie.class);
                        Log.d("DATASNAPPP", "itemSnapShot = " + itemSnapShot.getValue());
                        for (DataSnapshot data :itemSnapShot.getChildren()) {
                            Movie movie = data.getValue(Movie.class);
                            Log.d("DATASNAPPP", movie.title+"data  = " + data.child("title").getValue());
                            movies.add(movie);
                        }
                        adapter.notifyDataSetChanged();

                    }
                    Log.d(TAG, "movies list: "+movies.size());
                }
            });

        } else if (getSortValue().equals(getString(R.string.sort_popularity))){
            viewModel.getMovies( getString(R.string.sort_popularity) ).observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(@Nullable List<Movie> moviesModels) {
                    movies.clear();
                    if (moviesModels != null) {
                        movies.addAll(moviesModels);
                        adapter.notifyDataSetChanged();
                    }

                }
            });

             }else if (getSortValue().equals(getString(R.string.sort_top_rated))){
            viewModel.getMovies(getString(R.string.sort_top_rated)).observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(@Nullable List<Movie> moviesModels) {
                    movies.clear();
                    if (moviesModels != null) {
                        movies.addAll(moviesModels);
                        Log.d(TAG,"Top rated movies inserted");
                        adapter.notifyDataSetChanged();

                    }
                }
            });
        }

        Toast.makeText(this, getString(R.string.sort_by_pref_title) +" "+ getSortValue(),Toast.LENGTH_LONG).show();

         recyclerView = findViewById(R.id.recyclerView);
        // Calling the Adapter object and setting it to the recycler view.
        adapter = new MovieAdapter(this, movies);

        recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
         restoreRecyclerViewState();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(recyclerView != null){
            mMoviesRecyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        }
        outState.putParcelable(STATE_KEY,mMoviesRecyclerViewState);
    }


    private void restoreRecyclerViewState(){
        if (mMoviesRecyclerViewState != null){
            recyclerView.getLayoutManager().onRestoreInstanceState(mMoviesRecyclerViewState);
        }
    }

 /* private void packagesSharedPref(String title){
        shared = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        titlesList = new ArrayList<>();
        SharedPreferences.Editor editor = shared.edit();
        Set<String> set = new HashSet<String>();

      set.add(title);
      titlesList.addAll(set);
      editor.putStringSet(WIDGET_TITLE,);
      editor.apply();
      Log.d(TAG, "packagesSharedPref: "+set);
  }*/
}


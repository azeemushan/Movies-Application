
package com.example.movies.popularmovies.Database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.movies.popularmovies.API.Client;
import com.example.movies.popularmovies.API.Services;
import com.example.movies.popularmovies.BuildConfig;
import com.example.movies.popularmovies.Model.Movie;
import com.example.movies.popularmovies.Model.MoviesReply;
import com.example.movies.popularmovies.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieViewModel extends ViewModel {
    MutableLiveData<List <Movie>> movieLiveData = new MutableLiveData<>();
    public static final String TAG = MovieViewModel.class.getName();

    Context mContext;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference movieRef;
    private  FirebaseQueryLiveData liveData;

    public MovieViewModel( Context context,String userID) {
       this.movieRef = database.getReference().child(userID);
       this.liveData = new FirebaseQueryLiveData(movieRef);
       this.mContext = context;
    }

    public LiveData<List<Movie>> getMovies(String sort) {
            Client client = new Client();
            Services services = client.getClient().create(Services.class);
            //assign a call instence so we can use it to get popular movie or top rated

            Call<MoviesReply> moviesCall;
            if (sort.equals(mContext.getString(R.string.sort_popularity))) {
                Log.d("Calling", "Most Popular method is getting called");
                moviesCall = services.getPopularMovies(BuildConfig.API_KEY);

            } else if (sort.equals(mContext.getString(R.string.sort_top_rated))) {
                moviesCall = services.getTopRatedMovies(BuildConfig.API_KEY);
            } else moviesCall = null;

            moviesCall.enqueue(new Callback<MoviesReply>() {
                @Override
                public void onResponse(Call<MoviesReply> moviesCall, Response<MoviesReply> response) {
                    MoviesReply moviesReply = response.body();
                    movieLiveData.postValue(moviesReply.getMovies());
                    Log.d("onRespone", "getting the response "+response.body().getMovies().get(0).getId());
                }

                @Override
                public void onFailure(Call<MoviesReply> moviesCall, Throwable t) {
                    Log.d("error", "Couldnt get the movies");

                }
            });


            return movieLiveData;


        }

     @NonNull
    public LiveData<DataSnapshot> getLiveData() {
        return liveData;

     }
}




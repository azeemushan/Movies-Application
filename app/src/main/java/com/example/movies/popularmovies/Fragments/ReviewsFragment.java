package com.example.movies.popularmovies.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.movies.popularmovies.API.Client;
import com.example.movies.popularmovies.API.Services;
import com.example.movies.popularmovies.Adapters.ReviewAdapter;
import com.example.movies.popularmovies.BuildConfig;
import com.example.movies.popularmovies.Model.Review;
import com.example.movies.popularmovies.Model.ReviewReply;
import com.example.movies.popularmovies.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsFragment extends Fragment {

    public int movieID;
    private RecyclerView mRecyclerView;
    List<Review> reviews;
    ReviewAdapter mAdapter;
    public static final String ARG_MOVIEID = "movieID";
    private LinearLayoutManager manager;

    private Parcelable mReviewRecyclerViewState;
    public static final String STATE_KEY="positionKey";
    public ReviewsFragment() {
        // Required empty public constructor
    }

    public static ReviewsFragment newInstance(int movieID) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIEID,movieID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieID = getArguments().getInt(ARG_MOVIEID);
        }
        populateReview(movieID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie__reviews, container, false);
        mRecyclerView = view.findViewById(R.id.reviewRecyclerView);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            mReviewRecyclerViewState = savedInstanceState.getParcelable(STATE_KEY);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    //getting The reviews
    private void populateReview(int movie_id){
        Client client = new Client();
        Services services = client.getClient().create(Services.class);
        reviews = new ArrayList<>();
        Call<ReviewReply> call;
        call = services.getReviwes(movie_id, BuildConfig.API_KEY);
        call.enqueue(new Callback<ReviewReply>() {
                         @Override
                         public void onResponse(Call<ReviewReply> call, Response<ReviewReply> response) {

                             List<Review> reviews = response.body().getReviews();

                             mAdapter = new ReviewAdapter(reviews);

                            // GridLayoutManager gridLayoutManager2 = new GridLayoutManager(getActivity().getApplicationContext(),1);

                             if (getActivity().getApplicationContext() != null){
                                 manager = new LinearLayoutManager(getActivity().getApplicationContext());

                             }


                             mRecyclerView.setLayoutManager(manager);

                             mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(),LinearLayoutManager.VERTICAL));

                             mRecyclerView.setItemAnimator(new DefaultItemAnimator());

                             mRecyclerView.setAdapter(mAdapter);

                             mAdapter.notifyDataSetChanged();

                             restoreRecyclerViewState();

                         }

                         @Override
                         public void onFailure(Call<ReviewReply> call, Throwable t) {
                             Log.d("Error", "Failed to get a response"); }
                     });


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRecyclerView != null){
            mReviewRecyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(STATE_KEY,mReviewRecyclerViewState);
        }


    }


    private void restoreRecyclerViewState(){
      if (mReviewRecyclerViewState != null){
          mRecyclerView.getLayoutManager().onRestoreInstanceState(mReviewRecyclerViewState);
      }
    }



}

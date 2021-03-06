package com.example.movies.popularmovies.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movies.popularmovies.Model.Movie;
import com.example.movies.popularmovies.R;
import com.example.movies.popularmovies.UI.MovieDetails;


import java.util.List;

  public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
      Context context;
      List<Movie> moviesArray;
      public static final String MOVIE_KEY = "movie";
      public static final String POSTER_PATH = "https://image.tmdb.org/t/p/w500";



      public MovieAdapter( Context context,List<Movie> moviesArray) {
          this.context = context;
          this.moviesArray = moviesArray;


      }

      @NonNull
      @Override
      public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
          //infalte the grid_item layout to View object
          View view = LayoutInflater.from(context).inflate(R.layout.grid_item, viewGroup, false);
          //Create viewHolder and place the view inside it
          MovieViewHolder movieViewHolder = new MovieViewHolder(view);
          return movieViewHolder;
      }



      @Override
      public void onBindViewHolder(@NonNull MovieViewHolder holder, int i) {
          final  Movie movie = moviesArray.get(i);

          Glide.with(context).load(POSTER_PATH+movie.getPosterPath()).into(holder.itemImage);
      }


      @Override
      public int getItemCount() {
          return moviesArray.size();
      }



      public class MovieViewHolder extends RecyclerView.ViewHolder{
          ImageView itemImage;
          public MovieViewHolder(@NonNull View itemView) {
              super(itemView);
              itemImage = itemView.findViewById(R.id.image);
              itemView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View itemView) {
                      Intent intent = new Intent(context, MovieDetails.class);
                      Movie clickedMovie = moviesArray.get(getAdapterPosition());
                      intent.putExtra(MOVIE_KEY,  clickedMovie);
                      context.startActivity(intent);
                  }
              });
          }



      }

  }
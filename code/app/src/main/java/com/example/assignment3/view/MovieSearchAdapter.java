package com.example.assignment3.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
import com.example.assignment3.model.FirebaseMovie;

import java.util.ArrayList;
import java.util.List;

public class MovieSearchAdapter extends RecyclerView.Adapter<MovieSearchAdapter.MovieViewHolder> {

    private List<FirebaseMovie> movies;
    private OnMovieSelectListener onMovieSelectListener;

    public interface OnMovieSelectListener {
        void onMovieSelected(FirebaseMovie movie);
    }

    public MovieSearchAdapter() {
        this.movies = new ArrayList<>();
    }

    public void setOnMovieSelectListener(OnMovieSelectListener listener) {
        this.onMovieSelectListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_search, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        FirebaseMovie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvYear.setText(movie.getYear());
        holder.tvRating.setText(String.format("%.1f", movie.getCriticsRating()));

        // Load movie poster with Glide
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty() && !movie.getPosterUrl().equals("N/A")) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.getPosterUrl())
                    .placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.error_poster)
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.placeholder_poster);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onMovieSelectListener != null) {
                onMovieSelectListener.onMovieSelected(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateMovies(List<FirebaseMovie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvYear, tvRating;
        ImageView ivPoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvYear = itemView.findViewById(R.id.tvMovieYear);
            tvRating = itemView.findViewById(R.id.tvMovieRating);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
        }
    }
}
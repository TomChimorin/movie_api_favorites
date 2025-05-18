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

import java.util.List;

public class MovieFavoriteAdapter extends RecyclerView.Adapter<MovieFavoriteAdapter.MovieViewHolder> {

    private List<FirebaseMovie> movies;
    private OnMovieListener onMovieListener;

    public interface OnMovieListener {
        void onMovieClick(int position);
        void onDeleteClick(int position);

    }

    public MovieFavoriteAdapter(List<FirebaseMovie> movies, OnMovieListener onMovieListener) {
        this.movies = movies;
        this.onMovieListener = onMovieListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_favorite, parent, false);
        return new MovieViewHolder(view, onMovieListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        FirebaseMovie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvStudio.setText(movie.getStudio());
        holder.tvRating.setText(String.valueOf(movie.getCriticsRating()));

        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.getPosterUrl())
                    .placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.error_poster)
                    .into(holder.ivPoster);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void setMovies(List<FirebaseMovie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public FirebaseMovie getMovieAt(int position) {
        return movies.get(position);
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle, tvStudio, tvRating;
        ImageView ivPoster, ivDelete;
        OnMovieListener onMovieListener;

        public MovieViewHolder(@NonNull View itemView, OnMovieListener onMovieListener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStudio = itemView.findViewById(R.id.tvStudio);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            this.onMovieListener = onMovieListener;

            itemView.setOnClickListener(this);
            ivDelete.setOnClickListener(v -> {
                if (onMovieListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onMovieListener.onDeleteClick(position);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (onMovieListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onMovieListener.onMovieClick(position);
                }
            }
        }
    }
}
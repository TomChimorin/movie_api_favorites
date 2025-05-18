package com.example.assignment3.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
import com.example.assignment3.model.FirebaseMovie;
import com.example.assignment3.viewmodel.MovieViewModel;
import com.google.firebase.auth.FirebaseAuth;


public class MovieDetailsPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private MovieViewModel movieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);

        mAuth = FirebaseAuth.getInstance();
        movieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);
        FirebaseMovie movie = (FirebaseMovie) getIntent().getSerializableExtra("MOVIE_DATA");

        if (movie != null) {
            ImageView posterImageView = findViewById(R.id.detailsPosterImageView);
            TextView titleTextView = findViewById(R.id.detailsTitleTextView);
            TextView yearTextView = findViewById(R.id.detailsYearTextView);
            TextView ratingTextView = findViewById(R.id.detailsRatingTextView);
            TextView plotTextView = findViewById(R.id.detailsPlotTextView);

            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                Glide.with(this)
                        .load(movie.getPosterUrl())
                        .placeholder(R.drawable.placeholder_poster)
                        .error(R.drawable.error_poster)
                        .into(posterImageView);
            } else {
                posterImageView.setImageResource(R.drawable.placeholder_poster);
            }

            titleTextView.setText(movie.getTitle());
            yearTextView.setText(movie.getYear());
            ratingTextView.setText(String.format("Rating: %s", movie.getCriticsRating()));
            plotTextView.setText(movie.getPlot());
        }

        Button btnSearchMovie = findViewById(R.id.btnSearchMovie);
        btnSearchMovie.setOnClickListener(v -> finish());

        Button btnAddFavorite = findViewById(R.id.btnFavorite);
        btnAddFavorite.setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser().getUid();
            movieViewModel.addFavorite(userId, movie);
            startActivity(new Intent(this, MovieFavoriteActivity.class));
            finish();
        });
    }
}
package com.example.assignment3.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;
import com.example.assignment3.model.FirebaseMovie;
import com.example.assignment3.viewmodel.MovieViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MovieFavoriteActivity extends AppCompatActivity implements MovieFavoriteAdapter.OnMovieListener {

    private RecyclerView recyclerView;
    private MovieFavoriteAdapter movieAdapter;
    private MovieViewModel movieViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        Button btnAddMovie = findViewById(R.id.btnAddMovie);
        Button btnLogout = findViewById(R.id.btnLogOut);

        // Setup RecyclerView
        movieAdapter = new MovieFavoriteAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);

        // Initialize ViewModel
        movieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        // Observe favorites data
        movieViewModel.getFavorites().observe(this, movies -> {
            if (movies != null) {
                movieAdapter.setMovies(movies);
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Add movie button click
        btnAddMovie.setOnClickListener(v -> {
            startActivity(new Intent(this, MovieSearchActivity.class));
        });

        // Load favorites
        loadFavorites();
    }

    private void loadFavorites() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            movieViewModel.loadFavorites(userId);
        }
    }

    @Override
    public void onMovieClick(int position) {
        // Handle movie click - navigate to edit screen
        FirebaseMovie selectedMovie = movieAdapter.getMovieAt(position);
//        Intent intent = new Intent(this, ActivityAddEditMovie.class);
//        intent.putExtra("selected_movie", selectedMovie);
//        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        // Handle delete
        FirebaseMovie movieToDelete = movieAdapter.getMovieAt(position);
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            movieViewModel.deleteFavorite(userId, movieToDelete.getId());
        }
    }


}
package com.example.assignment3.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;
import com.example.assignment3.model.FirebaseMovie;
import com.example.assignment3.viewmodel.MovieViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MovieSearchActivity extends AppCompatActivity implements MovieSearchAdapter.OnMovieSelectListener {

    private EditText etSearchMovie;
    private RecyclerView recyclerViewMovies;
    private MovieSearchAdapter movieAddEditAdapter;
    private MovieViewModel movieViewModel;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupFirebase();
        setupViewModel();
        setupRecyclerView();
        setupSearchButton();
    }

    private void initializeViews() {
        etSearchMovie = findViewById(R.id.etSearchMovie);
        recyclerViewMovies = findViewById(R.id.searchRecyclerView);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupViewModel() {
        movieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);
        movieViewModel.getMovieData().observe(this, this::handleSearchResults);
        movieViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        movieAddEditAdapter = new MovieSearchAdapter();
        movieAddEditAdapter.setOnMovieSelectListener(this);

        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMovies.setAdapter(movieAddEditAdapter);
    }

    private void setupSearchButton() {
        findViewById(R.id.btnSearchMovie).setOnClickListener(v -> {
            String query = etSearchMovie.getText().toString().trim();
            if (!query.isEmpty()) {
                movieViewModel.searchMovie(query, 1);
            } else {
                Toast.makeText(this, "Please enter a movie title", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSearchResults(List<FirebaseMovie> movies) {
        if (movies == null || movies.isEmpty()) {
            Toast.makeText(this, "No movies found", Toast.LENGTH_SHORT).show();
            movieAddEditAdapter.updateMovies(new ArrayList<>());
        } else {
            movieAddEditAdapter.updateMovies(movies);
        }
    }

    @Override
    public void onMovieSelected(FirebaseMovie movie) {
        Intent intent = new Intent(this, MovieDetailsPage.class);
        intent.putExtra("MOVIE_DATA", movie);
        startActivity(intent);
    }
}
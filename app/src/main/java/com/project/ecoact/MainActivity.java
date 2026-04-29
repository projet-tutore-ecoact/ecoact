package com.project.ecoact;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.project.ecoact.util.SessionManager;
import com.project.ecoact.util.TestDataHelper;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Insérer un utilisateur de test au premier démarrage (à retirer en production!)
        TestDataHelper.insertTestDataIfNeeded(this);

        // Initialiser SessionManager
        sessionManager = new SessionManager(this);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);

            // ✅ NE PAS appeler navigate() ici! Le FragmentManager n'est pas prêt.
            // À la place, on laisse le startDestination du navGraph afficher LoginFragment.
            // LoginFragment va vérifier la session et naviguer si nécessaire.

            // Cacher la bottom nav sur les écrans d'authentification
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
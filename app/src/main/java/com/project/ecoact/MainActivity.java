/* classe principale de l'application, gère la navigation et l'affichage de la barre de navigation inférieur. 
* Elle vérifie également la session utilisateur au démarrage et redirige vers l'écran de connexion si nécessaire.
*/
package com.project.ecoact;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.project.ecoact.util.SessionManager;
import com.project.ecoact.util.TestDataHelper;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private NavController navController;
    private SessionManager sessionManager;

    // Vérifier la session utilisateur au démarrage
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
            applySystemBarInsets();

            //  NE PAS appeler navigate() ici! Le FragmentManager n'est pas prêt.
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

    private void applySystemBarInsets() {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }

        View root = findViewById(R.id.main_root);
        int rootPaddingLeft = root.getPaddingLeft();
        int rootPaddingTop = root.getPaddingTop();
        int rootPaddingRight = root.getPaddingRight();
        int rootPaddingBottom = root.getPaddingBottom();

        int bottomNavPaddingLeft = bottomNav.getPaddingLeft();
        int bottomNavPaddingTop = bottomNav.getPaddingTop();
        int bottomNavPaddingRight = bottomNav.getPaddingRight();
        int bottomNavPaddingBottom = bottomNav.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    rootPaddingLeft,
                    rootPaddingTop + systemBars.top,
                    rootPaddingRight,
                    rootPaddingBottom
            );

            bottomNav.setPadding(
                    bottomNavPaddingLeft,
                    bottomNavPaddingTop,
                    bottomNavPaddingRight,
                    bottomNavPaddingBottom + systemBars.bottom
            );

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);
    }
}

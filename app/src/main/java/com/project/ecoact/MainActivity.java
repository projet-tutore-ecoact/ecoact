/*
 * MainActivity est l'activité principale de l'application EcoAct.
 *
 * Son rôle est de :
 * - charger le layout principal activity_main.xml ;
 * - initialiser la navigation entre les fragments ;
 * - connecter la BottomNavigationView avec le NavController ;
 * - cacher la barre de navigation sur les écrans de connexion et d'inscription ;
 * - adapter l'affichage aux barres système Android sur les versions récentes.
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

    /*
     * BottomNavigationView correspond à la barre de navigation affichée en bas.
     * Elle permet à l'utilisateur de changer d'écran : Accueil, Appareils,
     * Habitudes, Profil, Conseils, etc.
     */
    private BottomNavigationView bottomNav;

    /*
     * NavController permet de gérer la navigation entre les fragments.
     * C'est lui qui sait quel écran est affiché et qui permet de passer
     * d'un fragment à un autre.
     */
    private NavController navController;

    /*
     * SessionManager sert à gérer la session utilisateur.
     * Il permet de savoir si un utilisateur est connecté ou non.
     */
    private SessionManager sessionManager;

    /*
     * onCreate est la première méthode appelée quand MainActivity démarre.
     * C'est ici qu'on initialise l'interface, la navigation et les éléments principaux.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * On indique à Android quel fichier XML utiliser pour afficher l'interface
         * principale de l'application.
         */
        setContentView(R.layout.activity_main);

        /*
         * Cette ligne ajoute des données de test si nécessaire.
         * C'est utile pendant le développement pour tester l'application
         * sans devoir tout saisir manuellement.
         */
        TestDataHelper.insertTestDataIfNeeded(this);

        /*
         * On crée le gestionnaire de session.
         * Il pourra être utilisé pour vérifier l'état de connexion de l'utilisateur.
         */
        sessionManager = new SessionManager(this);

        /*
         * On récupère le NavHostFragment depuis le layout activity_main.xml.
         * Ce fragment sert de conteneur principal pour afficher les autres fragments.
         */
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        /*
         * On vérifie que le NavHostFragment existe bien avant de configurer la navigation.
         * Cela évite une erreur si le fragment n'est pas trouvé.
         */
        if (navHostFragment != null) {

            /*
             * On récupère le NavController à partir du NavHostFragment.
             * Le NavController va gérer les déplacements entre les écrans.
             */
            navController = navHostFragment.getNavController();

            /*
             * On récupère la barre de navigation du bas depuis le layout.
             */
            bottomNav = findViewById(R.id.bottom_nav);

            /*
             * Cette ligne relie la BottomNavigationView au NavController.
             * Grâce à ça, quand l'utilisateur clique sur un onglet du bas,
             * l'application affiche automatiquement le fragment correspondant.
             */
            NavigationUI.setupWithNavController(bottomNav, navController);

            /*
             * On appelle cette méthode pour gérer les marges liées aux barres système
             * comme la barre de statut en haut ou la barre de navigation Android en bas.
             */
            applySystemBarInsets();

            /*
             * On n'appelle pas navigate() directement ici.
             * Le navGraph démarre sur LoginFragment, puis c'est LoginFragment
             * qui vérifie si l'utilisateur est déjà connecté ou non.
             *
             * Cela évite de faire une navigation trop tôt, avant que le système
             * de fragments soit complètement prêt.
             */

            /*
             * Ce listener est appelé à chaque changement d'écran.
             * Il permet de décider si la barre de navigation du bas doit être visible ou cachée.
             */
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

                /*
                 * Si l'utilisateur est sur l'écran de connexion ou d'inscription,
                 * on cache la barre de navigation du bas.
                 * C'est logique, car l'utilisateur n'est pas encore dans l'application.
                 */
                if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment) {
                    bottomNav.setVisibility(View.GONE);

                    /*
                     * Sur les autres écrans, on affiche la barre de navigation.
                     */
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /*
     * Cette méthode gère l'affichage avec les barres système Android.
     *
     * Sur les versions récentes d'Android, le contenu peut parfois passer sous
     * la barre de statut ou sous la barre de navigation du téléphone.
     * On ajoute donc des paddings pour éviter que l'interface soit cachée.
     */
    private void applySystemBarInsets() {

        /*
         * Cette gestion est seulement appliquée à partir d'Android 15.
         * Pour les versions plus anciennes, on ne change rien.
         */
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }

        /*
         * On récupère la vue principale du layout.
         * C'est le LinearLayout qui contient le NavHostFragment et la bottom navigation.
         */
        View root = findViewById(R.id.main_root);

        /*
         * On sauvegarde les paddings de départ du layout principal.
         * Comme ça, on peut ajouter les marges système sans perdre les valeurs déjà définies.
         */
        int rootPaddingLeft = root.getPaddingLeft();
        int rootPaddingTop = root.getPaddingTop();
        int rootPaddingRight = root.getPaddingRight();
        int rootPaddingBottom = root.getPaddingBottom();

        /*
         * On sauvegarde aussi les paddings de départ de la barre de navigation du bas.
         */
        int bottomNavPaddingLeft = bottomNav.getPaddingLeft();
        int bottomNavPaddingTop = bottomNav.getPaddingTop();
        int bottomNavPaddingRight = bottomNav.getPaddingRight();
        int bottomNavPaddingBottom = bottomNav.getPaddingBottom();

        /*
         * Ce listener récupère les dimensions des barres système.
         * Il est appelé quand Android applique les insets à la vue.
         */
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {

            /*
             * On récupère les marges occupées par les barres système :
             * - barre de statut en haut ;
             * - barre de navigation Android en bas.
             */
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            /*
             * On ajoute la hauteur de la barre système du haut au padding top.
             * Cela évite que le contenu soit caché sous la barre de statut.
             */
            view.setPadding(
                    rootPaddingLeft,
                    rootPaddingTop + systemBars.top,
                    rootPaddingRight,
                    rootPaddingBottom
            );

            /*
             * On ajoute la hauteur de la barre système du bas au padding bottom
             * de la BottomNavigationView.
             * Cela évite que la barre de navigation de l'application soit collée
             * ou cachée par la barre de navigation Android.
             */
            bottomNav.setPadding(
                    bottomNavPaddingLeft,
                    bottomNavPaddingTop,
                    bottomNavPaddingRight,
                    bottomNavPaddingBottom + systemBars.bottom
            );

            /*
             * On retourne les windowInsets pour laisser Android continuer son traitement normal.
             */
            return windowInsets;
        });

        /*
         * Cette ligne demande à Android d'appliquer les insets sur la vue principale.
         */
        ViewCompat.requestApplyInsets(root);
    }
}
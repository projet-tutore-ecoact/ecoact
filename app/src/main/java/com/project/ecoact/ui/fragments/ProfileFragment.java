package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.User;
import com.project.ecoact.data.repository.UserRepository;
import com.project.ecoact.util.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private TextView tvUserEmail, tvUserName;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les views
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserName = view.findViewById(R.id.tv_user_name);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Initialiser SessionManager et UserRepository
        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());

        // Afficher un état de chargement
        tvUserName.setText("Chargement...");
        tvUserEmail.setText("");

        // Charger les infos de l'utilisateur connecté sur un thread background
        loadUserInfoAsync();

        // Bouton Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Charger les infos utilisateur de manière asynchrone
     * IMPORTANT: Utilisé getUserByIdSync sur un thread background!
     */
    private void loadUserInfoAsync() {
        new Thread(() -> {
            try {
                // Récupérer l'ID de l'utilisateur connecté
                Long userId = sessionManager.getCurrentUserId();

                User user;
                if (userId != null) {
                    // ⚠️ IMPORTANT: getUserByIdSync DOIT être appelé sur un thread background
                    // Pas sur le main thread!
                    user = userRepository.getUserByIdSync(userId);
                } else {
                    user = null;
                }

                // Revenir au main thread pour mettre à jour l'UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (user != null) {
                            tvUserName.setText("Utilisateur: " + user.getFirstName() + " " + user.getLastName());
                            tvUserEmail.setText("Email: " + user.getEmail());
                        } else {
                            tvUserName.setText("Utilisateur: Non trouvé");
                            tvUserEmail.setText("Email: N/A");
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvUserName.setText("Erreur de chargement");
                        tvUserEmail.setText("");
                    });
                }
                e.printStackTrace();
            }
        }).start();
    }

    private void logout() {
        // Effacer la session
        sessionManager.logout();

        // Afficher un message
        Toast.makeText(getContext(), "Déconnecté", Toast.LENGTH_SHORT).show();

        // Rediriger vers Login
        Navigation.findNavController(getView())
                .navigate(R.id.action_profileFragment_to_loginFragment);
    }
}
package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.User;
import com.project.ecoact.data.repository.UserRepository;
import com.project.ecoact.util.PasswordUtils;
import com.project.ecoact.util.SessionManager;

import java.util.Objects;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink, tvErrorMessage;
    private ProgressBar pbLoading;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les views
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvRegisterLink = view.findViewById(R.id.tv_register_link);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);
        pbLoading = view.findViewById(R.id.pb_loading);

        // Initialiser le repository et le session manager
        userRepository = new UserRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        // ✅ Vérifier si l'utilisateur est déjà connecté sur un thread background
        new Thread(() -> {
            boolean isLoggedIn = sessionManager.isUserLoggedIn();
            
            if (isLoggedIn && getView() != null) {
                // Utilisateur déjà connecté → naviguer vers Home
                getActivity().runOnUiThread(() -> {
                    Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
                });
                return;  // Ne pas initialiser les listeners, on quitte le fragment
            }
        }).start();

        // Click listeners
        btnLogin.setOnClickListener(v -> login());
        tvRegisterLink.setOnClickListener(v -> navigateToRegister());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Tentative connexion avec: " + email);

        // Validation
        if (!validateInputs(email, password)) {
            return;
        }

        // Afficher le loading
        pbLoading.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        tvErrorMessage.setVisibility(View.GONE);

        // Effectuer la connexion sur un thread background
        new Thread(() -> {
            try {
                Log.d(TAG, "Recherche utilisateur...");
                User user = userRepository.getUserByEmail(email);
                
                if (user == null) {
                    Log.d(TAG, "❌ Utilisateur non trouvé: " + email);
                    showErrorUI("Email ou mot de passe incorrect");
                    return;
                }

                Log.d(TAG, "✅ Utilisateur trouvé: " + user.getEmail());
                Log.d(TAG, "Vérification du mot de passe...");
                
                boolean passwordValid = PasswordUtils.verifyPassword(password, user.getPassword());
                Log.d(TAG, "Password valid: " + passwordValid);

                if (passwordValid) {
                    Log.d(TAG, "✅ Connexion réussie!");
                    sessionManager.saveUserSession(user.getId());
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Connexion réussie", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
                        });
                    }
                } else {
                    Log.d(TAG, "❌ Mot de passe incorrect");
                    showErrorUI("Email ou mot de passe incorrect");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Exception lors de la connexion", e);
                e.printStackTrace();
                showErrorUI("Erreur: " + e.getMessage());
            }
        }).start();
    }

    private void showErrorUI(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                pbLoading.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                tvErrorMessage.setText(message);
                tvErrorMessage.setVisibility(View.VISIBLE);
                Log.d(TAG, "Erreur affichée: " + message);
            });
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return false;
        }

        if (!PasswordUtils.isValidEmail(email)) {
            showError("Email invalide");
            return false;
        }

        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void navigateToRegister() {
        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_registerFragment);
    }
}


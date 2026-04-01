package com.project.ecoact.ui.fragments;

import android.os.Bundle;
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

public class RegisterFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink, tvErrorMessage;
    private ProgressBar pbLoading;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les views
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnRegister = view.findViewById(R.id.btn_register);
        tvLoginLink = view.findViewById(R.id.tv_login_link);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);
        pbLoading = view.findViewById(R.id.pb_loading);

        // Initialiser le repository et le session manager
        userRepository = new UserRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        // Click listeners
        btnRegister.setOnClickListener(v -> register());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void register() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (!validateInputs(firstName, lastName, email, password, confirmPassword)) {
            return;
        }

        // Afficher le loading
        pbLoading.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        tvErrorMessage.setVisibility(View.GONE);

        // Effectuer l'enregistrement sur un thread background
        new Thread(() -> {
            try {
                // Vérifier si l'email existe déjà
                User existingUser = userRepository.getUserByEmail(email);
                if (existingUser != null) {
                    getActivity().runOnUiThread(() -> {
                        pbLoading.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        tvErrorMessage.setText("Cet email est déjà utilisé");
                        tvErrorMessage.setVisibility(View.VISIBLE);
                    });
                    return;
                }

                // Hasher le mot de passe
                String hashedPassword = PasswordUtils.encodePassword(password);

                // Créer l'utilisateur
                User newUser = new User(email, hashedPassword, firstName, lastName);

                // Insérer dans la base de données
                long userId = userRepository.insertUser(newUser);

                if (userId > 0) {
                    // Enregistrement réussi
                    // Sauvegarder SEULEMENT l'ID de l'utilisateur
                    sessionManager.saveUserSession(userId);
                    
                    getActivity().runOnUiThread(() -> {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_homeFragment);
                    });
                } else {
                    // Erreur lors de l'insertion
                    getActivity().runOnUiThread(() -> {
                        pbLoading.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        tvErrorMessage.setText("Erreur lors de la création du compte");
                        tvErrorMessage.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    tvErrorMessage.setText("Erreur lors de l'enregistrement");
                    tvErrorMessage.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private boolean validateInputs(String firstName, String lastName, String email, String password, String confirmPassword) {
        if (firstName.isEmpty()) {
            showError("Veuillez entrer votre prénom");
            return false;
        }

        if (lastName.isEmpty()) {
            showError("Veuillez entrer votre nom");
            return false;
        }

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return false;
        }

        if (!PasswordUtils.isValidEmail(email)) {
            showError("Email invalide");
            return false;
        }

        if (password.isEmpty()) {
            showError("Veuillez entrer un mot de passe");
            return false;
        }

        if (!PasswordUtils.isValidPassword(password)) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            showError("Veuillez confirmer votre mot de passe");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void navigateToLogin() {
        Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_loginFragment);
    }
}


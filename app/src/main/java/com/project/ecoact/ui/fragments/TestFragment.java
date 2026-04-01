package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.ecoact.R;
import com.project.ecoact.ui.adapter.UserAdapter;
import com.project.ecoact.data.entity.User;
import com.project.ecoact.viewModel.UserViewModel;

public class TestFragment extends Fragment {
//
//    private UserViewModel viewModel;
//    private UserAdapter adapter;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.brouillon, container, false);
//
//        EditText etNom = view.findViewById(R.id.etNom);
//        EditText etPrenom = view.findViewById(R.id.etPrenom);
//        Button btnAjouter = view.findViewById(R.id.btnAjouter);
//        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
//
//        adapter = new UserAdapter();
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(adapter);
//
//        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
//
//        // 🔥 Observe les données
//        viewModel.getUsers().observe(getViewLifecycleOwner(), personnes -> {
//            adapter.setData(personnes);
//        });
//
//        btnAjouter.setOnClickListener(v -> {
//            String nom = etNom.getText().toString().trim();
//            String prenom = etPrenom.getText().toString().trim();
//
//            if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prenom)) {
//                Toast.makeText(getContext(), "Champs requis", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            viewModel.insert(new User(nom, prenom));
//
//            etNom.setText("");
//            etPrenom.setText("");
//        });
//
//        return view;
//    }
}
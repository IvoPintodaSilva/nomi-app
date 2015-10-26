package com.example.ivosilva.nomi.profiles;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivosilva.nomi.R;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

/**
 * Created by silva on 19-10-2015.
 */
public class ProfileDetailsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Iconify.with(new FontAwesomeModule());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_details, container, false);


        Log.d("ProfileDetailsFragmentP", getArguments().getString("PROFILE", ""));
        Log.d("ProfileDetailsFragmentA", getArguments().getString("ATTRIBUTES", ""));

        return view;
    }
}

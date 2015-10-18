package com.example.ivosilva.nomi.contacts;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivosilva.nomi.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Created by ivosilva on 17/10/15.
 */
public class RVPContactsAdapter extends RecyclerView.Adapter<RVPContactsAdapter.ProfileViewHolder> {

    List<CollectedProfiles> user_profiles;

    RVPContactsAdapter(List<CollectedProfiles> user_profiles){
        this.user_profiles = user_profiles;
    }



    public static class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView cv;
        public TextView personName;
        public ProfileViewHolderClicks mListener;

        //ImageView personPhoto;

        public ProfileViewHolder(View itemView, ProfileViewHolderClicks listener) {
            super(itemView);
            mListener = listener;
            cv = (CardView)itemView.findViewById(R.id.profile_card);
            personName = (TextView)itemView.findViewById(R.id.person_name);
            itemView.setOnClickListener(this);
            //personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
        }

        @Override
        public void onClick(View v) {
            //Log.d("VIEWHOLDER", new Integer(this.getLayoutPosition()).toString());
            mListener.openDetails(v, this.getLayoutPosition());


        }


        public interface ProfileViewHolderClicks {
            void openDetails(View v, int position);
        }

    }


    public CollectedProfiles getItem(int position) {
        return user_profiles.get(position);
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);
        //ProfileViewHolder pvh = new ProfileViewHolder(v);

        ProfileViewHolder vh = new ProfileViewHolder(v, new ProfileViewHolder.ProfileViewHolderClicks() {
            public void openDetails(View v, int position) {

                /*  create new activity and display details  */
                /*  put selected object on intent extras  */


                Gson gson = new GsonBuilder().
                        registerTypeAdapter(CollectedProfiles.class, new CollectedProfilesSerializer())
                        .create();
                String profile_json = gson.toJson(user_profiles.get(position));

                Log.d("profile_json", profile_json);

                Intent contact_details = new Intent(v.getContext(), ContactDetailsActivity.class);
                contact_details.putExtra("PROFILE", profile_json);
                v.getContext().startActivity(contact_details);


            }
        });
        return vh;
    }



    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        //ProfileViewHolder.personName.setText(user_profiles.get(position).name);
        holder.personName.setText(user_profiles.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return user_profiles.size();
    }


}

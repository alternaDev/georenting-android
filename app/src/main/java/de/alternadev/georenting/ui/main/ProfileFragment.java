package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {
    public static ProfileFragment newInstance(Person googleUser) {
        return new ProfileFragment(googleUser);
    }

    private final Person mGoogleUser;


    @Inject
    Picasso picasso;


    public ProfileFragment() {
        // Required empty public constructor
        mGoogleUser = null;
    }

    public ProfileFragment(Person googleUser) {
        this.mGoogleUser = googleUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentProfileBinding b = FragmentProfileBinding.inflate(inflater, container, false);

        if(mGoogleUser.hasImage() && mGoogleUser.getImage().hasUrl()) {
            picasso.load(mGoogleUser.getImage().getUrl() + "&sz=250")
                    .into(b.profileImage);
        }

        if(mGoogleUser.hasCover() && mGoogleUser.getCover().hasCoverPhoto()) {
            picasso.load(mGoogleUser.getCover().getCoverPhoto().getUrl())
                    .fit()
                    .into(b.backgroundImage);
        }

        b.setUser(mGoogleUser);

        b.profileImage.bringToFront(); //HACKS

        return b.getRoot();
    }

}

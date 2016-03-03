package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {
    private static final String SAVED_INSTANCE_CURRENT_USER = "currentUser";

    public static ProfileFragment newInstance(User currentUser) {
        ProfileFragment f = new ProfileFragment();

        Bundle args = new Bundle();
        args.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(currentUser));
        f.setArguments(args);

        return f;
    }

    private User mCurrentUser;

    @Inject
    Picasso picasso;


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getArguments() != null && getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER) != null)
            mCurrentUser = Parcels.unwrap(getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER));

        if(mCurrentUser == null)
            mCurrentUser = Parcels.unwrap(savedInstanceState.getParcelable(SAVED_INSTANCE_CURRENT_USER));

        // Inflate the layout for this fragment
        FragmentProfileBinding b = FragmentProfileBinding.inflate(inflater, container, false);

        if(mCurrentUser.avatarUrl != null && !mCurrentUser.avatarUrl.isEmpty()) {
            picasso.load(mCurrentUser.avatarUrl + "&sz=250")
                    .into(b.profileImage);
        }

        if(mCurrentUser.coverUrl != null && !mCurrentUser.coverUrl.isEmpty()) {
            picasso.load(mCurrentUser.coverUrl)
                    .fit()
                    .centerCrop()
                    .into(b.backgroundImage);
        }

        b.setUser(mCurrentUser);

        if(Build.VERSION.SDK_INT >= 21) {
            b.profileUserCard.setClipToOutline(false);
            b.profileImage.setClipToOutline(false);
        }
        return b.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(mCurrentUser));
    }
}

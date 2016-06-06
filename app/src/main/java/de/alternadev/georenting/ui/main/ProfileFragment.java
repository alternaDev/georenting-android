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
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.FragmentProfileBinding;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    private FragmentProfileBinding mBinding;

    @Inject
    GeoRentingService mService;

    @Inject
    Picasso mPicasso;


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
        mBinding = FragmentProfileBinding.inflate(inflater, container, false);

        if(mCurrentUser.avatarUrl != null && !mCurrentUser.avatarUrl.isEmpty()) {
            mPicasso.load(mCurrentUser.avatarUrl + "&sz=250")
                    .placeholder(R.drawable.default_avatar)
                    .into(mBinding.profileImage);
        }

        mBinding.setUser(mCurrentUser);

        if(Build.VERSION.SDK_INT >= 21) {
            mBinding.profileUserCard.setClipToOutline(false);
            mBinding.profileImage.setClipToOutline(false);
        }

        loadCashStatus();

        return mBinding.getRoot();
    }

    private void loadCashStatus() {
        mService.getCash().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((cash) -> {
                    mBinding.setCash(cash);
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(mCurrentUser));
    }
}

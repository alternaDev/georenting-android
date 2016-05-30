package de.alternadev.georenting.data.auth;


import android.app.Application;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.ApiModule;
import de.alternadev.georenting.data.api.AvatarService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.auth.AuthModule;
import de.alternadev.georenting.data.auth.GoogleAuth;
import okhttp3.HttpUrl;
import rx.Observable;

public class TestAuthModule extends AuthModule {

    private final OptionalPendingResult<GoogleSignInResult> nullResult = new OptionalPendingResult<GoogleSignInResult>() {
        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public GoogleSignInResult get() {
            return new GoogleSignInResult(null, new Status(0));
        }

        @NonNull
        @Override
        public GoogleSignInResult await() {
            return new GoogleSignInResult(null, new Status(0));
        }

        @NonNull
        @Override
        public GoogleSignInResult await(long l, @NonNull TimeUnit timeUnit) {
            return new GoogleSignInResult(null, new Status(0));
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setResultCallback(@NonNull ResultCallback<? super GoogleSignInResult> resultCallback) {

        }

        @Override
        public void setResultCallback(@NonNull ResultCallback<? super GoogleSignInResult> resultCallback, long l, @NonNull TimeUnit timeUnit) {

        }
    };

    @Override
    GoogleAuth provideGoogleAuth(Application application) {
        GoogleAuth gA = Mockito.mock(GoogleAuth.class);
        AvatarService aS = new AvatarService(HttpUrl.parse(ApiModule.PRODUCTION_API_URL));

        User u = new User();
        u.name = "Peter B.";
        u.id = 0;
        u.balance = 42.13;
        u.avatarUrl = aS.getAvatarUrl(u.name);


        SessionToken t = new SessionToken();
        t.token = "token";
        t.user = u;


        Mockito.when(gA.getAuthTokenSilent(Mockito.any())).thenReturn(nullResult);
        Mockito.when(gA.handleSignIn(Mockito.any(GoogleSignInResult.class))).thenReturn(Observable.just(t));
        Mockito.when(gA.getGoogleSignInOptions()).thenReturn(new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode("peda", false)
                .build());
        return gA;
    }
}

package com.remotearthsolutions.expensetracker.services;

import android.content.Context;
import android.content.Intent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.remotearthsolutions.expensetracker.R;
import com.remotearthsolutions.expensetracker.utils.Constants;

public class GoogleServiceImpl implements GoogleService {
    private static final String GOOGLE_CLIENT_ID = Constants.KEY_GOOGLE_CLIENT_ID;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;

    public GoogleServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public void initializeGoogleSigninClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    @Override
    public void startGoogleLogin(Intent data, Callback callback) {

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String token = account.getIdToken();
            AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
            callback.onSocialLoginSuccess(credential);
        } catch (ApiException e) {
            e.printStackTrace();
            callback.onSocialLoginFailure(context.getString(R.string.google_signin_failed));
        }
    }


    @Override
    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }


}

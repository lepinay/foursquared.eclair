/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.app;

import com.joelapenna.foursquared.LoginActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AuthenticationService extends Service {
    private static final String TAG = "AuthenticationService";

    public static final String ACCOUNT_TYPE = "com.joelapenna.foursquared.ACCOUNT";

    public static final String OPTION_USERNAME = "com.joelapenna.foursquared.account.option.USERNAME";
    public static final String OPTION_PASSWORD = "com.joelapenna.foursquared.account.option.PASSWORD";

    @Override
    public IBinder onBind(Intent intent) {
        if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            Log.d(TAG, "onBind: " + intent.toString());
            return new AccountAuthenticator(this).getIBinder();
        } else if ("android.content.SyncAdapter".equals(intent.getAction())) {
            return new SyncAdapter(this, false).getSyncAdapterBinder();
        } else {
            return null;
        }
    }

    public static Bundle createSystemAccount(Context context, String username, String password)
            throws OperationCanceledException, AuthenticatorException, IOException {
        Log.d(TAG, "createSystemAccount: " + username);
        Bundle options = new Bundle();
        options.putString(OPTION_USERNAME, username);
        options.putString(OPTION_PASSWORD, password);
        AccountManagerFuture<Bundle> future = AccountManager.get(context).addAccount(ACCOUNT_TYPE, null,
                null, options, null, null, null);
        return future.getResult(); // Blocks
        //return null;
    }

    /**
     * @author Joe LaPenna (joe@joelapenna.com)
     *
     */
    private final class SyncAdapter extends AbstractThreadedSyncAdapter {
        /**
         * @param context
         * @param autoInitialize
         */
        private SyncAdapter(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                ContentProviderClient provider, SyncResult syncResult) {

        }
    }

    public class AccountAuthenticator extends AbstractAccountAuthenticator {

        public AccountAuthenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            Log.d(TAG, "addAccount");

            if (options != null && options.containsKey(OPTION_USERNAME)
                    && options.containsKey(OPTION_PASSWORD)) {
                // If we're called with a username/password from the app UI.
                Log.d(TAG, "called from app UI");
                final Account account = new Account(options.getString(OPTION_USERNAME),
                        ACCOUNT_TYPE);
                AccountManager.get(AuthenticationService.this).addAccountExplicitly(account,
                        options.getString(OPTION_PASSWORD), null);
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, options
                        .getString(OPTION_USERNAME));
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
                return result;

            } else {
                // If we've come from the AccountManager.
                Log.d(TAG, "called from account manager");
                Intent loginActivityIntent = new Intent(LoginActivity.ACTION_LOGIN);
                loginActivityIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                        response);

                Bundle result = new Bundle();
                result.putParcelable(AccountManager.KEY_INTENT, loginActivityIntent);
                return result;
            }
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                Bundle options) throws NetworkErrorException {
            Log.d(TAG, "confirmCredentials");
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
            return result;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            Log.d(TAG, "editProperties");
            return new Bundle();
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle options) throws NetworkErrorException {
            Log.d(TAG, "getAuthToken");
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, "this is not a real auth token.");
            return result;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            Log.d(TAG, "getAuthTokenLabel");
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                String[] features) throws NetworkErrorException {
            Log.d(TAG, "hasFeatures");
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
            return result;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle options) throws NetworkErrorException {
            Log.d(TAG, "updateCredentials");
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            return result;
        }
    }
}
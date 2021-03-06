package cordova.plugin.paywithbanknotes;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.provider.Settings;
import android.util.Log;
import android.os.Bundle;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;

public class PayWithBankNotesPlugin extends CordovaPlugin {

    public static final String TAG = "PayWithBankNotesPlugin";

    private CallbackContext _callbackContext;
    private CordovaInterface _cordova;
    public Context applicationContext;
    private PayWithBankNotesPlugin instance;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "initialize");

        instance = this;
        _cordova = cordova;
        applicationContext = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        _callbackContext = callbackContext;

        if (action.equals("createAccount")) {
            this.addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            return true;
        } 
        else if (action.equals("updateContact")) {
            JSONObject payload = args.getJSONObject(0);
            String id = payload.getString("id");
            String iban = payload.getString("iban");
            ContactsManager.updateContactById(applicationContext, id, iban);
            callbackContext.success();
            return true;
        }

        return false;
    }

	private void addNewAccount(String accountType, String authTokenType) {
        AccountManager accountManager = AccountManager.get(applicationContext);
        Account[] accounts = accountManager.getAccountsByType(accountType);
        Log.i(TAG, "Accounts length: " + accounts.length);
		if (accounts.length == 0) {
            final AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, authTokenType, null, null, _cordova.getActivity(), 
                new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bnd = future.getResult();
                        Log.i(TAG, "Account was created");
                        _callbackContext.success();
                    } 
                    catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }, null);
        }
        else {
            _callbackContext.success();
        }
    }
   
    
}

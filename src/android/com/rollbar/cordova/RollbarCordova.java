package com.rollbar.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.rollbar.api.payload.data.Client;
import com.rollbar.api.payload.data.Level;
import com.rollbar.api.payload.data.Notifier;
import com.rollbar.api.payload.data.Person;
import com.rollbar.api.payload.data.Request;
import com.rollbar.api.payload.data.Server;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import com.rollbar.notifier.filter.Filter;
import com.rollbar.notifier.fingerprint.FingerprintGenerator;
import com.rollbar.notifier.provider.Provider;
import com.rollbar.notifier.sender.Sender;
import com.rollbar.notifier.transformer.Transformer;
import com.rollbar.notifier.uuid.UuidGenerator;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.content.pm.PackageManager;
import android.util.Log;

public class RollbarCordova extends CordovaPlugin {
    private Rollbar rollbar;

    public static final String TAG = "Rollbar";

    private  String accessToken;
    private  String environment;
    private int versionCode;
    private String versionName;
    private String packageName;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Context context = this.cordova.getActivity().getApplicationContext();

        try {
            this.packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);

            this.versionCode = info.versionCode;
            this.versionName = info.versionName;


        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting package info.");
        }

        List<String> appPackages = Arrays.asList(packageName);
        this.accessToken = webView.getPreferences().getString("rollbarclienttoken", "");
        Config config = ConfigBuilder.withAccessToken(this.accessToken).codeVersion(this.versionName).appPackages(appPackages).build();

        rollbar = Rollbar.init(config);



    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("sendJsonPayload".equals(action)) {
            Config config = rollbar.config();

            JSONObject j = new JSONObject(args.getString(0));

            this.environment = j.getJSONObject("data").getString("environment");
            Config runtimeConfig = ConfigBuilder.withConfig(config).environment(this.environment).build();

            rollbar.configure(runtimeConfig);
            rollbar.sendJsonPayload(args.getString(0));

            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }
}

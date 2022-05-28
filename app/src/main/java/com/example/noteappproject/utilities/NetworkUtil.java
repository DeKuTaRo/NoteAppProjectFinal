package com.example.noteappproject.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtil {
    public static boolean isNetworkAvailable(Context context){
        if (context == null){
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( connectivityManager == null ){
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Network network = connectivityManager.getActiveNetwork();

            if ( network == null ){
                return false;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

            return networkCapabilities != null &&
                    ( networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) );
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }
}

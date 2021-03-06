package com.liu.mymy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.liu.mymy.util.LogUtil;

/**
 * 网络改变监听广播
 * <p>
 * 监听网络改变状态，只有在用户操作网络连接开关(wifi,mobile)的时候接受广播,
 * 然后对相应的界面进行相应的操作，并将状态保存在我们的APP里面
 * </p>
 * Created by liu on 2016/11/3.
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkConnectChangedReceiver.class.getSimpleName();

    private volatile static NetworkConnectChangedReceiver networkConnectChangedReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
//        listeningToWifi(intent);
//        listenToNetworkStateChanged(intent);
        listenToConnectivity(context, intent);
    }

    public static NetworkConnectChangedReceiver getInstance() {
        if (networkConnectChangedReceiver == null) {
            synchronized (NetworkConnectChangedReceiver.class) {
                if (networkConnectChangedReceiver == null) {
                    networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
                }
            }
        }
        return networkConnectChangedReceiver;
    }

    public void registerNetworkConnectChangedReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(networkConnectChangedReceiver, filter);
    }

    public void unRegisterNetworkConnectChangedReceiver(Context context) {
        if (networkConnectChangedReceiver != null) {
            context.unregisterReceiver(networkConnectChangedReceiver);
        }
    }

    /**
     * 监听Wifi的打开与关闭
     *
     * @param intent intent
     */
    private void listeningToWifi(Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            LogUtil.i(TAG, "WifiState" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED: //wifi不可用
                    break;
                case WifiManager.WIFI_STATE_DISABLING://wifi正在关闭
                    break;
                case WifiManager.WIFI_STATE_ENABLING://wifi正在打开
                    break;
                case WifiManager.WIFI_STATE_ENABLED://wifi可用
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN://wifi状态不可知
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
     * .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
     * 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
     * 当然刚打开wifi肯定还没有连接到有效的无线
     *
     * @param intent intent
     */
    private void listenToNetworkStateChanged(Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                LogUtil.e(TAG, "isConnected" + isConnected);
                if (isConnected) {
//                    APP.getInstance().setWifi(true);
                } else {
//                    APP.getInstance().setWifi(false);
                }
            }
        }
    }

    /**
     * 监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
     * 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
     * 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
     *
     * @param context context
     * @param intent  intent
     */
    private void listenToConnectivity(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            LogUtil.d(TAG, "CONNECTIVITY_ACTION");

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
//                        APP.getInstance().setWifi(true);
                        LogUtil.d(TAG, "当前WiFi连接可用 ");
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
//                        APP.getInstance().setMobile(true);
                        LogUtil.d(TAG, "当前移动网络连接可用 ");
                    }
                } else {
                    LogUtil.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                }


                LogUtil.e(TAG, "info.getTypeName()" + activeNetwork.getTypeName());
                LogUtil.e(TAG, "getSubtypeName()" + activeNetwork.getSubtypeName());
                LogUtil.e(TAG, "getState()" + activeNetwork.getState());
                LogUtil.e(TAG, "getDetailedState()"
                        + activeNetwork.getDetailedState().name());
                LogUtil.e(TAG, "getDetailedState()" + activeNetwork.getExtraInfo());
                LogUtil.e(TAG, "getType()" + activeNetwork.getType());
            } else {   // not connected to the internet
                LogUtil.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
//                APP.getInstance().setWifi(false);
//                APP.getInstance().setMobile(false);
//                APP.getInstance().setConnected(false);

            }
        }
    }
}

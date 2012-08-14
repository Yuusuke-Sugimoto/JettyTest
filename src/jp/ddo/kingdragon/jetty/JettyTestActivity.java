package jp.ddo.kingdragon.jetty;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import dalvik.system.DexClassLoader;

/**
 * JettyをAndroidアプリに組み込むテスト用のアプリ
 * @author 杉本祐介
 */
public class JettyTestActivity extends Activity {
    // 定数の宣言
    /**
     * ポート番号
     */
    private static final int PORT_NUMBER = 8080;
    
    // 変数の宣言
    /**
     * 保存用フォルダ
     */
    private File baseDir;
    /**
     * サーバのインスタンス
     */
    private Server mServer;
    /**
     * スリープ時にWi-Fiを維持するために使用
     */
    private WakeLock mWakeLock;
    /**
     * スリープ時にWi-Fiを維持するために使用
     */
    private WifiLock mWifiLock;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 保存用フォルダの作成
        baseDir = new File(Environment.getExternalStorageDirectory(), "JettyTest");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            Toast.makeText(JettyTestActivity.this, R.string.error_make_directory_failed, Toast.LENGTH_SHORT).show();

            finish();
        }

        /**
         * 端末のIPアドレスを取得
         * 参考:[Android] Wi-fi接続時のIP Address（アドレス）を取得 - adakoda
         *      http://www.adakoda.com/adakoda/2009/03/android-wi-fiip-address.html
         */
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = mWifiInfo.getIpAddress();
        String strIpAddress = (ipAddress & 0xff) + "."
                              + ((ipAddress >> 8)  & 0xff) + "."
                              + ((ipAddress >> 16) & 0xff) + "."
                              + ((ipAddress >> 24) & 0xff);
        TextView mTextView = (TextView)findViewById(R.id.local_ip_address);
        mTextView.setText(strIpAddress + ":" + JettyTestActivity.PORT_NUMBER);

        /**
         * スリープ時にWi-Fiを維持する
         * 参考:android - How do I keep Wifi from disconnecting when phone is asleep? - Stack Overflow
         *      http://stackoverflow.com/questions/3871824/how-do-i-keep-wifi-from-disconnecting-when-phone-is-asleep
         */
        PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
        
        /**
         * サーバを起動
         * 参考:ループのスケッチブック >> AndroidアプリにJettyを組込む
         *      http://www.loopsketch.com/blog/2011/08/31/940/
         */
        mServer = new Server(8080);
        HandlerList mHandlerList = new HandlerList();
        
        ResourceHandler mResourceHandler = new ResourceHandler();
        mResourceHandler.setResourceBase(baseDir.getAbsolutePath());
        mHandlerList.addHandler(mResourceHandler);
        
        ServletContextHandler mServletContextHandler = new ServletContextHandler();
        mServletContextHandler.addServlet(TestServlet.class, "/TestServlet");
        File servletDir = new File(baseDir, "Servlet");
        File[] classes = servletDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                boolean isClass = false;
                
                if (filename.endsWith(".dex")) {
                    isClass = true;
                }
                
                return isClass;
            }
        });
        DexClassLoader loader;
        for (File classFile : classes) {
            loader = new DexClassLoader(classFile.getAbsolutePath(), baseDir.getAbsolutePath(),
                                        null, getClassLoader());
            try {
                String classFileName = classFile.getName();
                String className = classFileName.substring(0, classFileName.length() - 4);
                Class<?> loadedClass = loader.loadClass(getPackageName() + "." + className);
                mServletContextHandler.addServlet(loadedClass.asSubclass(Servlet.class), "/" + className);
            }
            catch (ClassNotFoundException e) {
                Log.e("onCreate", e.getMessage(), e);
            }
            catch (ClassCastException e) {
                Log.e("onCreate", e.getMessage(), e);
            }
        }
        mHandlerList.addHandler(mServletContextHandler);
        
        mServer.setHandler(mHandlerList);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServer.start();
                }
                catch (Exception e) {
                    Log.e("onCreate", e.getMessage(), e);
                }
            }
        }).start();
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (mServer != null) {
            try {
                mServer.stop();
            }
            catch (Exception e) {
                Log.e("onDestroy", e.getMessage(), e);
            }
        }
    }
}
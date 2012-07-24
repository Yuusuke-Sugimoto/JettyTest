package jp.ddo.kingdragon.jetty;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * JettyをAndroidアプリに組み込むテスト用のアプリ
 * 参考:ループのスケッチブック >> AndroidアプリにJettyを組込む
 *      http://www.loopsketch.com/blog/2011/08/31/940/
 * @author 杉本祐介
 */
public class JettyTestActivity extends Activity {
    // 変数の宣言
    private Server mServer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mServer = new Server();
        SocketConnector connector = new SocketConnector();
        connector.setPort(8080);
        mServer.addConnector(connector);
        
        ServletContextHandler servletContext = new ServletContextHandler();
        ServletHolder holder = new ServletHolder();
        servletContext.addServlet(holder, "/*");
        
        HandlerList mHandlerList = new HandlerList();
        mHandlerList.addHandler(servletContext);
        mServer.setHandler(mHandlerList);
        
        try {
            mServer.start();
        }
        catch (Exception e) {
            Log.e("onCreate", e.getMessage(), e);
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
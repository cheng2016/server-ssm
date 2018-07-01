package com.cheng.server.main;

import com.cheng.server.socket.NettyServerThread;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 将socket service随tomcat启动
 *
 * @author huajian
 */
public class SocketServiceLoader extends ContextLoader implements ServletContextListener {
    //socket server 线程
    private SocketThread socketThread;

    private NettyServerThread nettyServerThread;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (null != socketThread && !socketThread.isInterrupted()) {
            socketThread.closeSocketServer();
            socketThread.interrupt();
        }
        if (null != nettyServerThread && !nettyServerThread.isInterrupted()) {
            nettyServerThread.shutdown();
            nettyServerThread.interrupt();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
            // TODO Auto-generated method stub
        if (null == socketThread) {
            //新建线程类
            socketThread = new SocketThread(null);
            //启动线程
            socketThread.start();
        }

        if(null == nettyServerThread){
            nettyServerThread = new NettyServerThread();
            nettyServerThread.start();
        }
    }
}

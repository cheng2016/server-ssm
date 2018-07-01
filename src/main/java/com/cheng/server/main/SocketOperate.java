package com.cheng.server.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * 多线程处理socket接收的数据
 * @author huajian
 *
 */
public class SocketOperate extends Thread{
    private Socket socket;

    public SocketOperate(Socket socket) {
        this.socket=socket;
    }
    @SuppressWarnings("unused")
    public void run()
    {
        try{

            InputStream in= socket.getInputStream();

            PrintWriter out=new PrintWriter(socket.getOutputStream());

            //BufferedReader wt = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                //读取客户端发送的信息
                String strXML = "";
                byte[] temp = new byte[1024];
                int length = 0;
                while((length = in.read(temp)) != -1){
                    strXML += new String(temp,0,length);
                }
                if("end".equals(strXML)){
                    System.out.println("准备关闭socket");
                    break;
                }
                if("".equals(strXML))
                    continue;

                System.out.println("客户端发来："+strXML.toString());

// MethodHandler mh = new MethodHandler(ReadXML.readXML(strXML.toString()));
// String resultXML = mh.getResultXML();
// System.out.println("返回："+resultXML.toString());

// if(!"".equals(resultXML)){
// out.print(resultXML);
                out.flush();
                out.close();
// }


            }
            socket.close();
            System.out.println("socket stop.....");

        }catch(IOException ex){

        }finally{

        }
    }
}
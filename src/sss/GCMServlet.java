package sss;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


/**
 * GCMのサーバ・サンプル・サーブレット
 * http://www.kotemaru.org/2013/07/28/android-push-message.html
 * 
 * 注：いろいろ端折ってます。Googleのサンプルも参照してください。
 * 
 * @author @kotemaru.org
 */

public class GCMServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * https://code.google.com/apis/console/ で生成したAPIキー。
     */
    private static final String API_KEY = Info.gcm_key;
    private static final int RETRY_COUNT = 5;
    private DatabaseClass db;
    
    public void init() throws ServletException{
    	try {
			db = new DatabaseClass();
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }
    
    public void destroy(){
    	try {
			db.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }

    /**
     * ユーザIDからRegistrationIdを引くテーブル。
     * -本来はストレージに保存すべき情報。
     * -key=ユーザID: サービスの管理するＩＤ。
     * -value=RegistrationId: AndroidがGCMから取得した端末ＩＤ。
     */

    public void doPost(HttpServletRequest req, HttpServletResponse res) 
            throws IOException {
    	res.setContentType("text/html; charset=UTF-8");
    	PrintWriter out = res.getWriter();
    	
        System.out.println("=> "+req.getQueryString());
        String action         = req.getParameter("action");
        String registrationId = req.getParameter("regId");
        String userId         = req.getParameter("userId");

        if ("register".equals(action)) {
            // 端末登録、Androidから呼ばれる。
            try {
				db.registAndroid(userId,registrationId);
				res.setStatus(200);
				out.println("register ok");
				return;
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

        } else if ("unregister".equals(action)) {
            // 端末登録解除、Androidから呼ばれる。
            try {
				db.logout(userId, registrationId);
				res.setStatus(200);
				out.println("unregister ok");
				return;
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
        } else {
            res.setStatus(500);
        }
    }
    
    /**
     * ファイルパスをAndroid端末へ送信する（GCM経由）
     * @param path
     * @param sender_id
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void sendPathInfo(String path,String sender_id) throws IOException, ClassNotFoundException, SQLException{
    	List<String> registers = null;
    	DatabaseClass dba = new DatabaseClass();
		try {
			registers = dba.getRegisteredIDs(sender_id);
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        Sender sender = new Sender(API_KEY);
        Message message = new Message.Builder().addData("msg", path).build();
        System.out.println("sender:"+sender_id);
        for(String regist:registers){
        	System.out.println("registered_id:"+regist);
        	if(regist.equals(sender_id)) continue;
            Result result = sender.send(message, regist, RETRY_COUNT);
//            res.setContentType("text/plain");
//            res.getWriter().println("Result="+result);
            System.out.println("Result="+result);
        }
        dba.close();    
    }
}
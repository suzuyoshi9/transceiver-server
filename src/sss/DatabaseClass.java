package sss;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * データベース接続用のクラス
 * @author suzuyoshi
 *
 */
public class DatabaseClass{
   Connection conn;
   String url = "jdbc:mysql://"+Info.db_host+"/"+Info.db_name+
                                "?useUnicode=true&characterEncoding=utf-8";
   
   /**
    * コンストラクタ
    * ドライバの読み込みとDBへの接続
    */
    public DatabaseClass() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.jdbc.Driver");
        this.connect();
    }
	
    /**
     * DBへの接続
     * @throws SQLException
     */
    private void connect() throws SQLException{
    	this.conn=DriverManager.getConnection(this.url,Info.db_user,Info.db_pass);
    }
    
    /**
     * DBの接続を閉じる
     * @throws SQLException
     */
    public void close() throws SQLException{
    	this.conn.close();
    	this.conn=null;
    }

    /**
     * ログイン処理
     * パスワードの確認とログイン状態の変更
     * @param username
     * @param password
     * @return ログインの成否(1:成功 0:失敗)
     * @throws SQLException
     */
    public int login(String username, String password) throws SQLException{
    	this.Reconnect();
    	boolean result = checkPassword(username,password);
        if(!result) return 0;
        System.out.println("pass ok");
        String sql="update user set logged_in = ? where name = ?";
        PreparedStatement pst = this.conn.prepareStatement(sql);
        pst.setInt(1,1);
        pst.setString(2,username);
        if(pst.executeUpdate()==1) return 1;
        else return 0;

    }
    
    /**
     * ログアウト処理
     * 通知トークンの削除とログイン状態の変更
     * @param username
     * @param regid
     * @return ログアウトの成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean logout(String username,String regid) throws SQLException{
    	int uid=this.getUid(username);
    	this.unregistAndroid(regid);
    	if(this.getRegistredDeviceNum(uid)==0){
    		String sql="update user set logged_in = ? where name = ?";
            PreparedStatement pst = this.conn.prepareStatement(sql);
            pst.setInt(1,0);
            pst.setString(2,username);
    		if(pst.executeUpdate()==1) return true;
    		else return false;
    	}
    	return true;
    }

    /** 
     * ユーザの追加
     * 同じユーザ名がDB上に無ければ追加
     * @param username
     * @param nickname
     * @param password
     * @return ユーザ追加の成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean addUser(String username,String nickname, String password) throws SQLException{
        System.out.println("user:"+username);
        System.out.println("nick:"+nickname);
        System.out.println("pass:"+password);
        this.Reconnect();
        if(this.isUserExist(username)) return false;
    	String sql="insert into user(name,nickname,pass) values(?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,username);
    	pst.setString(2,nickname);
    	pst.setString(3,password);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    /**
     * チャンネルの追加
     * 同じチャンネル名が無ければ追加
     * @param username
     * @param groupname
     * @return チャンネル追加の成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean addGroup(String username,String groupname) throws SQLException{
    	this.Reconnect();
    	if(this.isGroupExist(groupname)) return false;
    	String sql="insert into groups(name,creator_uid) values(?,?)";
    	int uid = this.getUid(username);
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,groupname);
    	pst.setInt(2,uid);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }

    /**
     * ユーザの削除（未実装）
     * @return
     */
    public boolean deleteUser(){
    	return false;
    }
    
    /**
     * チャンネルの取得
     * チャンネルの一覧と現在参加しているチャンネルを取得します
     * @param registID
     * @return チャンネル一覧+現在参加しているチャンネル情報
     * @throws SQLException
     */
    public List<String> getGroups(String registID) throws SQLException{
    	this.Reconnect();
    	List<String> list = new ArrayList<String>();
    	String sql="select name from groups";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	ResultSet rs = pst.executeQuery();
    	while(rs.next()){
    		list.add(rs.getString("name"));
    	}
    	String now = "now:"+this.getGroupName(this.getParticipateGid(registID));
    	list.add(now);
    	return list;
    }
    
    /**
     * チャンネルの削除（未実装）
     * @return
     */
    public boolean deleteGroup(){
    	return false;
    }
    
    /**
     * 通知用トークンの登録
     * 既に登録されている場合は、最終ログイン日時の更新のみ行います
     * @param userid
     * @param registID
     * @return 登録の成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean registAndroid(String userid,String registID) throws SQLException{
    	this.Reconnect();
    	Date date = new Date(System.currentTimeMillis());
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	int uid=this.getUid(userid);
    	if(this.isalreadyregistered(uid, registID)){
    		String sql="update notification set last_login = ? where regist_id = ?";
    		PreparedStatement pst = this.conn.prepareStatement(sql);
    		pst.setString(1, df.format(date));
    		pst.setString(2, registID);
    		if(pst.executeUpdate()==1) return true;
    		else return false;
    	}
    	if(!this.unregistAndroid(registID)) return false;
    	String sql="insert into notification(uid,regist_id,last_login) values(?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	pst.setString(2, registID);
    	pst.setString(3, df.format(date));
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    /**
     * 通知用トークンの削除
     * @param registid
     * @return 削除の成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean unregistAndroid(String registid) throws SQLException{
    	this.Reconnect();
    	String sql="delete from notification where regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, registid);
    	pst.executeUpdate();
    	return true;
    }
    
    /**
     * 通知用トークンが既にDB上にあるかどうか
     * @param uid
     * @param registID
     * @return 通知用トークンがDB上にあるかないか(true:ある false:ない)
     * @throws SQLException
     */
    public boolean isalreadyregistered(int uid,String registID) throws SQLException{
    	this.Reconnect();
    	String sql="select ID from notification where uid=? and regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	pst.setString(2, registID);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }
    
    /**
     * 同チャンネルに参加しているAndroid端末トークンの取得
     * @param sender_id
     * @return 複数の端末トークン
     * @throws SQLException
     */
    public List<String> getRegisteredIDs(String sender_id) throws SQLException{
    	this.Reconnect();
    	List<String> list = new ArrayList<String>();
    	int sender_gid = this.getParticipateGid(sender_id);
    	String sql="select regist_id from notification where participate_gid=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, sender_gid);
    	ResultSet rs = pst.executeQuery();
    	while(rs.next()){
    		list.add(rs.getString("regist_id"));
    	}
    	return list;
    }
    
    /**
     * 音声ファイルをDBに追加
     * @param userid
     * @param registid
     * @param path
     * @return 追加結果(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean addFile(String userid,String registid,String path) throws SQLException{
    	this.Reconnect();
    	int uid=this.getUid(userid);
    	int gid=this.getParticipateGid(registid);
    	String sql="insert into files(uid,gid,path) values(?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	pst.setInt(2, gid);
    	pst.setString(3, path);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    /**
     * ログインされているか
     * @param username
     * @return ログインされているか否か(true:ログインされている false:ログインされていない)
     * @throws SQLException
     */
    public boolean isLoggedin(String username) throws SQLException{
    	this.Reconnect();
    	String sql="select logged_in from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, username);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()){
    		return rs.getInt("logged_in")==1 ? true:false; 
    	}
    	else throw new SQLException("User NotFound");
    }
    
    /**
     * チャンネルの変更
     * @param regid
     * @param groupname
     * @return 変更の成否(true:成功 false:失敗)
     * @throws SQLException
     */
    public boolean changeGroup(String regid,String groupname) throws SQLException{
    	this.Reconnect();
    	int gid = this.getGid(groupname);
    	String sql="update notification set participate_gid = ? where regist_id = ?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, gid);
    	pst.setString(2, regid);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    	
    }
    
    public String getNickname(String username) throws SQLException{
    	this.Reconnect();
    	String sql="select nickname from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, username);
    	ResultSet rs = pst.executeQuery();
    	if(!rs.next()) throw new SQLException("User NotFound");
    	return rs.getString("nickname");
    }
    
    /**
     * 同ユーザ名のユーザがいるか否か
     * @param username
     * @return (true:いる false:いない)
     * @throws SQLException
     */
    private boolean isUserExist(String username) throws SQLException{
    	this.Reconnect();
    	String sql="select name from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,username);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }
    
    /**
     * 同チャンネル名のチャンネルがあるか否か
     * @param groupname
     * @return (true:いる false:いない)
     * @throws SQLException
     */
    private boolean isGroupExist(String groupname) throws SQLException{
    	this.Reconnect();
    	String sql="select name from groups where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, groupname);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }
    
    /**
     * パスワードのチェック
     * @param username
     * @param input_pass
     * @return パスワードの確認結果(true:合致 false:不一致)
     * @throws SQLException
     */
    private boolean checkPassword(String username,String input_pass) throws SQLException{
    	this.Reconnect();
        String sql="select pass from user where name=?";
        PreparedStatement pst = this.conn.prepareStatement(sql);
        pst.setString(1,username);
        ResultSet rs = pst.executeQuery();
        if(!rs.next()) throw new SQLException("User NotFound");
        String pass = rs.getString("pass");
        if(input_pass.equals(pass)) return true;
        else return false;
    }
    
    /**
     * ユーザ名からidを取得
     * @param username
     * @return id
     * @throws SQLException
     */
    private int getUid(String username) throws SQLException{
    	this.Reconnect();
    	String sql="select ID from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, username);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("ID");
    	else throw new SQLException("User NotFound");
    }
    
    /**
     * チャンネル名からidを取得
     * @param groupname
     * @return id
     * @throws SQLException
     */
    private int getGid(String groupname) throws SQLException{
    	this.Reconnect();
    	String sql="select ID from groups where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, groupname);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("ID");
    	else throw new SQLException("Group NotFound");
    }
    
    /**
     * 参加しているチャンネルのidを取得
     * @param registID
     * @return id
     * @throws SQLException
     */
    private int getParticipateGid(String registID) throws SQLException{
    	this.Reconnect();
    	String sql="select participate_gid from notification where regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, registID);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("participate_gid");
    	else throw new SQLException("registID NotFound");
    }
    
    /**
     * idからチャンネル名を取得
     * @param gid
     * @return チャンネル名
     * @throws SQLException
     */
    private String getGroupName(int gid) throws SQLException{
    	this.Reconnect();
    	String sql="select name from groups where ID=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, gid);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getString("name");
    	else throw new SQLException("Group NotFound");
    }
    
    /**
     * 同ユーザ名でDBに登録されているAndroidの端末数を取得
     * @param uid
     * @return 登録されている端末数
     * @throws SQLException
     */
    private int getRegistredDeviceNum(int uid) throws SQLException{
    	this.Reconnect();
    	String sql="select count(ID) from notification where uid=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	ResultSet rs = pst.executeQuery();
    	rs.next();
    	return rs.getInt(1);
    }
    
    /**
     * DBへの接続が閉じられていた場合に、再接続を行う
     * @throws SQLException
     */
    private void Reconnect() throws SQLException{
    	if(this.conn.isClosed()||this.conn==null){
    		this.connect();
    	}
    }
    
    /**
     * オブジェクトがもはや呼ばれなくなった際に呼ばれるメソッド
     * デストラクタ的なもの
     */
    @Override
    protected void finalize() throws Throwable{
    	try{
    		super.finalize();
    	}finally{
    		this.close();
    	}
    }
    
}
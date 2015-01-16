package sss;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class DatabaseClass{
   Connection conn;
   String url = "jdbc:mysql://"+Info.db_host+"/"+Info.db_name+
                                "?useUnicode=true&characterEncoding=utf-8";	

    public DatabaseClass() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.jdbc.Driver");
        this.connect();
    }
	
    private void connect() throws SQLException{
    	this.conn=DriverManager.getConnection(this.url,Info.db_user,Info.db_pass);
    }
    
    public void close() throws SQLException{
    	this.conn.close();
    	this.conn=null;
    }

    public int login(String username, String password) throws SQLException{
        boolean result = checkPassword(username,password);
        if(!result) return 0;
        System.out.println("pass ok");
        /* String sql="update user set logged_in = ? where name = ?";
        PreparedStatement pst = this.conn.prepareStatement(sql);
        pst.setInt(1, 1);
        pst.setString(2, username);
        pst.executeUpdate(sql); */
        return 1;

    }

    public boolean addUser(String username,String nickname, String password) throws SQLException{
        System.out.println("user:"+username);
        System.out.println("nick:"+nickname);
        System.out.println("pass:"+password);
        if(this.isUserExist(username)) return false;
    	String sql="insert into user(name,nickname,pass) values(?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,username);
    	pst.setString(2,nickname);
    	pst.setString(3,password);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    public boolean addGroup(String username,String groupname) throws SQLException{
    	if(this.isGroupExist(groupname)) return false;
    	String sql="insert into groups(name,creator_uid) values(?,?)";
    	int uid = this.getUid(username);
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,groupname);
    	pst.setInt(2,uid);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }

    public boolean deleteUser(){
    	return false;
    }
    
    public List<String> getGroups(String registID) throws SQLException{
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
    
    public boolean deleteGroup(){
    	return false;
    }
    
    public boolean registAndroid(String userid,String registID) throws SQLException{
    	int uid=this.getUid(userid);
    	if(!this.unregistAndroid(registID)) return false;
    	String sql="insert into notification(uid,regist_id,last_login) values(?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	Date date = new Date(System.currentTimeMillis());
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	pst.setInt(1, uid);
    	pst.setString(2, registID);
    	pst.setString(3, df.format(date));
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    public boolean unregistAndroid(String registid) throws SQLException{
    	String sql="delete from notification where regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, registid);
    	pst.executeUpdate();
    	return true;
    }
    
    public List<String> getRegisteredIDs(String sender_id) throws SQLException{
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
    
    public boolean addFile(String userid,String registid,String path) throws SQLException{
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
    
    public boolean isLoggedin(String username) throws SQLException{
    	String sql="select logged_in from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, username);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()){
    		return rs.getInt("logged_in")==1 ? true:false; 
    	}
    	else throw new SQLException("User NotFound");
    }
    
    public boolean changeGroup(String regid,String groupname) throws SQLException{
    	int gid = this.getGid(groupname);
    	String sql="update notification set participate_gid = ? where regist_id = ?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, gid);
    	pst.setString(2, regid);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    	
    }
    
    private boolean isUserExist(String username) throws SQLException{
    	String sql="select name from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,username);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }
    
    private boolean isGroupExist(String groupname) throws SQLException{
    	String sql="select name from groups where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, groupname);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }

    private boolean checkPassword(String username,String input_pass) throws SQLException{
        String sql="select pass from user where name=?";
        PreparedStatement pst = this.conn.prepareStatement(sql);
        pst.setString(1,username);
        ResultSet rs = pst.executeQuery();
        if(!rs.next()) throw new SQLException("User NotFound");
        String pass = rs.getString("pass");
        System.out.println("aaa");
        if(input_pass.equals(pass)) return true;
        else return false;
    }
    
    private int getUid(String username) throws SQLException{
    	String sql="select ID from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, username);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("ID");
    	else throw new SQLException("User NotFound");
    }
    
    private int getGid(String groupname) throws SQLException{
    	String sql="select ID from groups where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, groupname);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("ID");
    	else throw new SQLException("Group NotFound");
    }
    
    private int getParticipateGid(String registID) throws SQLException{
    	String sql="select participate_gid from notification where regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, registID);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getInt("participate_gid");
    	else throw new SQLException("registID NotFound");
    }
    
    private String getGroupName(int gid) throws SQLException{
    	String sql="select name from groups where ID=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, gid);
    	ResultSet rs = pst.executeQuery();
    	if(rs.next()) return rs.getString("name");
    	else throw new SQLException("Group NotFound");
    }
    
}
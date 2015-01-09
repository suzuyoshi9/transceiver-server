package sss;

import java.sql.*;
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
        String sql="insert into user(logged_in,last_login) values(1,now())";
        Statement st = this.conn.createStatement();
        st.executeUpdate(sql);
        
        if(result) return 1;
        else return 0;
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
    
    public boolean addGroup(String username,String groupname, double latitude,double longtitude) throws SQLException{
    	String sql="insert into group(name,adminID,latitude,longtitude) values(?,?,?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,groupname);
    	pst.setInt(2,this.getUid(username));
    	pst.setDouble(3,latitude);
    	pst.setDouble(4,longtitude);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    

    public boolean deleteUser(){
    	return false;
    }
    
    public boolean deleteGroup(){
    	return false;
    }
    
    public boolean registAndroid(String userid,String registID) throws SQLException{
    	int uid=this.getUid(userid);
    	if(!this.unregistAndroid(registID)) return false;
    	String sql="insert into notification(uid,regist_id) values(?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	pst.setString(2, registID);
    	if(pst.executeUpdate()==1) return true;
    	else return false;
    }
    
    public boolean unregistAndroid(String registid) throws SQLException{
    	String sql="delete from notification where regist_id=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1, registid);
    	return true;
    }
    
    public List<String> getRegisteredIDs() throws SQLException{
    	List<String> list = new ArrayList<String>();
    	String sql="select regist_id from notification";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	ResultSet rs = pst.executeQuery();
    	while(rs.next()){
    		list.add(rs.getString("regist_id"));
    	}
    	return list;
    }
    
    public boolean addFile(String userid,String path) throws SQLException{
    	int uid=this.getUid(userid);
    	String sql="insert into files(uid,path) values(?,?)";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setInt(1, uid);
    	pst.setString(2, path);
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
    
    private boolean isUserExist(String username) throws SQLException{
    	String sql="select name from user where name=?";
    	PreparedStatement pst = this.conn.prepareStatement(sql);
    	pst.setString(1,username);
    	ResultSet rs = pst.executeQuery();
    	return rs.next();
    }
    
    private boolean isGroupExist() throws SQLException{
    	return false;
    }

    private boolean checkPassword(String username,String input_pass) throws SQLException{
        String sql="select pass from user where name=?";
        PreparedStatement pst = this.conn.prepareStatement(sql);
        pst.setString(1,username);
        ResultSet rs = pst.executeQuery();
        if(!rs.next()) throw new SQLException("User NotFound");
        String pass = rs.getString("pass");
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
}
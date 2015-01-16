<%@ page contentType="text/html; charset=utf-8" errorPage="error.jsp" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="sss.*" %>
<%@ page import="java.util.*" %>
<%
  DatabaseClass db = new DatabaseClass();
  List<String> list;
  String user=request.getParameter("user");
  String groupname=request.getParameter("groupname");
  if(db.isLoggedin(user)){
	  if(db.addGroup(user,groupname)) out.println("ok");
	  else out.println("ng");
  }
  else out.println("not logged_in");
%>
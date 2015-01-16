<%@ page contentType="text/html; charset=utf-8" errorPage="error.jsp" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="sss.*" %>
<%@ page import="java.util.*" %>
<%
  DatabaseClass db = new DatabaseClass();
  List<String> list;
  String user=request.getParameter("user");
  String regid=request.getParameter("regId");
  String group=request.getParameter("groupname");
  if(db.isLoggedin(user)){
	  if(db.changeGroup(regid, group)) out.println("ok");
	  else out.println("ng");
  }
  else out.println("not logged_in");
  db.close();
%>

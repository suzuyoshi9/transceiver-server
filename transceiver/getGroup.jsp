<%@ page contentType="text/html; charset=utf-8" errorPage="error.jsp" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="sss.*" %>
<%@ page import="java.util.*" %>
<%
  DatabaseClass db = new DatabaseClass();
  List<String> list;
  String user=request.getParameter("user");
  String regid=request.getParameter("regId");
  if(db.isLoggedin(user)){
	  list=db.getGroups(regid);
	  //out.println(list.size());
	  for(String name:list){
		  out.println(name);
	  }
  }
  else out.println("not logged_in");
%>
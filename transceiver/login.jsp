<%@ page contentType="text/html; charset=utf-8" errorPage="error.jsp" %>
<%@ page import="sss.*" %>
<%
  DatabaseClass db = new DatabaseClass();
  String user=request.getParameter("user");
  String pass=request.getParameter("pass");
  out.println(db.login(user,pass));
%>

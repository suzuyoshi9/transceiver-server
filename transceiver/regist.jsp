<%@ page language="java" contentType="text/html; charset=Shift_JIS"
    pageEncoding="Shift_JIS" errorPage="error.jsp"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="sss.*" %>
<%
  request.setCharacterEncoding("UTF-8");
  DatabaseClass db = new DatabaseClass();
  String user=request.getParameter("user");
  String nickname=request.getParameter("nickname");
  String pass1=request.getParameter("pass1");
  String pass2=request.getParameter("pass2");
  
  if(!pass1.equals(pass2)){
    throw new RuntimeException("パスワードが一致していません");
  }
  if(db.addUser(user,nickname,pass1)) out.println("ok");
  else out.println("ng");
  db.close();
%>
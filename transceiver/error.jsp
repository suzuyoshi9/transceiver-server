<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8" isErrorPage="true"%>
<%@ page trimDirectiveWhitespaces="true"%>
<% out.println(exception.toString()); %>
<% exception.printStackTrace(); %>
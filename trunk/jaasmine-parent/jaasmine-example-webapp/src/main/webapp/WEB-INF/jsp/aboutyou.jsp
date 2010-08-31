<%--
 Copyright 2010 LogicLander

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<%--
 JSP that AboutYouServlet dispatches to.  This will display the logged in
 Subject.
--%>
<%@page contentType="text/html" pageEncoding="MacRoman"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=MacRoman">
        <title>All About You</title>
    </head>
    <body>
        <h1>All About You</h1>
        <p>
        <pre>
<c:out value="${subject}"/>
        </pre>
        </p>
        <p><a href="Home">Home</a> | <a href="Logout">Logout</a></p>
    </body>
</html>

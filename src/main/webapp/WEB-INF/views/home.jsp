<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>

<P>  The time on the server is ${serverTime}. </P>

<h1>${message}</h1>
	<br/>
	<a href="db">DB Status</a>
</body>
</html>

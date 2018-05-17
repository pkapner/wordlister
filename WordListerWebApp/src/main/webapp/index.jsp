<%@ page import="com.uestechnology.WordLister" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.uestechnology.SetType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Wordlister</title>
</head>
<body>

<%

    if (request.getParameter("letters") != null) {
        String letters = request.getParameter("letters");
        Set<String> words = WordLister.getWords(letters, SetType.LONG); // only find common words

        for (String word : words) {
            out.println(word);
            out.println("<br>");
        }
    }
%>

</body>
</html>

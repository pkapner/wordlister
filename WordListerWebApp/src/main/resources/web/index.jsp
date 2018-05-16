<%@ page import="com.uestechnology.WordLister" %>
<%@ page import="java.util.Set" %>
<

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>$Title$</title>
</head>
<body>

<%

    if (request.getParam("letters") != null) {
        String letters = request.getParam("letters");
        Set<String> words = WordLister.getWords(letters, false); // only find common words

        for (String word : words) {
            out.println(word);
            out.println("<br>");
        }
    }

%>

</body>
</html>

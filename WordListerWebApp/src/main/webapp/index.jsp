<%@ page import="com.uestechnology.WordLister" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.uestechnology.SetType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
     WordLister wordLister = new WordLister();
%>
<html>
<head>
    <title>Wordlister</title>
</head>
<body>

<%! private StringBuilder displayWords(Set<String> shortWords, Set<String> medWords, Set<String> longWords, SetType type) {
    StringBuilder sb = new StringBuilder();
    sb.append("<table cellspacing='8' cellpadding='8' width='100%'><tr>");
    int columnCount = 0;
    Set<String> curList;
    switch (type) {
        case SHORT:
            curList = shortWords;
            break;
        case MEDIUM:
            curList = medWords;
            break;
        default:
            curList = longWords;
    }

    for (String word : curList) {
        if (shortWords.contains(word) && type == SetType.MEDIUM) {
            sb.append("<td><font face='monospace' color='green' size='7'>");
        } else if (shortWords.contains(word) && type == SetType.LONG) {
            sb.append("<td><font face='monospace' color='red' size='7'>");
        } else {
            sb.append("<td><font face='monospace' color='black' size='7'>");
        }
        sb.append(word).append("</font></td>");
        if (++columnCount == 4) {
            sb.append("</tr><tr>");
            columnCount = 0;
        }
    }
    sb.append("</tr></table>");
    return sb;
}
%>

<form action="index.jsp">
    <label>
        <input name="letters" type="text">
    </label><br>
</form>


<%

    if (request.getParameter("letters") != null) {
        String letters = request.getParameter("letters");
        wordLister.setLetters(letters.toLowerCase());

        final Set<String> commonCombos = wordLister.findCombos(SetType.SHORT);
        final Set<String> moreCombos = wordLister.findCombos(SetType.MEDIUM);
        final Set<String> allCombos = wordLister.findCombos(SetType.LONG);

        StringBuilder sb = displayWords(commonCombos, moreCombos, allCombos, SetType.SHORT);
        out.println(sb.toString());
        out.print("<br><hr><br>");

        sb = displayWords(commonCombos, moreCombos, allCombos, SetType.MEDIUM);
        out.println(sb.toString());
        out.print("<br><hr><br>");


        sb = displayWords(commonCombos, moreCombos, allCombos, SetType.LONG);
        out.println(sb.toString());
        out.print("<br><hr><br>");
    }
%>

</body>
</html>

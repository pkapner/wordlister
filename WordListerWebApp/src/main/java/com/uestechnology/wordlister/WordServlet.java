package com.uestechnology.wordlister;

import com.uestechnology.SetType;
import com.uestechnology.WordLister;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class WordServlet extends HttpServlet {

    static final private WordLister wordLister = new WordLister();

    private StringBuilder displayWords(Set<String> shortWords, Set<String> medWords, Set<String> longWords, SetType type) {
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
            if (++columnCount == 3) {
                sb.append("</tr><tr>");
                columnCount = 0;
            }
        }
        sb.append("</tr></table>");
        return sb;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPut(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html");

        // Actual logic goes here.
        PrintWriter out = response.getWriter();
        out.println("<html><body>\n");
        out.println("<form action=\"wordlister\">\n" +
                "    <label>\n" +
                "        <input name=\"letters\" type=\"text\" autocapitalize=\"none\" autocorrect=\"off\" autocomplete=\"off\">" +
                "    </label><br>\n" +
                "</form>");


        if (request.getParameter("letters") != null) {
            try {
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        out.println("</body></html>\n");
    }


}

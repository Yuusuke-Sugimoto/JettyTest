package jp.ddo.kingdragon.jetty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DynamicServlet extends HttpServlet {
    /**
     * シリアルバージョンUID
     */
    private static final long serialVersionUID = -8862915576392194797L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter pw = response.getWriter();
        pw.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        pw.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">");
        pw.println("<head>");
        pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />");
        pw.println("<meta http-equiv=\"content-style-type\" content=\"text/css\" />");
        pw.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
        pw.println("");
        pw.println("<title>DynamicServlet</title>");
        pw.println("</head>");
        pw.println("<body>");
        pw.println("<p>");
        pw.println("DynamicServletです。<br />");
        pw.println("<a href=\"index.html\" title=\"index.htmlへ\">index.htmlへ</a><br />");
        pw.println("<a href=\"another.html\" title=\"another.htmlへ\">another.htmlへ</a><br />");
        pw.println("<a href=\"TestServlet\" title=\"TestServletへ\">TestServletへ</a><br />");
        pw.println("</p>");
        pw.println("</body>");
        pw.println("</html>");
        pw.close();
    }
}
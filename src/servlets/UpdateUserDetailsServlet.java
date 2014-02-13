package servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import ldap.LdapTool;

@SuppressWarnings("serial")
public class UpdateUserDetailsServlet extends HttpServlet {
	Logger logger = Logger.getRootLogger();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
    {
		String redirectURL = response.encodeRedirectURL("UserDetails.jsp?dn="+request.getParameter("dn"));
		response.sendRedirect(redirectURL);
    }
	
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
    {
		HttpSession session = request.getSession(true);
		Map<String,String[]> paramMaps = (Map<String,String[]>)request.getParameterMap();
		LdapTool lt = new LdapTool();
		String[] updateStatus = lt.updateUser(paramMaps);
		lt.close();
		if( updateStatus[0].equals("true") ){
			session.setAttribute("passed", "User has been updated successfully.");
			logger.info("User has been updated successfully.");
		}else{
			session.setAttribute("failed", updateStatus[1]);
			logger.info(updateStatus[1]);
		}
		String redirectURL = response.encodeRedirectURL("UserDetails.jsp?dn="+updateStatus[1]);
		response.sendRedirect(redirectURL);
	}
}
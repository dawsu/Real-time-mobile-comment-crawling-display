package link;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetData
 */
@WebServlet("/GetData")
public class GetData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetData() {
        super();

    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String time=request.getParameter("time");		
		Connection connect=null;
		Statement statement=null;
		ResultSet result=null;		
		String json=null;	
		Date date = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, -10);
		String time1 = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime());
		try {
			connect = getConnection();
			statement=connect.createStatement();
			String Query=null;
			Query = "select count(*) from tests where tae <'"+time+"' and tae >'"+time1+"'";					
			result = statement.executeQuery(Query);
			while(result.next()){	
				json=result.getObject(1).toString();				
			}	
			result.close();
			statement.close();
			close(connect);
		} catch (SQLException e) {
		}
		System.out.println(time+"----"+time1);
		System.out.println(json);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=gb2312");
		response.getWriter().write(json);
	}
	 public static  Connection getConnection() {
		 Connection con=null;
	     try {
	    	 Class.forName("com.mysql.cj.jdbc.Driver");
	         con=DriverManager.getConnection("jdbc:mysql://localhost:3306/web?serverTimezone=UTC","11111","111111");
	     } catch (Exception e) {	        	
	         e.printStackTrace();
	     }
	     return con;
	 }
	 public static void close(Connection con) {
		 if(con!=null)
	     try {
	         con.close();
	     } catch (SQLException e) {
	         e.printStackTrace();
	     }       
	 }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

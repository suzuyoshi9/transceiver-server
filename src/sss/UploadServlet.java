package sss;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet("/UploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DatabaseClass db = null;
		String userid=request.getParameter("userId");
		String registid=request.getParameter("regId");
		String Filename = null;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletContext servletContext = this.getServletConfig().getServletContext();
		factory.setRepository((File)servletContext.getAttribute("javax.servlet.context.tempdir"));
		String uppath = servletContext.getRealPath("/")+"files/";
		byte[] buff = new byte[1024];
		int size = 0;
		
		Part filePart = request.getPart("file");
		InputStream in = filePart.getInputStream();
		Filename = this.getRandomString(10)+".wav";
		File f = new File(uppath+Filename);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		while((size=in.read(buff))>0){
			out.write(buff,0,size);
		}
		out.close();
		in.close();
		
		try {
			db = new DatabaseClass();
			db.addFile(userid,registid,Filename);
			GCMServlet.sendPathInfo(new String(userid+","+Filename),registid);
			db.close();
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		response.getWriter().print("upload ok\n");
		response.getWriter().flush();
	}
	
	private String getFilename(Part part){
		for(String cd : part.getHeader("Content-Disposition").split(";")){
			if(cd.trim().startsWith("filename")){
				return cd.substring(cd.indexOf('=')+1).trim().replace("\"", "");
			}
		}
		return null;
	}
	
	private String getRandomString(int cnt) {
		  final String chars ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		  Random rnd=new Random();
		  StringBuffer buf = new StringBuffer();
		  for(int i=0;i<cnt;i++){
		   int val=rnd.nextInt(chars.length());
		   buf.append(chars.charAt(val));
		  }
		  return buf.toString();
	}
}

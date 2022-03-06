package http;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.google.common.io.FileBackedOutputStream;

import webserver.HttpRequest;

public class HttpRequestTest {
	private String testDirectory = "./src/test/resources";
	
	@Test
	public void request_GET() throws Exception {
		InputStream in = new FileInputStream(new File(testDirectory +"HTTP_GET.txt"));
		// FileInputStream in = new FileInputStream(new File(testDirectory +"HTTP_GET.txt"));
		// 위와 같이 쓸 수 있지만 그러면 InputStrema의 유용한 
		HttpRequest request = new HttpRequest(in);
		
		assertEquals("GET", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userid"));
	}
	
	@Test
	public void request_POST() throws Exception {
		InputStream in = new FileInputStream(new File(testDirectory +"HTTP_POST.txt"));
		HttpRequest request = new HttpRequest(in);
		
		assertEquals("POST", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userid"));
	}
	
	
}

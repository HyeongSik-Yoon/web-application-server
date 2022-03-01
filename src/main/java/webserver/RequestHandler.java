package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        // socket 기본적으로 stream을 통해서 주고 받는다. Webserver에 socket이라는 구멍을 뚫는데, 구멍에서 주고 받는게 stream
        // stream의 속성에 직렬화가 존재함, 직렬화는 개발자가 어떤 변수(객체형, 기본자료형)로 값을 만들던, 정해진 방식(byte)로만
        // 주고 받을 수있게함.
        // InputStream을 통해 response을 webserver로 보냄, 그에대한 request로 OutputStream으로  String형을 byte화 한 값을 보내줌.
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	String line = br.readLine();// 순차적으로 읽고, 이전내용을 안가지고 있는다.
        	log.debug("request line : {}", line);
        	
        	if(line == null) {
        		return;
        	}
        	
        	String[] tokens = line.split(" ");
        	boolean logined = false;
        	int contentLength = 0;
        	while(!line.equals("")) {
        		log.debug("header : {}", line);
        		line = br.readLine();
        		if(line.contains("Cookie")) {
        			logined = isLogin(line);
        		}
        		if(line.contains("Content-Length")) {
        			contentLength = getContentLength(line);  // 회원가입 값의 길이
        		}
        	}
        	String url = tokens[1];  // index.html
        	if(("/userc v/create".equals(url))) {
        		String body = IOUtils.readData(br, contentLength);  // ?userId=abc&password=null&name=yoon&email=1234%40naver.com
				 Map<String, String> params =
						 HttpRequestUtils.parseQueryString(body);  // userId=abc 
				 User user = new User(params.get("userId"), params.get("password"), 
						              params.get("name"), params.get("email"));
				 DataBase.addUser(user);
				 log.debug("User : {}", user);
				 DataOutputStream dos = new DataOutputStream(out);
				 response302Header(dos, "/index.html");  // 회원가입끝
        	}else if ("/user/login".equals(url)) {
				String body = IOUtils.readData(br, contentLength);
				Map<String, String> params =
						HttpRequestUtils.parseQueryString(body);
				User user = DataBase.findUserById(params.get("userId"));
				if(user == null) {
					responseResource(out, "/user/login_failed.html");
					return;
				}
				if(user.getPassword().equals(params.get("password"))) {
					DataOutputStream dos = new DataOutputStream(out);
					response302LoginSuccessHeader(dos);
				}  else {
					responseResource(out, "/user/login_failed.html");
				}
			}else if ("/user/list".equals(url)) {
				if(!logined) {
					responseResource(out, "/user/login.html");
					return;
				}
				Collection<User> users = DataBase.findAll();
				StringBuilder sb = new StringBuilder();
				sb.append("<table border='1'>");
				for (User user : users) {
					sb.append("<tr>");
					sb.append("<td>"+ user.getUserId() +"</td>");
					sb.append("<td>"+ user.getName() +"</td>");
					sb.append("<td>"+ user.getEmail() +"</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				byte [] body = sb.toString().getBytes();
				DataOutputStream dos = new DataOutputStream(out);
				response200Header(dos, body.length);
				responseBody(dos, body);
			} else if (url.endsWith(".css")) {
				DataOutputStream dos = new DataOutputStream(out);
				byte [] body = Files.readAllBytes(new File("./webapp" + url).toPath());
				response200CssHeader(dos, body.length);
				responseBody(dos, body);
			}
        	
        	else {
				responseResource(out, url);
			}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
   

	private int getContentLength(String line) {
    	String[] headerTokens = line.split(":");  // : 을 구분자로 Content-Length: 57 -> "Content-Length", " 57" 구분
    	return Integer.parseInt(headerTokens[1].trim());  // trim으로 공백제거 "57" -> parserint 57
    }
    
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();   // 초기화
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String url) {
    	try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: " + url + " \r\n ");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
    }
    
    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);  // dos 보내는 전용으로 객체를 만들어둠
        //byte[] body = "Hello World 형식22222".getBytes();
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    
    private void response302LoginSuccessHeader(DataOutputStream dos) {  // console 창에서 보는게 아닌 화면으로 던짐
    	try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
    }
    
    private boolean isLogin(String line) {
    	String [] headerTokens = line.split(":");
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
    	String value = cookies.get("logined");
    	if (value == null) {
    		return false;
    	}
    	return Boolean.parseBoolean(value);
    }
    
    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}
}

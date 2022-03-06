package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	
	private Map<String, String> headers = new HashMap<String, String>();  // 다형성, Map의 특성도 쓰면서 Hash맵도 사용
	private Map<String, String> params = new HashMap<String, String>();
	private RequestLine requestLine;
	
	public HttpRequest(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			// new InputStreamReader(in, "UTF-8") 처럼 쓰면 주소값은 생성되겠지만, 변수에 담지 하지 않았으므로
			// 우리가 재사용할 순 없다. 그냥 쓰는 건데 그냥 매개변수처럼 만들어쓰는거..
			
			String line = br.readLine();
			// Http_GET.txt를 예를 들자면
			// br.readLine 1번째 : GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1
			if(line == null) {
				return;
			}
			
			processRequestLine(line);  // 첫번째 줄처리완료
			
			line = br.readLine();  // 첫번째 줄은 저장(서버내), 2번째 줄처리
			while (!line.equals("")) {
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();
				
			}
			
			if("POST".equals(method)) {
				String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				params = HttpRequestUtils.parseQueryString(body);
			}
		} catch (IOException io) {
			log.error(io.getMessage());
		}
	}
	
	private void processRequestLine(String requestLine) {
		log.debug("request line : {}", requestLine);
		String[] tokens = requestLine.split(" ");
		method = tokens[0];
		
		if("POST".equals(method)) {
			path = tokens[1];
			return;
		}
		
		int index = tokens[1].indexOf("?");  // 12

		// indexof  내가 지정한 문자 조건으로 입맛에 맞게 위치를 지정할 수 있음, 실제 자르는 것은 substring
		if(index == -1) {  // indexOf 메서드에서 특정 문자열을 찾는데, 특정문자열이 없다는 것, 즉 여기선 물음표가 없다는것
			path = tokens[1];  // /user/create
		} else {
			path = tokens[1].substring(0, index);  // /user/create? ?을 기준으로, 처음인 /부터 마지막인 ?이전까지 잘라서 경로를 만들어준다.
			params = HttpRequestUtils.parseQueryString(tokens[1].substring(index +1));  // ? 이후, userid, u이후부터 값을 잘라서 Map으로 반환하는데, 
			// 파라미터값, userId=javajigi ---> <userid, javajigi>

		}
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getParams(String name) {
		return headers.get(name);
	}
	

}

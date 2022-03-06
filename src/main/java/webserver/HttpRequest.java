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
			
			requestLine  = new RequestLine(line);  // 첫번째 줄처리완료
			
			line = br.readLine();  // 첫번째 줄은 저장(서버내), 2번째 줄처리
			while (!line.equals("") ) { //
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();
				
			}
			
			if("POST".equals(getMethod().toString())) {
				String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				params = HttpRequestUtils.parseQueryString(body);
			} else {
				params = requestLine.getParams();
			}
		} catch (IOException io) {
			log.error(io.getMessage());
		}
	}
	
	

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getParams(String name) {
		return params.get(name);
	}
	

}

package webserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestLine {
	private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
	
	private String path;
	private Map<String, String> params = new HashMap<String, String>();
	private HttpMethod method;  // 상수관리 클래스
	
	public RequestLine(String requestLine) {
		log.debug("request line : {}", requestLine);
		String[] tokens = requestLine.split(" ");
		if(tokens.length != 3) {
			throw new IllegalArgumentException(requestLine + "이 형식에 맞지 않습니다.");
		}
		method = HttpMethod.valueOf(tokens[0]);
		if(method.isPost()) {
			path = tokens[1];
			return;
		}
		
		int index = tokens[1].indexOf("?");  // 12
		// indexof  내가 지정한 문자 조건으로 입맛에 맞게 위치를 지정할 수 있음, 실제 자르는 것은 substring
		if (index == -1) {  // indexOf 메서드에서 특정 문자열을 찾는데, 특정문자열이 없다는 것, 즉 여기선 물음표가 없다는것
			path = tokens[1]; // /user/create
		} else {
			path = tokens[1].substring(0, index);  // /user/create? ?을 기준으로, 처음인 /부터 마지막인 ?이전까지 잘라서 경로를 만들어준다.
			params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));  // ? 이후, userid, u이후부터 값을 잘라서 Map으로 반환하는데,
			// 파라미터값, userId=javajigi ---> <userid, javajigi>
		}
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	
	public String getPath() {
		return path;
	}
	
	public Map<String, String> getParams(){
		return params;
	}
}

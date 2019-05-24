package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class HttpRequest {

	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private Map<String, String> header = new HashMap<>();
	private Map<String, String> parameter = new HashMap<>();
	private RequestLine requestLine;

	public HttpRequest(InputStream in) {
		try {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = buffer.readLine();

			if (line == null) {
				return;
			}

			requestLine = new RequestLine(line);

			line = buffer.readLine();
			while (!line.equals("")) {
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				header.put(tokens[0].trim(), tokens[1].trim());

				line = buffer.readLine();
			}

			if ("POST".equals(getMethod())) {
				String body = IOUtils.readData(buffer, Integer.parseInt(header.get("Content-Length")));
				parameter = HttpRequestUtils.parseQueryString(body);
			} else {
				parameter = requestLine.getParameter();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}

	public String getHeader(String name) {
		return header.get(name);
	}

	public String getParameter(String name) {
		return parameter.get(name);
	}
}

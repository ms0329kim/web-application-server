package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class HttpRequest {

	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private String method;
	private String path;
	private Map<String, String> header;
	private Map<String, String> parameter;

	public HttpRequest(InputStream in) {
		try {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = buffer.readLine();

			String[] tokens = line.split(" ");
			method = tokens[0];

			if ("GET".equals(method)) {
				int index = tokens[1].indexOf("?");
				path = tokens[1].substring(0, index);
				String queryString = tokens[1].substring(index + 1);
				parameter = HttpRequestUtils.parseQueryString(queryString);
			} else if ("POST".equals(method)) {
				path = tokens[1];
			}

			header = new HashMap<>();

			while(!line.equals("")) {
				line = buffer.readLine();

				if (line == null || line.equals("")) {
					break;
				}

				HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
				header.put(pair.getKey(), pair.getValue());
			}

			if (header.get("Content-Length") != null) {
				String body = IOUtils.readData(buffer, Integer.parseInt(header.get("Content-Length")));
				parameter = HttpRequestUtils.parseQueryString(body);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getMethod() {
		return this.method;
	}

	public String getPath() {
		return this.path;
	}

	public String getHeader(String header) {
		return this.header.get(header);
	}

	public String getParameter(String parameter) {
		return this.parameter.get(parameter);
	}
}

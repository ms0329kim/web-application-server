package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			String url = null, requestPath = null, type = null;
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = buffer.readLine();
			int count = 0, contentLength = 0;

			while (!"".equals(line)) {
				if (line == null) {
					return;
				}

				String[] tokens = line.split(" ");
				if (count++ == 0) {
					type = tokens[0];
					url = tokens[1];
				}

				if (url.contains("html")) {
					requestPath = url;
				} else {
					if ("/".equals(url)) {
						requestPath = "/index.html";
					} else if (url.contains("/user/create")) {
						if ("GET".equals(type)) {
							int index = url.indexOf("?");
							requestPath = "/user/form.html";

							if (index > -1) {
								String params = url.substring(index+1);
								createUser(params);
								requestPath = "/index.html";
							}
						} else if ("POST".equals(type)) {
							if (tokens[0].contains("Content-Length")) {
								contentLength = Integer.parseInt(tokens[1]);
							}
						}
					}
				}

				line = buffer.readLine();
			}

			if (contentLength > 0) {
				createUser(IOUtils.readData(buffer, contentLength));
			}

			DataOutputStream dos = new DataOutputStream(out);
			byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
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
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void createUser(String params) {
		Map<String, String> map = HttpRequestUtils.parseQueryString(params);
		User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));
		System.out.println(user.toString());
	}
}

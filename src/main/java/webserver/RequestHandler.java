package webserver;

import db.DataBase;
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
			DataOutputStream dos = new DataOutputStream(out);
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
								response302Header(dos);
							}
						}
					}
				}

				if (tokens[0].contains("Content-Length")) {
					contentLength = Integer.parseInt(tokens[1]);
				}

				line = buffer.readLine();
			}

			String loginYn = null;
			if (contentLength > 0) {

				if (url.contains("/user/create")) {
					createUser(IOUtils.readData(buffer, contentLength));
					response302Header(dos);
				} else if (url.contains("/user/login")) {
					User user = findUser(IOUtils.readData(buffer, contentLength));

					if (user == null) {
						requestPath = "/login_failed.html";
						loginYn = "logined=false";
					} else {
						requestPath = "/index.html";
						loginYn = "logined=true";
					}
				}

			}

			byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
			response200Header(dos, body.length, loginYn);
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

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cookieYn) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");

			if (cookieYn != null) {
				dos.writeBytes("Cookie: " + cookieYn + "\r\n");
			}

			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html");
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

	private User findUser(String params) {
		Map<String, String> map = HttpRequestUtils.parseQueryString(params);
		return DataBase.findUserById(map.get("userId"));
	}

	private void createUser(String params) {
		Map<String, String> map = HttpRequestUtils.parseQueryString(params);
		User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));
		DataBase.addUser(user);
	}
}

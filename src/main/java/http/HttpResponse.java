package http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class HttpResponse {

	private DataOutputStream dos;

	public HttpResponse(OutputStream out) {
		this.dos = new DataOutputStream(out);
	}

	public void addHeader(String type, String content) {
		try {
			dos.writeBytes(type + ": " + content + " \r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void forward(String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(body.length);
		responseBody(body);
	}

	public void response200Header(int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRedirect(String location) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: " + location + " \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void processHeaders() {

	}

}

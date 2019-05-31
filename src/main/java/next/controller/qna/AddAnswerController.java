package next.controller.qna;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.mvc.Controller;
import next.dao.AnswerDao;
import next.model.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class AddAnswerController implements Controller {
	private static final Logger log = LoggerFactory.getLogger(AddAnswerController.class);

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Answer answer = new Answer(request.getParameter("writer"),
				request.getParameter("contents"),
				Long.parseLong(request.getParameter("questionId")));
		log.debug("answer : {}", answer);
		AnswerDao answerDao = new AnswerDao();
		Answer savedAnswer = answerDao.insert(answer);

		ObjectMapper mapper = new ObjectMapper();
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print(mapper.writeValueAsString(savedAnswer));

		return "/qna/show.jsp";
	}
}

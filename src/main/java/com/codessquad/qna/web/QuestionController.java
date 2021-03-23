package com.codessquad.qna.web;

import com.codessquad.qna.domain.Question;
import com.codessquad.qna.repository.QuestionRepository;
import com.codessquad.qna.domain.User;
import com.codessquad.qna.exception.AccessDeniedException;
import com.codessquad.qna.exception.NoQuestionException;
import com.codessquad.qna.exception.NoUserException;
import com.codessquad.qna.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/question")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/form")
    public String form(HttpSession session, Model model) {
        try {
            hasPermission(session);
            return "qna/form";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "/user/login";
        }
    }

    @PostMapping
    public String createNewQuestion(Question question, HttpSession session) {
        questionService.save(HttpSessionUtils.getUserFromSession(session), question);
        return "redirect:/";
    }

    @GetMapping
    public String showQuestionList(Model model) {
        model.addAttribute("question", questionService.getQuestionList());
        return "index";
    }

    @GetMapping("/{id}")
    public String showOneQuestion(@PathVariable long id, Model model) {
        model.addAttribute("question", questionService.getQuestionById(id));
        return "qna/show";
    }

    @GetMapping("{id}/form")
    public String editQuestion(@PathVariable long id, Model model, HttpSession session) {
        try {
            Question question = questionService.getQuestionById(id);
            hasPermission(question, session);
            model.addAttribute("question", question);
            return "qna/updateForm";
        } catch (IllegalStateException | AccessDeniedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "/user/login";
        }
    }

    @PostMapping("{id}")
    public String update(@PathVariable long id, Question updateQuestion) {
        questionService.updateQuestion(id, updateQuestion);
        return "redirect:/";
    }

    @DeleteMapping("{id}")
    public String delete(@PathVariable long id, Model model, HttpSession session) {
        try {
            Question question = questionService.getQuestionById(id);
            hasPermission(question, session);
            questionService.delete(question);

            return "redirect:/";
        } catch (IllegalStateException | AccessDeniedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "/user/login";
        }
    }

    private void hasPermission(Question question, HttpSession session) {
        if (!HttpSessionUtils.isLoginUser(session)) {
            throw new IllegalStateException("로그인을 먼저 진행해주세요.");
        }

        User sessionUser = HttpSessionUtils.getUserFromSession(session);

        if (!question.userConfirmation(sessionUser)) {
            throw new AccessDeniedException();
        }
    }

    private void hasPermission(HttpSession session) {
        if (!HttpSessionUtils.isLoginUser(session)) {
            throw new IllegalStateException("로그인을 먼저 진행해주세요.");
        }
    }
}

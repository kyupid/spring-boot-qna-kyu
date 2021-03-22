package com.codessquad.qna.web;

import com.codessquad.qna.service.UserService;
import com.codessquad.qna.domain.User;
import com.codessquad.qna.exception.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public String create(User user) {
        if (userService.checkEmpty(user)) {
            return "user/form";
        }
        userService.save(user);
        return "redirect:/users";
    }

    @GetMapping
    public String getUserList(Model model) {
        model.addAttribute("users", userService.getUserList());
        return "user/list";
    }

    @GetMapping("/{id}")
    public String userProfile(@PathVariable long id, Model model, HttpSession session) {
        if (!HttpSessionUtils.isLoginUser(session)) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", userService.getUserById(id));
        return "user/profile";
    }

    @GetMapping("/{id}/form")
    public String editUserInfo(@PathVariable long id, Model model, HttpSession session) {
        if (!HttpSessionUtils.isLoginUser(session)) {
            return "redirect:/users/login";
        }

        User sessionUser = HttpSessionUtils.getUserFromSession(session);

        if (!sessionUser.userIdConfirmation(id)) {
            throw new AccessDeniedException();
        }

        model.addAttribute("user", sessionUser);

        return "user/updateForm";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id, User updateUser, String newPassword) {
        userService.updateUser(id, updateUser, newPassword);
        return "redirect:/users";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    @PostMapping("/login")
    public String login(String userId, String password, HttpSession session) {
        if (HttpSessionUtils.isLoginUser(session)) {
            return "redirect:/users/login";
        }
        User user = userService.getUserByUserId(userId);
        if (!user.checkPassword(password)) {
            return "redirect:/users/login";
        }
        session.setAttribute(HttpSessionUtils.USER_SESSION_KEY, user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(HttpSessionUtils.USER_SESSION_KEY);
        return "redirect:/";
    }

}

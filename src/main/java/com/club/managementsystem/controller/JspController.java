package com.club.managementsystem.controller;

import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/jsp")
public class JspController {

    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;

    public JspController(ClubRepository clubRepository, StudentRepository studentRepository) {
        this.clubRepository = clubRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/clubs")
    public String clubsList(Model model) {
        List<Club> clubs = clubRepository.findAll();
        model.addAttribute("title", "社团列表");
        model.addAttribute("clubCount", clubs.size());
        model.addAttribute("clubs", clubs);
        model.addAttribute("isAdmin", true);
        return "clubs";
    }

    @GetMapping("/student/{studentId}")
    public String studentDetail(@PathVariable String studentId, Model model) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isPresent()) {
            model.addAttribute("student", studentOpt.get());
            model.addAttribute("found", true);
        } else {
            model.addAttribute("found", false);
        }
        return "student-detail";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long studentCount = studentRepository.count();
        long clubCount = clubRepository.count();
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("clubCount", clubCount);
        model.addAttribute("title", "系统统计面板");
        return "dashboard";
    }
}

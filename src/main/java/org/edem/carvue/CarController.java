package org.edem.carvue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload"; // This maps to upload.html
    }

    @GetMapping
    public String showCarGallery(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Car> carPage = carService.getCars(PageRequest.of(page, 6));
        model.addAttribute("cars", carPage);
        model.addAttribute("currentPage", page);
        return "gallery"; // This maps to gallery.html
    }
}

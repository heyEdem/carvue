package org.edem.carvue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;


    @GetMapping
    public String showCarGallery(Model model,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "12") int size) {
        Map<String, Object> result = carService.getImages(page, size);


        model.addAttribute("images", result.get("images"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("hasNextPage", result.get("hasNextPage"));
        model.addAttribute("pageSize", size);

        return "gallery";
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("images") MultipartFile file,
                             @RequestParam("description") String description,
                             RedirectAttributes redirectAttributes) {
        try {
            String response = carService.uploadImage(file, description);
            if (response.equals("success")) {
                redirectAttributes.addFlashAttribute("message", "Successfully uploaded");
            } else if (response.equals("empty")) {
                redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
                return "redirect:/upload";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to upload file: " + e.getMessage());
            return "redirect:/upload";
        }

        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteImage(@RequestParam("imageKey") String imageKey, RedirectAttributes redirectAttributes) {
        String sanitizedKey = imageKey.split("\\?")[0];

        String decodedKey = URLDecoder.decode(sanitizedKey, StandardCharsets.UTF_8);

        boolean deleted = carService.deleteImage(decodedKey);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Image deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete image.");
        }

        return "redirect:/";
    }
}

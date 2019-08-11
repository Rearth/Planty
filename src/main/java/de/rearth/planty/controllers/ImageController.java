package de.rearth.planty.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//used to provide the frontend with the pictures of the plants, which are stored on the server's filesystem
@Controller
public class ImageController {

    @RequestMapping(value = "/getImage/{imageId}")
    @ResponseBody
    public byte[] getImage(@PathVariable String imageId, HttpServletRequest request) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("upload-dir/" + imageId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
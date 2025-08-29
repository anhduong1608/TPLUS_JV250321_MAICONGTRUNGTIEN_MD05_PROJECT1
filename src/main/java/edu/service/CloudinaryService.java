package edu.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dfnkuwnpk",
            "api_key", "346783481985569",
            "api_secret", "wwTg04UjKWACWqnBTiss3WKB1FU",
            "secure", true
    ));


    public String uploadFile(MultipartFile multipartFile) throws IOException {
        File temp = File.createTempFile("upload", multipartFile.getOriginalFilename());
        multipartFile.transferTo(temp);

        Map uploadResult = cloudinary.uploader().upload(temp, ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

}

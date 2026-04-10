package com.example.socialnetworkingbackend.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Objects;

public class FileUtil {

    private static final Path TEMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"));

  public static String saveFile(String newFileName, String uploadPath, MultipartFile multipartFile) throws IOException {
      Path path = TEMP_PATH.resolve(Paths.get(uploadPath));
      if (!Files.exists(path)) {
          Files.createDirectories(path);
      }
      String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
      String fileType = fileName.substring(fileName.lastIndexOf("."));
      String newFile = newFileName + fileType;
      Path filePath;
      try (InputStream inputStream = multipartFile.getInputStream()) {
          filePath = path.resolve(newFile);
          Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ioe) {
          throw new IOException("Could not save file: " + fileName);
      }
      return filePath.toAbsolutePath().toString();
  }

}

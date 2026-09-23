package com.yihong.growth.controller.admin;

import com.yihong.growth.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    /** 按扩展名校验文件魔数，避免信任客户端声明的 Content-Type */
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
        ".jpg",  new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        ".jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        ".png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        ".gif",  new byte[]{0x47, 0x49, 0x46, 0x38},
        ".webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件为空");
        }

        // 限制文件大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过5MB");
        }

        // 1. 校验扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error(400, "不支持的文件类型，仅允许: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 2. 校验文件魔数（防止伪造扩展名或 Content-Type）
        byte[] expectedMagic = MAGIC_BYTES.get(ext);
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < expectedMagic.length || !startsWith(header, expectedMagic)) {
                return Result.error(400, "文件内容与扩展名不匹配");
            }
            if (".webp".equals(ext) && (read < 12
                    || header[8] != 0x57 || header[9] != 0x45
                    || header[10] != 0x42 || header[11] != 0x50)) {
                return Result.error(400, "文件内容与扩展名不匹配");
            }
        } catch (IOException e) {
            return Result.error(500, "文件读取失败");
        }

        try {
            // 按日期分目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path uploadPath = Paths.get(UPLOAD_DIR, dateDir);
            Files.createDirectories(uploadPath);

            // 使用完整 UUID 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            // 返回可访问的 URL
            String url = "/uploads/" + dateDir + "/" + fileName;
            return Result.ok(Map.of("url", url));
        } catch (IOException e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    private boolean startsWith(byte[] actual, byte[] expected) {
        if (actual.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) return false;
        }
        return true;
    }
}

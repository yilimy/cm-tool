package com.gomain.cm.tool.tika;

import com.gomain.cm.tool.pojo.Result;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件类型控制器
 * @author caimeng
 * @date 2024/3/11 14:11
 */
@Slf4j
@RestController
public class FileTypeCheckController {

    /**
     * 文件类型检查
     * Tika还是太重了
     * @param file 上传的文件对象
     * @return 文件类型
     */
    @SneakyThrows
    @RequestMapping("/file/typeCheck")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("检查文件类型, fileName={}", file.getOriginalFilename());
        Tika tika = new Tika();
        String detect = tika.detect(file.getBytes());
        return Result.success(detect);
    }

    public static void main(String[] args) {
        String pdfPath = "E:\\tmp\\pdf\\blank_gd_signed.pdf";
        Tika tika = new Tika();
        String detectPdf = tika.detect(pdfPath);
        System.out.println("detectPdf = " + detectPdf);
        assert detectPdf.equals("application/pdf");

        String ofdPath = "E:\\tmp\\ofd\\blank_15.ofd";
        String detectOfd = tika.detect(ofdPath);
        System.out.println("detectOfd = " + detectOfd);
        assert detectOfd.equals("application/octet-stream");
    }
}

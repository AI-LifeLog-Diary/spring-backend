package com.lifelog.diary.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.lifelog.diary.common.file.FileConstant;
import com.lifelog.diary.common.file.FileUtil;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
    private final AmazonS3 amazonS3;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;


    public String uploadImageToS3(MultipartFile image) {
        try {
            String originalFileName = image.getOriginalFilename();
            String mimeType = image.getContentType();


            //최대용량 체크
            if (image.getSize() > FileConstant.MAX_IMAGE_SIZE) {
                throw new GeneralException(Code.FILE_SIZE_EXCEEDED, "5MB 이하 파일만 업로드 할 수 있습니다.");
            }

            //MIMETYPE 체크
            if (!FileUtil.isImageFile(mimeType)) {
                throw new GeneralException(Code.INVALID_FILE_TYPE, "이미지 파일(jpg, jpeg, png)만 업로드할 수 있습니다.");
            }

            String fileName = "profile-images/" + UUID.randomUUID() + "-" + originalFileName;

            try {
                amazonS3.putObject(bucketName, fileName, image.getInputStream(), null);
            } catch (IOException e) {
                throw new GeneralException(Code.FILE_UPLOAD_ERROR, "S3에 파일 업로드 도중 오류 발생");
            }

            // 업로드된 파일의 S3 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(Code.FILE_UPLOAD_ERROR, "파일을 업로드하는 도중 알 수 없는 오류 발생");

        }
    }
}
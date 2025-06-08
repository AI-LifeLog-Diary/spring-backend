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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
    private final AmazonS3 amazonS3;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
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

    public String generatePresignedUrlFromFullUrl(String fullUrl) {
        try {
            URL url = new URL(fullUrl);
            // 버킷 도메인을 제외한 path (앞에 '/' 제거)
            String path = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
            String objectKey = path.startsWith("/") ? path.substring(1) : path;
            System.out.println(objectKey);
            return generatePresignedUrl(objectKey);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("잘못된 S3 URL 형식입니다.", e);
        }
    }

    public String generatePresignedUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();
        try {
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Presigned URL 생성에 실패하였습니다.", e);
        }
    }
}
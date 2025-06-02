package com.lifelog.diary.common.file;

public class FileUtil {

    // 이미지 MIME 타입을 체크하는 메서드
    public static boolean isImageFile(String mimeType) {
        return (mimeType != null) && (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg") || mimeType.equals("image/png") );
    }
}

package com.lifelog.diary.common.file;

public class FileUtil {

    // 이미지 MIME 타입을 체크하는 메서드
    public static boolean isImageFile(String mimeType) {
        return (mimeType != null) && (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg") || mimeType.equals("image/png") );
    }

    public static String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }

        return switch (mimeType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            default -> "";
        };
    }
}

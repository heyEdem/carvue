package org.edem.carvue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository repository;
    private final   S3Client s3Client;
    private final S3Presigner s3Presigner;

    private String BUCKET_NAME = "imagery-app";

    // Map to store pagination state
    private Map<Integer, String> pageTokenMap = new HashMap<>();

//    public Map<String, Object> getImages(int page, int size) {
//        Map<String, Object> result = new HashMap<>();
//        List<String> imageUrls = new ArrayList<>();
//
//        String continuationToken = page > 0 ? pageTokenMap.get(page - 1) : null;
//
//        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
//                .bucket(BUCKET_NAME)
//                .maxKeys(size);
//
//        if (continuationToken != null && !continuationToken.isEmpty()) {
//            requestBuilder.continuationToken(continuationToken);
//        }
//
//        ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
//
//        List<S3Object> objects = response.contents();
//        imageUrls = objects.stream()
//                .filter(s3Object -> isImage(s3Object.key()))
//                .map(obj -> generatePresignedUrl(obj.key()))
//                .collect(Collectors.toList());
//
//        if (response.isTruncated()) {
//            pageTokenMap.put(page, response.nextContinuationToken());
//            result.put("hasNextPage", true);
//        } else {
//            result.put("hasNextPage", false);
//        }
//
//        long totalCount = countTotalImages();
//        int totalPages = (int) Math.ceil((double) totalCount / size);
//
//        result.put("images", imageUrls);
//        result.put("totalPages", Math.max(1, totalPages));
//        result.put("currentPage", page);
//
//        return result;
//    }

    public Map<String, Object> getImages(int page, int size) {
        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<Car> carPage = repository.findAll(pageable);

        List<Map<String, String>> imageData = carPage.getContent().stream()
                .map(car -> {
                    Map<String, String> imageMap = new HashMap<>();
                    imageMap.put("key", car.getS3url());
                    imageMap.put("value", car.getDescription() != null ? car.getDescription() : "No description available");
                    return imageMap;
                })
                .collect(Collectors.toList());

        result.put("images", imageData);
        result.put("totalPages", carPage.getTotalPages());
        result.put("currentPage", carPage.getNumber());
        result.put("hasNextPage", carPage.hasNext());

        return result;
    }



//    private long countTotalImages() {
//        long count = 0;
//        String continuationToken = null;
//
//        do {
//            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
//                    .bucket(BUCKET_NAME);
//
//            if (continuationToken != null) {
//                requestBuilder.continuationToken(continuationToken);
//            }
//
//            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
//
//            count += response.contents().stream()
//                    .filter(s3Object -> isImage(s3Object.key()))
//                    .count();
//
//            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
//        } while (continuationToken != null);
//
//        return count;
//    }

//    public String generatePresignedUrl(String objectKey) {
//        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
//                .signatureDuration(Duration.ofHours(1))
//                .getObjectRequest(GetObjectRequest.builder()
//                        .bucket(BUCKET_NAME)
//                        .key(objectKey)
//                        .build())
//                .build();
//
//        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
//        return presignedGetObjectRequest.url().toString();
//    }
//
//    private boolean isImage(String key) {
//        return key.toLowerCase().endsWith(".jpg") ||
//                key.toLowerCase().endsWith(".jpeg") ||
//                key.toLowerCase().endsWith(".png");
//    }


    public String uploadImage(MultipartFile file, String description) throws IOException {

        if (file.isEmpty()) {
            return "empty";
        }

      if (file.getSize() > 100000) {
          return "size";
        }

            String key = "image_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            String s3Url = "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + key;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Save to the database
            Car car = Car.builder()
                    .name(file.getOriginalFilename())
                    .description(description)
                    .s3url(s3Url)
                    .build();

            repository.save(car);

        return "success";
    }



    public boolean deleteImage(String imageKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(imageKey)
                    .build();
            Car carOptional = repository.findByS3urlEndingWith(imageKey).orElseThrow( ()-> new RuntimeException("Image not found"));
            repository.delete(carOptional);
            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (S3Exception e) {
            e.printStackTrace();
            System.out.println("false");
            return false;
        }
    }


}

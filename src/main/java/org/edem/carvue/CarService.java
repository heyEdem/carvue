package org.edem.carvue;
import org.edem.carvue.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository repository;
    private final S3Client s3Client;


    private final String bucketName = "imagery-app";

    public Car uploadCar(MultipartFile file, String name, String description) throws Exception {
        // Generate unique filename
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Upload to S3
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        // S3 URL
        String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;

        // Save metadata in DB
        Car car = Car.builder()
                .name(name)
                .description(description)
                .s3url(s3Url)
                .build();

        return repository.save(car);
    }

    public void deleteCar(Long carId) {
        // Find car by ID
        Car car = repository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        // Extract S3 key from URL (assuming URL format: https://bucket-name.s3.amazonaws.com/key)
        String fileKey = car.getS3url().substring(car.getS3url().lastIndexOf("/") + 1);
        System.out.println(fileKey);

        // Delete from S3
        s3Client.deleteObject(d -> d.bucket(bucketName).key(fileKey));

        // Delete from database
        repository.delete(car);
    }


    public Page<Car> getCars (Pageable pageable){
//        Pageable pageable = PageRequest.of(page,size);
        return repository.findAll(pageable);
    }
}

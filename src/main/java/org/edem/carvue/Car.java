package org.edem.carvue;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(nullable = false, unique = true)
    private String s3url;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void onCreate(){
        this.uploadedAt = LocalDateTime.now();
    }

}

package com.siriusxi.ms.store.revs.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// import javax.persistence.*;
// import javax.validation.constraints.NotBlank;
// import javax.validation.constraints.Size;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import static jakarta.persistence.GenerationType.IDENTITY;

// import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "review", indexes = {
        @Index(name = "review_unique_idx", unique = true, columnList = "productId, reviewId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    @Version
    private int version;

    // @NonNull
    private int productId;
    // @NonNull
    private int reviewId;
    @NonNull
    @NotBlank
    @Size(min = 6, max = 50)
    private String author;
    @NonNull
    @NotBlank
    private String subject;
    @NonNull
    @NotBlank
    private String content;

    public ReviewEntity(int i, int i1, String s, String s1, String c) {
    }
}

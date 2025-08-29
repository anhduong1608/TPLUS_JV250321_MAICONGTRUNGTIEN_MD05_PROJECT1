package edu.model.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO5TOP {
    private Long id;
    private String name;
    private String image;
    private Long studentCount;
}

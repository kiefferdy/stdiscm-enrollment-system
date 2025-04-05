package com.stdiscm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto implements Serializable {
    
    private Long id;
    
    @NotBlank
    private String courseCode;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
    
    @NotNull
    @Min(1)
    private Integer credits;
    
    @NotNull
    @Min(1)
    private Integer capacity;
    
    private Integer enrolled;
    
    private Boolean isOpen;
    
    private String facultyName;
    
    private Long facultyId;
    
    private Boolean hasAvailableSlots;
}

package com.stdiscm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmissionDto implements Serializable {
    
    @NotNull
    private Long enrollmentId;
    
    @NotBlank
    private String grade;
}

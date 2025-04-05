package com.stdiscm.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String courseCode;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Integer credits;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(nullable = false)
    private Integer enrolled;
    
    @Column(nullable = false)
    private Boolean isOpen;
    
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private User faculty;
    
    public boolean hasAvailableSlots() {
        return this.isOpen && this.enrolled < this.capacity;
    }
}

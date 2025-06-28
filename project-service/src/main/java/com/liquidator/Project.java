package com.liquidator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @Id
    private String requestId;
    @Column(nullable = false, length = 2048)
    private String downloadLink;
}

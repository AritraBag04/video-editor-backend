package com.liquidator;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {
    // This interface can be extended with custom query methods if needed
}

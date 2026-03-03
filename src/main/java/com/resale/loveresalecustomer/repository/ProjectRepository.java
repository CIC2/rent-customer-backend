package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
}

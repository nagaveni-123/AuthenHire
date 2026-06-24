package com.authenhire.authenhire.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authenhire.authenhire.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJob_Company_Id(Long companyId);
}

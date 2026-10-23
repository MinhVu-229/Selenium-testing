package com.koreanwithme.backend.repository;

import com.koreanwithme.backend.entity.User;
import com.koreanwithme.backend.entity.UserExamAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExamAccessRepository extends JpaRepository<UserExamAccess, Long> {
    boolean existsByUserAndExamId(User user, Integer examId);
}
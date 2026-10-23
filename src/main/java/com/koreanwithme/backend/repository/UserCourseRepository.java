package com.koreanwithme.backend.repository;

import com.koreanwithme.backend.entity.User;
import com.koreanwithme.backend.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    boolean existsByUserAndCourseId(User user, Integer courseId);
}
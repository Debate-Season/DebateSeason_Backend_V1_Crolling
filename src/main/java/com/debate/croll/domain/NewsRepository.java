package com.debate.croll.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debate.croll.domain.entity.News;

@Repository
public interface NewsRepository extends JpaRepository<News,Long> {
}

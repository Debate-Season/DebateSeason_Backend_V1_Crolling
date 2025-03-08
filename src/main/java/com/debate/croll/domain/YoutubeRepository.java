package com.debate.croll.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debate.croll.domain.entity.News;
import com.debate.croll.domain.entity.Youtube;

@Repository
public interface YoutubeRepository extends JpaRepository<Youtube,Long> {
}

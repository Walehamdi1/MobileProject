package com.work.truetech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.work.truetech.entity.Option;

import java.util.List;

@Repository
public interface OptionRepository  extends JpaRepository<Option, Long>  {
    List<Option> findByModelId(Long modelId);
    Option findByTitle(String title);
}

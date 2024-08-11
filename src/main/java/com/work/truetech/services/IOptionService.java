package com.work.truetech.services;

import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Option;

import java.io.IOException;
import java.util.List;

public interface IOptionService {
    Option createOption(Option option,Long modelId, MultipartFile file) throws IOException;
    List<Option> retrieveOptions();
    List<Option> retrieveOptionByModel(Long modelId);
    Option getOptionById(Long optionId);
    Option updateOption(Long optionId, Option updatedOption, MultipartFile file) throws IOException;
    void deleteOption(Long id);
}

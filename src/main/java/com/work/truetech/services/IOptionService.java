package com.work.truetech.services;

import com.work.truetech.entity.Phone;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Option;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IOptionService {
    Option createOption(Option option,Long modelId, MultipartFile file) throws IOException;
    List<Option> retrieveOptions();
    List<Option> retrieveOptionByModel(Long modelId);
    Option getOptionById(Long optionId);
    Option updateOption(Long optionId, Option updatedOption, MultipartFile file) throws IOException;
    void deleteOption(Long id);
    List<Map<String, Long>> getTotalOptionsBoughtByPhone();
}

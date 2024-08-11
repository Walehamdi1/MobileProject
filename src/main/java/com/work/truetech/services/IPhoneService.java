package com.work.truetech.services;

import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Phone;

import java.io.IOException;
import java.util.List;

public interface IPhoneService {
    Phone createPhone(Phone phone, MultipartFile file) throws IOException;
    List<Phone> retrievePhones();

    Phone updatePhone(Long phoneId, Phone updatedPhone, MultipartFile file) throws IOException;
    public void deletePhone(long id);
    public Phone retrievePhoneById(long id);
}

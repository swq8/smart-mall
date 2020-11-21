package alex.service;

import alex.repository.SpecRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SpecService {

    @Resource
    SpecRepository specRepository;

}

package smart.service;

import smart.repository.UserLogRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class UserLogService {

    @Resource
    private UserLogRepository userLogRepository;
}

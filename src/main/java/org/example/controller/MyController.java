package org.example.controller;

import org.demospirng.Autowired;
import org.demospirng.Component;
import org.demospirng.Configuration;
import org.example.domain.User;

/**
 * @author yimingyu
 */
@Component
@Configuration
public class MyController {

    @Autowired
    private User user;
}

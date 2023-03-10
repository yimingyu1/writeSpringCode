package org.example.controller;

import org.demospirng.Autowired;
import org.demospirng.Component;
import org.demospirng.Configuration;
import org.demospirng.Scope;
import org.example.domain.User;

/**
 * @author yimingyu
 */
@Component
@Configuration
@Scope("prototype")
public class MyController {

    @Autowired
    private User user;
}

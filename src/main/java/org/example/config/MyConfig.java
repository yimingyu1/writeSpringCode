package org.example.config;

import org.demospirng.Autowired;
import org.demospirng.Component;
import org.demospirng.ComponentScan;
import org.demospirng.Configuration;
import org.example.domain.User;

/**
 * @author yimingyu
 */
@Configuration()
@ComponentScan("org.example")
@Component
public class MyConfig {

    @Autowired
    private User user;

    @Override
    public String toString() {
        return "MyConfig{" +
                "user=" + user +
                '}';
    }
}

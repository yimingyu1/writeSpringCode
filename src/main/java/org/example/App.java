package org.example;

import org.demospirng.Component;
import org.demospirng.DemoApplicationContext;
import org.example.config.MyConfig;

/**
 * Hello world!
 *
 */
@Component
public class App 
{
    public static void main( String[] args )
    {
        DemoApplicationContext demoApplicationContext = new DemoApplicationContext(MyConfig.class);
        Object myController = demoApplicationContext.getBean("myConfig");
        System.out.println(myController);
        Object myController1 = demoApplicationContext.getBean("myConfig");
        System.out.println(myController1);
        System.out.println(demoApplicationContext.getAllBeans());
    }
}

package org.demospirng;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yimingyu
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component()
public @interface Configuration {

    String value() default "";
}

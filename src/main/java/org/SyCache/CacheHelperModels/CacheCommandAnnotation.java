package org.SyCache.CacheHelperModels;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheCommandAnnotation {
    String commandName() default "";
    String commandPattern() default "";
    String commandDescription() default "";
    String singleCommand() default "";
}

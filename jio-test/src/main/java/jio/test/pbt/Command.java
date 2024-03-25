package jio.test.pbt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The `@Command` annotation is used to mark fields that represent commands within the Property Console. Fields
 * annotated with `@Command` will be identified as callable commands when using the console.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Command {

}

package github.tmx.spring.annotion;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 09:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
@Component
public @interface RpcReference {
}

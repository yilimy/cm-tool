package com.gomain.cm.tool.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定义非空校验器
 * @author caimeng
 * @date 2023/12/22 11:07
 */
public class NotBlankHzValidator implements ConstraintValidator<NotBlankHz, CharSequence> {
    @Value("${hz.validate.enable:false}")
    private boolean enable;

    @Override
    public void initialize(NotBlankHz constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * 自定义非空校验器 <br>
     * 参考 {@link org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator NotBlankValidator}
     * @param charSequence object to validate
     * @param context context in which the constraint is evaluated
     *
     * @return 校验是否通过
     */
    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext context) {
        if (enable) {
            if ( charSequence == null ) {
                return false;
            }
            return charSequence.toString().trim().length() > 0;
        }
        return true;
    }

}

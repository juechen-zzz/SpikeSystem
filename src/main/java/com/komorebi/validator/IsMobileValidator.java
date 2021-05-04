package com.komorebi.validator;
import  javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.komorebi.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

// JSR303参数检验
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required) {
			return ValidatorUtil.isMobile(value);
		}else {
			if(StringUtils.isEmpty(value)) {
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}

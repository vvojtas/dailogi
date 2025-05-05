package com.github.vvojtas.dailogi_server.validation;

import com.github.vvojtas.dailogi_server.model.auth.request.RegisterCommand;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterCommand> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(RegisterCommand command, ConstraintValidatorContext context) {
        if (command == null) {
            return true; 
        }
        return command.password() != null && command.password().equals(command.passwordConfirmation());
    }
} 
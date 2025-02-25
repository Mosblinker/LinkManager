/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * This wraps an {@link GeneralSecurityException} with an unchecked exception.
 * @author Milo Steier
 */
public class UncheckedSecurityException extends RuntimeException{
    /**
     * This constructs an instance of this class with the given message and 
     * cause.
     * @param message The detail message, or null.
     * @param cause The {@code GeneralSecurityException} (cannot be null).
     * @throws NullPointerException If the cause is null.
     */
    public UncheckedSecurityException(String message, GeneralSecurityException cause){
        super(message,Objects.requireNonNull(cause, "Cause cannot be null"));
    }
    /**
     * This constructs an instance of this class with the given cause.
     * @param cause The {@code GeneralSecurityException} (cannot be null).
     * @throws NullPointerException If the cause is null.
     */
    public UncheckedSecurityException(GeneralSecurityException cause){
        super(Objects.requireNonNull(cause, "Cause cannot be null"));
    }
    /**
     * This returns the cause of this exception.
     * @return The {@code GeneralSecurityException} that was the cause of this 
     * exception.
     */
    @Override
    public GeneralSecurityException getCause(){
        return (GeneralSecurityException) super.getCause();
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import java.io.*;
import java.util.*;

/**
 * This wraps an {@link DbxException} with an unchecked exception.
 * @author Milo Steier
 */
public class UncheckedDbxException extends RuntimeException{
    /**
     * This constructs an instance of this class with the given message and 
     * cause.
     * @param message The detail message, or null.
     * @param cause The {@code DbxException} (cannot be null).
     * @throws NullPointerException If the cause is null.
     */
    public UncheckedDbxException(String message, DbxException cause){
        super(message,Objects.requireNonNull(cause, "Cause cannot be null"));
    }
    /**
     * This constructs an instance of this class with the given cause.
     * @param cause The {@code DbxException} (cannot be null).
     * @throws NullPointerException If the cause is null.
     */
    public UncheckedDbxException(DbxException cause){
        super(Objects.requireNonNull(cause, "Cause cannot be null"));
    }
    /**
     * This returns the cause of this exception.
     * @return The {@code DbxException} that was the cause of this exception.
     */
    @Override
    public DbxException getCause(){
        return (DbxException) super.getCause();
    }
    /**
     * This returns the unique ID associated with the request that triggered the 
     * {@code DbxException} that caused this exception. <p>
     * 
     * The ID may be null if we could not receive a response from the Dropbox 
     * servers.
     * @return The unique ID associated with the request that caused this 
     * exception, or null if one is not available.
     */
    public String getRequestId(){
        return getCause().getRequestId();
    }
    /**
     * Called to read the object from a stream.
     *
     * @throws InvalidObjectException if the object is invalid or has a cause 
     * that is not an {@code DbxException}
     */
    private void readObject(ObjectInputStream s)throws IOException, 
            ClassNotFoundException {
        s.defaultReadObject();  // Read the object from stream
            // Get the cause provided by the stream
        Throwable cause = super.getCause();
            // If the cause is not an DbxException
        if (!(cause instanceof DbxException))
            throw new InvalidObjectException("Cause must be an DbxException");
    }
}

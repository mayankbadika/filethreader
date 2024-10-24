/**
 * This class serves as a global exception handler in a Spring application.
 *
 * 1. **Purpose**: The primary role of this class is to centralize exception handling
 *    across all controllers, allowing for a consistent response format for errors.
 *
 * 2. **Naming Convention**: While the name 'GlobalExceptionHandler' is commonly used
 *    to indicate its functionality, it is not mandatory. Developers can choose any
 *    descriptive name that fits their project's naming conventions.
 *    Examples: 'CustomExceptionHandler', 'AppExceptionHandler', 'SecurityExceptionHandler'.
 *
 * 3. **Annotations**: The essential annotation for this class is
 *    '@RestControllerAdvice' or '@ControllerAdvice'. This annotation informs Spring
 *    that the class is intended for global exception handling, regardless of its name.
 *
 * 4. **Method Annotations**: Inside this class, methods can be annotated with
 *    '@ExceptionHandler' to specify how particular exceptions should be handled.
 *    These methods will be invoked automatically by Spring when an exception of
 *    the specified type is thrown.
 *
 * 5. **Maintainability**: Using descriptive names improves code readability and
 *    maintainability. Clear naming helps other developers (or yourself in the future)
 *    to understand the purpose of the class quickly.
 *
 * Overall, the key takeaway is that while naming the class 'GlobalExceptionHandler'
 * is a helpful convention, it is not a requirement. Developers have the flexibility
 * to name the class as desired, as long as they apply the appropriate annotations
 * to enable Spring's exception handling capabilities.
 */

package com.example.filethreader.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//The @RestControllerAdvice annotation tells Spring that your class is a global exception handler.
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;

        // TODO send this stack trace to an observability tool
        exception.printStackTrace();

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
            errorDetail.setProperty("description", "The username or password is incorrect");

            return errorDetail;
        }

        if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The account is locked");
        }

        if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "You are not authorized to access this resource");
        }

        if (exception instanceof SignatureException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The JWT signature is invalid");
        }

        if (exception instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The JWT token has expired");
        }

        if (errorDetail == null) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        return errorDetail;
    }
}

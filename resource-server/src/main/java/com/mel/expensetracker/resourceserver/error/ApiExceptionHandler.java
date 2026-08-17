package com.mel.expensetracker.resourceserver.error;

import com.mel.expensetracker.resourceserver.report.ReportNotFoundException;
import com.mel.expensetracker.resourceserver.report.VersionConflictException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Supplements Spring MVC's own {@code ProblemDetail} support (Bean Validation
 * failures, unmapped paths -- see {@code spring.mvc.problemdetails.enabled})
 * with the two exceptions unique to this API's domain.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ProblemDetail handleNotFound(ReportNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(VersionConflictException.class)
    public ProblemDetail handleVersionConflict(VersionConflictException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, ex.getMessage());
        problem.setType(URI.create("urn:expensetracker:problem:version-conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}

package base.boudicca.publisher.event.html.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.util.*

@ControllerAdvice
class GlobalDefaultExceptionHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @ExceptionHandler(value = [Exception::class])
    @Throws(Exception::class)
    fun defaultErrorHandler(req: HttpServletRequest, e: Exception): ModelAndView {
        if (AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java) != null) {
            throw e
        }

        if (e is NoResourceFoundException) {
            return handleError404()
        }
        return handleErrorUnknown(req, e)
    }

    private fun handleError404(): ModelAndView {
        val mav = ModelAndView()
        mav.viewName = "error404"
        mav.status = HttpStatus.NOT_FOUND
        return mav
    }

    private fun handleErrorUnknown(req: HttpServletRequest, e: Exception): ModelAndView {
        val errorId = UUID.randomUUID().toString()

        MDC.putCloseable("errorId", errorId)
            .use {
                logger.error(e) { "errorId '$errorId' handling url '${req.requestURL}' with queryParams '${req.queryString}'" }
            }

        val mav = ModelAndView()
        mav.addObject("errorId", errorId)
        mav.viewName = "errorUnknown"
        mav.status = HttpStatus.INTERNAL_SERVER_ERROR
        return mav
    }
}

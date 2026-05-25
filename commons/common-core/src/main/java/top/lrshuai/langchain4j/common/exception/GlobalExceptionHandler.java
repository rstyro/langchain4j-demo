package top.lrshuai.langchain4j.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.lrshuai.langchain4j.common.resp.R;

/**
 * 全局异常捕获
 * @author rstyro
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public R notPermission(MethodArgumentNotValidException ex){
		String message = ex.getBindingResult().getFieldError().getDefaultMessage();
		log.error(ex.getMessage(),ex);
		return R.error(message);
	}

	/**
	 * 服务器自定义异常
	 */
	@ExceptionHandler(ServiceException.class)
	public R ApiException(ServiceException ex) {
		log.error(ex.getMessage(),ex);
		return R.error().code(ex.getCode()).msg(ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public R exception(Exception ex){
		log.error(ex.getMessage(),ex);
		return R.error(ex.getMessage());
	}

}

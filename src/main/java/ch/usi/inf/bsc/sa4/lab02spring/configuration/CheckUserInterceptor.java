package ch.usi.inf.bsc.sa4.lab02spring.configuration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
   * @return true if the user is authenticated and the execution chain should proceed;
   * false if the user is unauthenticated, in which case a redirect is issued to (users/me)
   * and the request execution is halted.
   * @throws Exception in case of errors during redirect or session access
   */
   @Component
public class CheckUserInterceptor implements HandlerInterceptor {
   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       // check if user is logged in
       Object user = "usertoken";
       if (user == null) {
           response.sendRedirect("/users/me");
           return false;
       }
       return true;
   }
}

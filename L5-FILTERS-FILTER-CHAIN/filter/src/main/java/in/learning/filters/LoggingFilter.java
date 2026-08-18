package in.learning.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(2)
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        // CASTING
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;



        String requestId = UUID.randomUUID().toString();
        httpResponse.setHeader("REQUEST-ID",requestId);
        //LOGG REQUEST
        System.out.println("Incoming Request: "+httpRequest.getMethod()+" "+httpRequest.getRequestURI());



        //PASSING TO OTHER FILTER
        try{
            chain.doFilter(servletRequest,servletResponse);
        }
        finally {

            // FINAL RESPONSE WE GET
            System.out.println("Response Status : "+ httpResponse.getStatus());

        }



    }
}

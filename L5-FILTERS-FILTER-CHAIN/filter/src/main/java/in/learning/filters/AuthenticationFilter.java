package in.learning.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        // CASTING- THIS IS DONE BECAUSE WE HAVE TO WORK WITH HTTP REQUEST AND RESPONSES
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;


        // CHECKING IF THE REQUEST IS COMING WITH A CORRECT  TOKEN OR NOT
        String token = httpRequest.getHeader("token");

        if(token==null || !token.equals("12345")){
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //PASSING TO OTHER FILTER
            chain.doFilter(servletRequest,servletResponse);


    }
}

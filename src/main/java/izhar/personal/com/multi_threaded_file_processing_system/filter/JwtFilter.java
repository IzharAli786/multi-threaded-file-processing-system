//package izhar.personal.com.multi_threaded_file_processing_system.filter;
//
//import izhar.personal.com.multi_threaded_file_processing_system.utils.JwtUtils;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//  @Autowired
//  private UserDetailsService userDetailsService;
//  @Autowired
//  private JwtUtils jwtUtils;
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//    String authorizationHeader = request.getHeader("Authorization");
//    String jwtToken = null;
//    String userName = null;
//
//
//    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//      jwtToken = authorizationHeader.substring(7);
//      userName = jwtUtils.extractUserEmail(jwtToken);
//    }
//    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
//
//    if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//      if (jwtUtils.validateToken(userDetails.getUsername(), jwtToken)) {
//        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
//        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(auth);
//      }
//    }
//    filterChain.doFilter(request, response);
//  }
//}

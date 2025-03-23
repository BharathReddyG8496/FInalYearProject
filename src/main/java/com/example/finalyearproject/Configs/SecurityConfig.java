package com.example.finalyearproject.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
//@EnableWebSecurity
public class SecurityConfig {


//  @Bean
//  public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
//     return http.csrf(AbstractHttpConfigurer::disable)
//              .authorizeHttpRequests(auth-> auth
//                      .requestMatchers("/").permitAll()
//                      .anyRequest().authenticated()
//              )
//              .sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//              .build();
//  }

  /*
  To create this Bean it requires Spring-Security dependency.
   */
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider(){
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        daoAuthenticationProvider.setUserDetailsService(userDetailService);
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//        return daoAuthenticationProvider;
//    }
//  public PasswordEncoder passwordEncoder(){
//      return new BCryptPasswordEncoder();
//  }
}
